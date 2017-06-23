(ns wtdash.pages.mainpage.core
  (:require
    [reagent.core :as reagent]
    [clojure.set :refer [rename-keys]]
    [wtdash.data.db :as mydb]
    [wtdash.data.subs :as data-subs]
    [wtdash.pages.mainpage.subs :as mainpage-subs]
    [wtdash.data.util :as data-util]
    [wtdash.pages.mainpage.handlers :as mainpage-handles]
    [wtdash.widgets.loadingoverlay :refer [LoadingOverlay]]
    [wtdash.widgets.dropdownmenu :refer [DropdownMenuWithBlank]]
    [wtdash.widgets.box :refer [BoxContainer]]
    [wtdash.widgets.highchart :as highchart :refer [HighChart]]
    [wtdash.widgets.datatable :refer [DataTable]]
    [wtdash.utils.format :as rformat]
    [wtdash.utils.common :as com-utils]
    [wtdash.utils.merge :as merge-utils]
    [wtdash.utils.table :as table-utils]
    [wtdash.components.welltesttable :refer [WellTestInfo]]
    [wtdash.components.tubingpipe :refer [TubingPipe]]
    [wtdash.components.glvtable :refer [GLVTable]])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))




(defn ContentMsg
  "Simple component to display a message to the user on the WellOverview page in place of the content"
  [msg]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"}}
   [:p {:style {:font-size "2rem"}} msg]])

(defn upload-btn [file-name]
  [:span.upload-label
   [:label
    [:input.hidden-xs-up
     {:type "file" :accept ".csv"}]
    ;{:type "file" :accept ".csv" :on-change put-upload}]
    [:i.fa.fa-upload.fa-lg]
    (or file-name "click here to upload and render csv...")]])
;(when file-name
;  [:i.fa.fa-times {:on-click #(reset! app-state {})}])])

;;------------------chart---------------------------
(defn reset-chart []
  (.log js/console "reset chart"))


(defn reset-welltest [])
(.log js/console "reset welltest")

(defn home-render []
  [:div {:style {:min-width "310px" :max-width "800px"
                 :height "800px" :margin "0 auto"}}])

(defn chart-display [this]
  (reagent/create-class {:reagent-render
                         (fn []
                           [:div {:style {:min-width "300px" :max-width "800px"
                                          :height "500px" :margin "0 auto"}}])
                         :component-did-mount
                         (fn [this]
                           (js/Highcharts.Chart. (reagent/dom-node this)
                                                 (clj->js mydb/chart-data)))}))

(defn DVSPChart []
  (let [data-source (mainpage-subs/get-selected-datasource)
        well (mainpage-subs/get-selected-well)
        dvsp-config (mainpage-subs/get-dvsp-config)
        depth-profile (data-subs/get-depth-profile)
        equilibrium-profile (data-subs/get-equilibrium-profile)
        ppm-curve (:production-string-depth-profile-map depth-profile)
        ipm-curve (:injection-string-depth-profile-map depth-profile)
        valve-map (:valves-status-map depth-profile)
        mandrel-survey (data-subs/get-mandrel-survey)
        vert-depth-list (:vert-depth-list ppm-curve)
        max-depth (* (+ (int (/ (apply max vert-depth-list) 1000)) 1) 1000)
        plot-lines (vec (for [mandrel (:vert-depth-list valve-map)]
                          {:color "#000000"
                           :value mandrel
                           :marker {:enabled false}
                           :zIndex 1 ;; Show above gridlines
                           :width 2
                           :label {:text (str "Mandrel Line: " (rformat/format-dec mandrel 2) " ft")}
                           :tooltip {:headerFormat
                                                     (str "<span style=\"font-size: 10px\">Mandrel</span><br/>")
                                     :pointFormatter com-utils/mandrel-point-formatter}}))
        filtered-valve-map (com-utils/filter-dummy-valves-from-valve-map valve-map)
        chart-config (-> (merge-utils/deep-merge
                           (highchart/prep-chart-config
                             dvsp-config
                             {:ppm (table-utils/map-table-to-array
                                     ppm-curve :flow-press-list :vert-depth-list)}
                             {:ipm (table-utils/map-table-to-array
                                     ipm-curve :flow-press-list :vert-depth-list)}
                             {:eq (table-utils/map-table-to-array
                                    equilibrium-profile :flow-press-list :vert-depth-list)}
                             {:open-points (table-utils/map-table-to-array
                                             filtered-valve-map :open-press-list :vert-depth-list)}
                             {:close-points (table-utils/map-table-to-array
                                              filtered-valve-map :close-press-list :vert-depth-list)}
                             {:vpc-bfp (table-utils/map-table-to-array
                                         filtered-valve-map :vpc-begin-flow-list :vert-depth-list)})
                           {:chart {:height 500}}
                           {:yAxis {:plotLines plot-lines
                                    :max max-depth}})
                         ;; There's no way to turn plotlines off/on so we have a dummy series and remove/add the
                         ;; plot lines
                         (update-in [:series] #(into %1 %2)
                                    [{:marker {:enabled false} ;; Don't show the little marker on the legend to remain consistent with other curves
                                      :events {:show #(let [y-axis (aget (js* "this") "chart" "yAxis" 0)]
                                                        (.update y-axis (clj->js {:plotLines plot-lines})))
                                               :hide #(let [chart-plot-lines (aget % "target" "chart" "yAxis" 0 "plotLinesAndBands")]
                                                        (doall
                                                          (for [idx (range (- (aget chart-plot-lines "length") 1) -1 -1)]
                                                            (.destroy (aget chart-plot-lines idx)))))}
                                      :name "Mandrel Lines"}]))]
    (.log js/console "chart: " chart-config)
    (if (and
          (some? depth-profile)
          (some? max-depth))
      [:div.row
       ;[:div.col-xs-3.col-sm-2
       ; (if (and (some? max-depth)
       ;          (some? depth-profile))
       ;   [TubingPipe data-source well {:max-depth max-depth :height 480}]
       ;   [LoadingOverlay])]
       [:div.col-xs-9.col-sm-10
        (if (and  (some? depth-profile)
                  (some? max-depth)
                  (some? equilibrium-profile)
                  (some? ppm-curve)
                  (some? ipm-curve)
                  (some? valve-map)
                  (some? mandrel-survey))
          [HighChart chart-config]
          [LoadingOverlay])]]
      [LoadingOverlay])))


;;--------------------------------------------------------------------------------
(defn DataSourceDropdown []
  "Data Source selector for the well picker"
  (let [datasources (mainpage-subs/get-datasources)
        selected-data-source (mainpage-subs/get-selected-datasource)]
    (if (some? datasources)
      (if (= 1 (count datasources))
        (do
          (mainpage-handles/set-selected-datasource (first (keys (first datasources))))
          [:div])
        [:div {:style {:margin-bottom "15px"}}
         [:label "Data Source"]
         [:div
          [DropdownMenuWithBlank (when (some? datasources) datasources)
           selected-data-source
           #(mainpage-handles/set-selected-datasource %)]]])
      [LoadingOverlay])))


(defn WellSelector [data-source on-select-fn]
  (let [well-list (mainpage-subs/get-all-well-status)]
    [:div
     (if (some? well-list)
       [DataTable
        {:data well-list
         :searching false
         :paging false
         :scrollY 400
         :columns [{:title "Well"
                    :data :well}
                   {:title "Lease"
                    :data :lease}
                   {:title "Completion"
                    :searchable false
                    :data :cmpl}
                   {:title "GL status"
                    :searchable false
                    :data :glstatus
                    :render data-util/render-gl-status}]
         :deferRender true
         :select "single"}
        {:select (fn [e dt type index]
                   (on-select-fn (-> (.rows dt index)
                                     (.data)
                                     (aget 0)
                                     (js->clj)
                                     (rename-keys {"field" :field
                                                   "lease" :lease
                                                   "well" :well
                                                   "cmpl" :cmpl
                                                   "glstatus" :glstatus}))))}])]))


(defn WellPicker []
  (let [selected-data-source (mainpage-subs/get-selected-datasource)]
    [:div
     [DataSourceDropdown]
     (if (nil? selected-data-source)
       [ContentMsg "Select a Data Source"]
       [:div
        ;; Well Selector
        [BoxContainer
         {:header
          {:title "Select Well"
           :with-border true}}
         [WellSelector selected-data-source
          #(do
             (mainpage-handles/set-selected-well %))]]])]))

;;------------Data table------------------------------------------------------------
(defn WellTestInfo []
  (let [welltest-hist-map (data-subs/get-welltest-hist)
        welltest-list (vals welltest-hist-map)
        indata (map (fn [in] {:welltest-date (rformat/format-iso-date (:welltest-date in))
                              :calib-oil-rate (rformat/format-dec (:calib-oil-rate in) 2)
                              :calib-water-rate (rformat/format-dec (:calib-water-rate in) 2)
                              :calib-liquid-rate (rformat/format-dec (:calib-liquid-rate in) 2)
                              :calib-wc (rformat/format-dec (:calib-wc in) 2)
                              :calib-total-gas (rformat/format-dec (:calib-total-gas in) 2)
                              :calib-flowing-tubing-press (rformat/format-dec (:calib-flowing-tubing-press in) 2)
                              :calib-casing-head-press (rformat/format-dec (:calib-casing-head-press in) 2)
                              :est-fbhp (rformat/format-dec (:est-fbhp in) 2)
                              :calib-lift-gas-rate (rformat/format-dec (:calib-lift-gas-rate in) 2)
                              :calib-total-glr (rformat/format-dec (:calib-total-glr in) 2)
                              :calib-formation-gas-rate (:calib-formation-gas-rate in) })(sort-by :welltest-date > welltest-list))]
    (.log js/console "Well test info: " indata)
    [:div
     [BoxContainer
      {:header
       {:title "Well Test History"
        :with-border true}}
      (if (> (count indata) 1)
        [DataTable
         {:data indata
          :columns [{:title "Date"
                     :data :welltest-date}
                    {:title "Oil (bbl/day)"
                     :data :calib-oil-rate}
                    {:title "Water (bbl/day)"
                     :data :calib-water-rate}
                    {:title "Total liquid (bbl/day)"
                     :data :calib-liquid-rate}
                    {:title "Watercut (%)"
                     :data :calib-wc}
                    {:title "Total Gas (MCF/data)"
                     :data :calib-total-gas}
                    {:title "Tubing Press. (psig)"
                     :data :calib-flowing-tubing-press}
                    {:title "Casing Press. (psig)"
                     :data :calib-casing-head-press}
                    {:title "Stored FBHP (psig)"
                     :data :est-fbhp}
                    {:title "Gas LG (MCF/data)"
                     :data :calib-lift-gas-rate}
                    {:title "Total GL Rate (MCF/day)"
                     :data :calib-total-glr}]
          :deferRender true
          :select "single"}
         {:select (fn [e dt type index])}]
        [LoadingOverlay])]]))

;;----oil rate over time------------------------------------

(defn OilrateInf []
  (let [welltest-hist-map (data-subs/get-welltest-hist)
        welltest-list (vals welltest-hist-map)
        indata (vec (map (fn [in] [(com-utils/getUTCtime (:welltest-date in)) (js/parseFloat (rformat/format-dec (:calib-oil-rate in) 2))])
                         (sort-by :welltest-date > welltest-list)))
        chart-config {:chart {:type "spline"}
                      :title {:text "Oil rate vs. time"}
                      :xAxis {:type "datetime"
                              :labels {:format "{value:%Y-%m-%d}"}
                              :title {:text "Date"}}
                      :yAxis {:title {:text "Oil rate (bbq/day)"}}
                      :series [{:name "Oil rate"
                                :data indata}]}]
    [:div
     [BoxContainer
      {:header
       {:title "Oil rate vs. time"
        :with-border true}}
      [HighChart chart-config]]]))


(defn SelectedWellInf []
  (let [selected-well (mainpage-subs/get-selected-well)
        field (:field selected-well)
        lease (:lease selected-well)
        well (:well selected-well)
        cmpl (:cmpl selected-well)]
    [:div
     [:p (str " Field: " field "  |Lease: " lease " |Well: " well "  |Cmpl: " cmpl)]]))


(defn well-summary-table [data on-select-fn]
  (fn []
    [DataTable
     {
      :data data
      :paging false
      :scrollY 400
      :searching false
      :columns [{:title "Well"
                 :data :name}
                {:title "Diagsnostic Date"
                 :data :date}
                {:title "GL Status"
                 :data :status
                 :render data-util/render-gl-status}
                {:title "Valves Status"
                 :data :desciption}
                {:title "GLIR Calculated"
                 :data :rate}]
      :deferRender true
      :select "single"}
     {:select (fn [e dt type index]

                (on-select-fn (-> (.rows dt index)
                                  (.data)
                                  (aget 0)
                                  (js->clj)
                                  (rename-keys {"well" :name
                                                "date" :date}))))}]))

(defn welltest-table [data]
  (fn []
    [DataTable
     {
      :data data
      :paging false
      :searching false
      :columns [{:title "Date"
                 :data :date}
                {:title "Oil Rate"
                 :data :oil-rate}
                {:title "Water Rate"
                 :data :water-rate}
                {:title "Formation Gas"
                 :data :form-gas}
                {:title "LG Rate"
                 :data :lg-rate}
                {:title "Production Pressure"
                 :data :prod-press}
                {:title "Injection Pressure"
                 :data :inj-press}
                {:title "Choke size"
                 :data :choke}
                {:title "Separate Pressure"
                 :data :sep-press}]
      :deferRender true
      :select "single"}]))

;;----------------------------------------------------------------------------

(defn main-content []
  [:div
   [BoxContainer {:solidBox true}
    [:div
     [:div.row
      [:div.col-md-6
       [WellPicker]]
      [:div.col-sm-6.col-md-6
       [BoxContainer
        {:header
         {:title "Selected Well"
          :with-border true}}
        [SelectedWellInf]]
       [BoxContainer
        [DVSPChart]]]]
     [:div.row
      [:div.col-sm-12.col-md-12
       [OilrateInf]]]
     ;[:div.row
     ; [:div.col-sm-12.col-md-12
     ;  [WellTestInfo]]]
     [:div.row
      [:div.col-md-12
       [BoxContainer {:table-responsive true}
        [WellTestInfo]]]]
     [:div.row
      [:div.col-md-12
       [BoxContainer {:header {:title "Gas Lift Valves"}
                      :table-responsive true}
        [GLVTable]]]]]]])
;; -------------------------

(ns wtdash.pages.mainpage.core
  (:require
    [reagent.core :as reagent]
    [clojure.set :refer [rename-keys]]
    [wtdash.widgets.datatable :refer [DataTable]]
    [wtdash.data.db :as db]
    [wtdash.data.util :as data-util])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))


;;------------------chart---------------------------
(defn reset-chart []
  (.log js/console "reset chart"))


(defn reset-welltest [])
(.log js/console "reset welltest")

(def chart-state (atom {}))

(defn test-chart-config [data]
  (reset!  chart-state data))

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
                                                 (clj->js (test-chart-config db/chart-data))))}))

(defn upload-btn [file-name]
  [:span.upload-label
   [:label
    [:input.hidden-xs-up
     {:type "file" :accept ".csv" :on-change put-upload}]
    [:i.fa.fa-upload.fa-lg]
    (or file-name "click here to upload and render csv...")]
   (when file-name
     [:i.fa.fa-times {:on-click #(reset! app-state {})}])])

;;------------Data table------------------------------------------------------------
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
   [:div
    [:div {:style {:width "45%" :float "left" :margin "40px 20px 20px 20px"}}

     [:div {:style {:height "50px" :text-align "center" :font-weight "600" :font-size "large"}} "Wells Summary"]
     ;[WellPicker]
     (.log js/console db/well-data)
     [well-summary-table db/well-data
      #(do
         (reset-chart)
         (reset-welltest))]]
    [:div {:style {:width "50%" :float "right" :margin "50px 20px 0 0"}}
     [chart-display]]]


   [:div
    [:div{:style {:width "95%" :align "center" :margin "40px 20px 20px 20px"}}

     [:div {:style {:height "150px" :text-align "center" :font-weight "600" :font-size "large"}} "Well Test History"]
     [welltest-table db/welltest-data]]]

     ;[welltest-table-display]]]])
   [:div.topbar.hidden-print
    [upload-btn]]])
;; -------------------------

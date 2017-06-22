(ns wtdash.data.db
  (:require [reagent.core :as reagent]
    [wtdash.utils.format :as rformat]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars
(defn series-tooltip-formatter
  [x-text y-text x-units y-units]
  (fn []
    (let [js-point (js* "this")
          y (rformat/format-dec (aget js-point "y") 2)
          x (rformat/format-dec (aget js-point "x") 2)
          color (aget js-point "series" "color")]
      (str "<span style=\"color:" color "\">\u25CF</span>" x-text ": <b>" x "</b> " x-units "<br/>"
           "<span style=\"color:" color "\">\u25CF</span>" y-text ": <b>" y "</b> " y-units))))

(defn line-tooltip
  [x-text y-text x-units y-units]
  {:headerFormat
   ;; Makes the date appear in the tooltip
                   "<span style=\"font-size: 10px\">{series.name}</span><br/>"
   :pointFormatter (series-tooltip-formatter x-text y-text x-units y-units)})

(defn scatter-tooltip
  [x-text y-text x-units y-units]
  {:headerFormat
   ;; Makes the date appear in the tooltip
                   "<span style=\"font-size: 10px\">
                   {series.name}<br/>
                   {point.key}<br/>
                   </span>"
   :pointFormatter (series-tooltip-formatter x-text y-text x-units y-units)})

(def dvsp-tooltip
  (line-tooltip "Pressure" "Depth" "psig" "ft"))

(def dvsp-scatter-tooltip
  ;; Don't want point.key in these
  {:headerFormat
   ;; Makes the date appear in the tooltip
                   "<span style=\"font-size: 10px\">
                   {series.name}<br/>
                   </span>"
   :pointFormatter (series-tooltip-formatter "Presure" "Depth" "psig" "ft")})

(def pvsq-tooltip
  (line-tooltip "Liquid Rate" "Pressure" "bbl/day" "psig"))

(def pvsq-scatter-tooltip
  (scatter-tooltip "Liquid Rate" "Pressure" "bbl/day" "psig"))

(def qvsi-tooltip
  (line-tooltip "Injection" "Production" "MCF/day" "bbl/day"))

(def qvsi-scatter-tooltip
  (scatter-tooltip "Injection" "Production" "MCF/day" "bbl/day"))

(defonce local-app-state
         (reagent/atom
           {:user nil
            :group nil
            :input-text ""
            :open-well-selector false
            :network {:connected true}
            :chart/by-id {:dvsp {:id     "dvsp"
                                 :title  {:text "Depth vs. Pressure"}
                                 :xAxis  {:title {:text "Pressure (psig)"}
                                          :opposite true ;; Draw on top of graph
                                          :reversed false}
                                 :yAxis  {:title {:text "Depth (ft)"}
                                          :reversed  true}
                                 :series {:ppm
                                          {:name  "Production Pres."
                                           :color "#008B8B"
                                           :tooltip dvsp-tooltip
                                           :marker {:enabled false}}
                                          :ipm
                                          {:name "Injection Pres."
                                           :color "#EE0000"
                                           :tooltip dvsp-tooltip
                                           :marker {:enabled false}}
                                          :eq
                                          {:name "Equilibrium"
                                           :tooltip dvsp-tooltip
                                           :marker {:enabled false}}
                                          :open-points
                                          {:name "Opening Points"
                                           :color "#00FF00"
                                           :type "scatter"
                                           :tooltip dvsp-scatter-tooltip}
                                          :close-points
                                          {:name "Closing Points"
                                           :color "#FF0000"
                                           :type "scatter"
                                           :tooltip dvsp-scatter-tooltip}
                                          :vpc-bfp
                                          {:name "VPC BFP Points"
                                           :color "#0000FF"
                                           :type "scatter"
                                           :tooltip dvsp-scatter-tooltip}}}
                          :pvsq {:id "pvsq"
                                 :title  {:text "Pressure vs Production"}
                                 :xAxis  {:title {:text "Liquid Rate (bbl/day)"}
                                          :reversed false}
                                 :yAxis  {:title {:text "Pressure (psig)"}
                                          :reversed false}
                                 :series {:meas_outflow_liquid_rate
                                          {:name "Measured Outflow - Gross Liquid"
                                           :color  "#00008B"
                                           :tooltip pvsq-tooltip
                                           :marker {:enabled false}}
                                          :meas_inflow_liquid_rate
                                          {:name  "Measured IPR - Gross Liquid"
                                           :color "#FF00FF"
                                           :tooltip pvsq-tooltip
                                           :marker {:enabled false}}
                                          :well-tests
                                          {:name "Well Test Points"
                                           :type "scatter"
                                           :color  "#00ff00"
                                           :xAxis 0
                                           :yAxis 0
                                           :tooltip pvsq-scatter-tooltip}
                                          :uncal-well-tests
                                          {:name "Well Test Points (Uncalibrated)"
                                           :type "scatter"
                                           :color  "#ff0000"
                                           :xAxis 0
                                           :yAxis 0
                                           :tooltip pvsq-scatter-tooltip}}}
                          :qvsi {:id "qvsi"
                                 :title  {:text "Production vs Injection"}
                                 :xAxis  {:title {:text "Injection (MCF/day)"}
                                          :reversed false}
                                 :yAxis  {:title {:text "Production (bbl/day)"}
                                          :reversed false}
                                 :series {:lg-resp
                                          {:name "Oil Rate Response"
                                           :lineWidth 3
                                           :tooltip qvsi-tooltip
                                           :marker {:enabled false}}
                                          :uncal-wellotests
                                          {:name "Well Test Oil Points (Uncalibrated)"
                                           :type "scatter"
                                           :color  "#ff0000"
                                           :tooltip qvsi-scatter-tooltip}
                                          :wellotests
                                          {:name "Well Test Oil Points (Calibrated)"
                                           :type "scatter"
                                           :color  "#00ff00"
                                           :tooltip qvsi-scatter-tooltip}}}}}))

(defonce well-state (reagent/atom {}))
(defonce field-state (reagent/atom {}))

;;---------highcharts--------------------

(def default-chart-config
  "The base default options we apply to HighChart in our application.
   We apply this to normalize the style of highcharts throughout our application.
   Any of the options can be overriden by the chart-config parameter passed to the
   HighChart component."
  {
   :credits {:enabled false}
   :legend  {:layout        "horizontal"
             :align         "center"
             :verticalAlign "bottom"
             :borderWidth   0}
   :yAxis {:labels {:format "{value}"}}
   :xAxis {:gridLineWidth 1
           :labels {:format "{value}"}}})

(def chart-data
  {
   :title {:text "Production Chart"}
   :subtitle {:text "Source: AppSmiths.com"}
   :xAxis {:title {:text "Pressure"}}
   :yAxis {:min 0
           :title {:text "Depth"
                   :align "middle"}
           :labels {:overflow "justify"}}
   :tooltip {:valueSuffix " psi"}
   :plotOptions {:series {:dataLabels {:enabled true}}}
   :legend {:layout "vertical"
            :align "center"
            :verticalAlign "bottom"
            :floating false
            :borderWidth 1
            :shadow true}
   :credits {:enabled false}
   :series [{:name "Production press."
             :data [4394, 5250, 5777, 6965, 9703, 11931, 13133, 24175]}
            {:name "Injection press."
             :data [24916, 24064, 29742, 29851, 32490, 30282, 38121, 40434]}
            {:name "Measured Outflow"
             :data ["null", "null", 7988, 12169, 15112, 22452, 34400, 34227]}
            {:name "Oil Rate"
             :data [12908, 5948, 8105, 11248, 8989, 11816, 18274, 18111]}]})

;;-----------Handsontable------------------------------------------------------
(def table-data
  {
   :colHeaders ["Well" "Diagnostic Date" "GL Status" "GL Valves Status" "GLIR Calculated"]
   :columnSorting true
   :columnHeaderHeight 35
   :rowHeights 25
   :width 500
   :manualColumnResize true
   :data
   [{:name "Well 1" :date "11/03/2015 8:00AM" :status 2 :desciption "Valves 1, 2 of 3 Inj" :rate 0.402}
    {:name "Well 2" :date "11/03/2015 8:00AM" :status 1 :desciption "Valve 2 of 3 Inj" :rate 0.400}
    {:name "Well 3" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1, 2 of 3 Inj" :rate 0.555}
    {:name "Well 4" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 3,4 of 4 Inj" :rate 0.444}
    {:name "Well 5" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1 of 3 Inj" :rate 0.666}
    {:name "Well 6" :date "11/03/2015 8:00AM" :status 1 :desciption "Valves 2,3 of 4 Inj" :rate 0.333}
    {:name "Well 7" :date "11/03/2015 8:00AM" :status 2 :desciption "Valves 1 of 4 Inj" :rate 0.404}
    {:name "Well 8" :date "11/03/2015 8:00AM" :status 1 :desciption "Valves 1, 2 of 3 Inj" :rate 0.606}
    {:name "Well 9" :date "11/03/2015 8:00AM" :status 1 :desciption "Valves 2, 3 of 3 Inj" :rate 0.777}
    {:name "Well 10" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 2,3  of 4 Inj" :rate 0.555}
    {:name "Well 11" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1, 2 of 3 Inj" :rate 0.788}
    {:name "Well 12" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1, 2 of 3 Inj" :rate 0.345}]

   :columns
   [
    {:data "name" :renderer "html"}
    {:data "date" :renderer "html"}
    {:data "status" :renderer (fn [instance td row col prop value cellProperties]
                                (js/imageRenderer instance td row col prop value cellProperties))}

    {:data "desciption" :renderer "html"}
    {:data "rate" :renderer "html"}]

   :editor false
   :rowHeaders  true
   :contextMenu false
   :autoWrapRow true})

(def well-data
  [{:name "Well 1" :date "11/03/2015 8:00AM" :status 2 :desciption "Valves 1, 2 of 3 Inj" :rate 0.402}
   {:name "Well 2" :date "11/03/2015 8:00AM" :status 2 :desciption "Valve 2 of 3 Inj" :rate 0.400}
   {:name "Well 3" :date "11/03/2015 8:00AM" :status 1 :desciption "Valves 1, 2 of 3 Inj" :rate 0.555}
   {:name "Well 4" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 3,4 of 4 Inj" :rate 0.444}
   {:name "Well 5" :date "11/03/2015 8:00AM" :status 1 :desciption "Valves 1 of 3 Inj" :rate 0.666}
   {:name "Well 6" :date "11/03/2015 8:00AM" :status 1 :desciption "Valves 2,3 of 4 Inj" :rate 0.333}
   {:name "Well 7" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1 of 4 Inj" :rate 0.404}
   {:name "Well 8" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1, 2 of 3 Inj" :rate 0.606}
   {:name "Well 9" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 2, 3 of 3 Inj" :rate 0.777}
   {:name "Well 10" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 2,3  of 4 Inj" :rate 0.555}
   {:name "Well 11" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1, 2 of 3 Inj" :rate 0.788}
   {:name "Well 12" :date "11/03/2015 8:00AM" :status 0 :desciption "Valves 1, 2 of 3 Inj" :rate 0.345}])

(def welltest-table
  {
   :colHeaders ["Date" "Oil Rate" "Water Rate" "Form. Gas" "LG Rate" "Prod. Press" "Inj. Press" "Choke" "Sep. Press"]
   :columnSorting true
   :columnHeaderHeight 35
   :rowHeights 25
   :width 800
   :manualColumnResize true
   :data
   [
    {:date "12-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
    {:date "13-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
    {:date "14-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
    {:date "15-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
    {:date "16-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
    {:date "17-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}]
   :columns
   [
    {:data "date" :type "date" :renderer "html"}
    {:data "oil-rate" :type "numeric" :renderer "html"}
    {:data "water-rate" :type "numeric"  :renderer "html"}
    {:data "form-gas" :renderer "html"}
    {:data "lg-rate" :renderer "html"}
    {:data "prod-press" :renderer "html"}
    {:data "inj-press" :renderer "html"}
    {:data "choke" :renderer "html"}
    {:data "sep-press" :renderer "html"}]

   :editor false
   :rowHeaders  false
   :contextMenu false
   :autoWrapRow true})

(def welltest-data
  [
     {:date "12-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "13-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "14-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "15-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "16-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "12-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "13-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "14-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "15-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "16-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "12-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "13-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "14-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "15-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "16-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}
     {:date "17-Oct-1994 00:00:00" :oil-rate 19.08  :water-rate 94.48  :form-gas 2265.35  :lg-rate 14158.4  :prod-press 689  :inj-press 5378  :choke 25.4  :sep-press 689}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
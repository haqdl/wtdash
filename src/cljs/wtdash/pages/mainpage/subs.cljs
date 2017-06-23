(ns wtdash.pages.mainpage.subs
  (:require [wtdash.data.db :as mydb]
            [wtdash.utils.common :as com-utils]
            [wtdash.data.subs :as datasubs])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))

(defn cal-glstatus [inwell]
  (let [valve-map (datasubs/get-valve-map-of-well inwell)
        status-list (mapv #(com-utils/decode-valve-status %) (:status-list valve-map))
        islastopen (= :open (last status-list))
        listopen (filterv #(= :open %) status-list)
        Nopen (count listopen)]
    ;(.log js/console (str "status-list: " status-list))
    ;(.log js/console (str "islastopen: " islastopen))
    ;(.log js/console (str "listopen: " listopen))
    (cond
      (and (= true islastopen)
           (= 1 Nopen)) (assoc inwell :glstatus 2)
      (and (= true islastopen)
           (> Nopen 1)) (assoc inwell :glstatus 1)
      :else (assoc inwell :glstatus 0))))

(defn get-all-well-status
  []
  (let [in-welllist (get-in @mydb/well-state [:all-well])
        out-welllist (map #(cal-glstatus %) in-welllist)]
    out-welllist))


(defn get-datasources
  []
  (get-in @mydb/well-state [:all-dsn]))

(defn get-selected-datasource
  []
  (get-in @mydb/well-state [:current-dsn]))

(defn get-all-well
  []
  (get-in @mydb/well-state [:all-well]))

(defn get-selected-well
  []
  (get-in @mydb/well-state [:current-well]))

(defn get-is-open-wellselector
  []
  (get-in @mydb/local-app-state [:open-well-selector]))

(defn get-well-doc
  []
  (get-in @mydb/well-state [:welldoc]))

(defn get-dvsp-config
  []
  (get-in @mydb/local-app-state [:chart/by-id :dvsp]))

(defn get-pvsq-config
  []
  (get-in @mydb/local-app-state [:chart/by-id :pvsq]))

(defn get-qvsi-config
  []
  (get-in @mydb/local-app-state [:chart/by-id :qvsi]))

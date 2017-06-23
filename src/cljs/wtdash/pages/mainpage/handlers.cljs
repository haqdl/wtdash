(ns wtdash.pages.mainpage.handlers
  (:require [wtdash.senteclient :as se]
            [wtdash.data.db :as mydb]))

(defn set-selected-well [well]
  (let [rawwell well
        cleanwell (dissoc rawwell :glstatus)
        welldata (->> @mydb/field-state
                      (:wells)
                      (filter #(= cleanwell (:well %))))
        welldoc (:welldoc (first welldata))]
    (swap! mydb/well-state assoc :current-well cleanwell)
    ;(if (some? welldoc)
    ;  (swap! mydb/well-state assoc :welldoc welldoc))))
    (se/sendAction :pickwell cleanwell)))

(defn set-selected-datasource [dsn]

  (se/sendAction :pickdsn dsn))

;(defn set-selected-well [well]
;  (swap! mydb/well-state assoc :current-well well)
;  (se/sendAction :pickwell well))

(defn set-open-well-selector [in]
  (swap! mydb/local-app-state assoc :open-well-selector in))

(def default-options
  {:nav-bar true
   :left-sidebar true
   :right-sidebar true
   :footer true
   :left-sidebar-on? true})

(defn set-main-page-option []
  (swap! mydb/local-app-state assoc-in [:pages :mainpage :options] default-options))

(defn set-main-page-content [content]
  (swap! mydb/local-app-state assoc-in [:pages :mainpage :content] content))

(defn toggle-left-sidebar []
  (.log js/console "Toggling")
  (let [on? (get-in @mydb/local-app-state [:pages :mainpage :options :left-sidebar-on?])]
    (if (= on? true)
      (swap! mydb/local-app-state assoc-in [:pages :mainpage :options :left-sidebar-on?] false)
      (swap! mydb/local-app-state assoc-in [:pages :mainpage :options :left-sidebar-on?] true))))

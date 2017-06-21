(ns wtdash.data.util
  (:require-macros [hiccups.core :as hiccups :refer [html]]))


(defn render-gl-status [data type row]
  (cond
    (= 0 data) (html
                 [:img {:src "images/g.png"
                        :style {:height "100%" :width "100%"}}])
    (= 1 data) (html
                 [:img {:src "images/y.png"
                        :style {:height "100%" :width "100%"}}])
    (= 2 data) (html
                 [:img {:src "images/r.png"
                        :style {:height "100%" :width "100%"}}])))

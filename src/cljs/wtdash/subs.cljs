(ns wtdash.subs
  (:require [wtdash.data.db :as mydb]))

(defn get-window-focus []
  (get-in @mydb/well-state [:window :focus]))

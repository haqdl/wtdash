(ns wtdash.data.subs
  (:require [wtdash.data.db :as mydb]))

(defn get-welltest []
  (get-in @mydb/well-state [:welldoc :welltest-map]))

(defn get-welltest-hist []
  (get-in @mydb/well-state [:welldoc :welltest-hist-map]))

(defn get-depth-profile []
  (get-in @mydb/well-state [:welldoc :depth-profile-map]))

(defn get-depth-profile-of-well [well]
  (let [welldata (->> @mydb/field-state
                      (:wells)
                      (filter #(= well (:well %))))
        welldoc (:welldoc (first welldata))
        depth-profile (:depth-profile-map welldoc)]
    depth-profile))

(defn get-valve-map-of-well [well]
  (let [welldata (->> @mydb/field-state
                      (:wells)
                      (filter #(= well (:well %))))
        welldoc (:welldoc (first welldata))
        depth-profile (:depth-profile-map welldoc)
        valve-map (:valves-status-map depth-profile)]
    valve-map))

(defn get-equilibrium-profile []
  (get-in @mydb/well-state [:welldoc :equilibrium-map]))

(defn get-outflow-curve []
  (get-in @mydb/well-state [:welldoc :outflow-map]))

(defn get-inflow-curve []
  (get-in @mydb/well-state [:welldoc :ipr-curve-map]))

(defn get-mandrel-survey []
  (get-in @mydb/well-state [:welldoc :mandrel-survey-map]))

(defn get-stored-lgas-response []
  (get-in @mydb/well-state [:welldoc :stored-lgas-response-map]))

(defn get-calced-lgas-response []
  (get-in @mydb/well-state [:welldoc :calced-lgr-curves-map]))

(defn get-lgas-response []
  (get-in @mydb/well-state [:welldoc :lgr-curves-map]))

(defn get-cal-wt []
  (get-in @mydb/well-state [:welldoc :cal-wt]))

(defn get-uncal-wt []
  (get-in @mydb/well-state [:welldoc :uncal-wt]))



(ns wtdash.server
  (:require [wtdash.handler :refer [app wrapped-app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [wtdash.senteserver :as sys]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn -main [& args]
  (sys/ws-message-router)
  (let [port (Integer/parseInt (or (env :port) "8000"))]
    (println (format "Starting server on port: %d\n" port))
    ;(run-jetty wrapped-app {:port port :join? false})
    (run-server wrapped-app {:port port :join? false})))

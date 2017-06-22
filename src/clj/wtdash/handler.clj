(ns wtdash.handler
  (:use     [ring.util.response :only [redirect response resource-response]]
            [ring.middleware.transit :only
             [wrap-transit-response wrap-transit-body]])
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [compojure.handler :as handler]
            [hiccup.page :refer [include-js include-css html5]]
            [wtdash.middleware :refer [wrap-middleware]]
            ;[ring.middleware.cors :refer [wrap-cors]]
            [config.core :refer [env]]
            [wtdash.oauth :as oauth]
            [wtdash.senteserver :as sys]
            [ring.middleware.reload :refer [wrap-reload]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))


(defroutes routes

  (GET "/" []
    (redirect (:uri oauth/auth-request)))
  (GET "/google-oauth" []
    (redirect (:uri oauth/auth-request)))
  (GET "/oauth2callback" []
       ;(loading-page))
       ;(println oauth/verifed-appsmiths)
    oauth/verifed-appsmiths)
  (GET  "/chsk" req (sys/ring-ws-handoff req))
  (POST "/chsk" req (sys/ring-ws-post req))
  (POST "/login" req (sys/login-handler req))
  ;(GET "/" [] (loading-page))
  ;(GET "/about" [] (loading-page))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))

(def wrapped-app
  (-> app
      (wrap-reload)
      ;(wrap-cors :access-control-allow-origin [#".*"]
      ;           :access-control-allow-methods [:get :put :post :delete])
      ;(wrap-transit-response {:encoding :json, :opts {}})
      (wrap-transit-body)
      ;(wrap-exceptions)
      (handler/site)))
      ;(wrap-request-logging)))

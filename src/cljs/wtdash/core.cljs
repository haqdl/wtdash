(ns wtdash.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [wtdash.pages.mainpage.core :refer [main-content]])
    (:require-macros [hiccups.core :as hiccups :refer [html]]))

;; -------------------------

;;--------admin-lte----------------------------
(defn- logo []
  (fn []
    [:a.logo {:href "#"}
     [:span.logo-mini "WT"]
     [:span.logo-lg "WT Dashboard"]]))

(defn- nav-bar []
  (fn []
    [:nav.navbar.navbar-static-top
     [:div.navbar-custom-menu
      [:ul.nav.navbar-nav]]]))

(defn main-header []
  [:header.main-header
   [logo]
   [nav-bar]])
;; Views

(defn home-page []
  [:div [:h2 "Welcome to wtdash"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About wtdash"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn main-page []
  [:div
     [main-header]
     [main-content]])

;; Routes

(def page (atom #'main-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'main-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))


;; -------------------------

(defn mount-root []
  (reagent/render [main-page] (.getElementById js/document "app")))

(defn ^:export init []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (print "init website")
  (mount-root))

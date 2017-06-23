(ns wtdash.core
    (:require [reagent.core :as reagent]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [wtdash.pages.mainpage.core :refer [main-content]]
              [wtdash.handlers :as handles]
              [wtdash.router.router :as router])
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

(defn main-page []
  [:div
   [main-header]
   [main-content]])

(comment
  (defn home-page []
    [:div [:h2 "Welcome to wtdash"]
     [:div [:a {:href "/about"} "go to about page"]]])

  (defn about-page []
    [:div [:h2 "About wtdash"]
     [:div [:a {:href "/"} "go to the home page"]]])

  ;; Routes

  (def page (reagent/atom #'main-page))

  (defn current-page []
    [:div [@page]])

  (secretary/defroute "/" []
    (reset! page #'main-page))

  (secretary/defroute "/about" []
    (reset! page #'about-page)))


;; -------------------------

(defn mount-root []
  (reagent/render [main-page] (.getElementById js/document "app")))

(defn ^:export init []
  ;(accountant/configure-navigation!
  ;  {:nav-handler
  ;   (fn [path]
  ;     (secretary/dispatch! path))
  ;   :path-exists?
  ;   (fn [path]
  ;     (secretary/locate-route path))})
  ;(accountant/dispatch-current!)
  (router/init-routes)
  (handles/init-states)
  (mount-root))

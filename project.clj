(defproject wtdash "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring-server "0.4.0"]
                 [reagent "0.6.2"]
                 [reagent-utils "0.2.1"]
                 [ring "1.6.1"]
                 [ring/ring-defaults "0.3.0"]
                 [ring-transit "0.1.6" :exclusions [commons-codec]]
                 [ring/ring-json "0.3.1"]
                 [ring-cors "0.1.8"]
                 ;[ring/ring-core "1.5.1"]
                 [compojure "1.6.0"]
                 [cljsjs/highcharts "5.0.4-0"]
                 ;[hiccup "1.0.5"]
                 [hiccups "0.3.0"]
                 [yogthos/config "0.8"]
                 [org.clojure/clojurescript "1.9.562"
                  :scope "provided"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.0"
                  :exclusions [org.clojure/tools.reader]]
                 [stuarth/clj-oauth2 "0.3.2"]
                 [cheshire "5.7.1"]
                 [clj-http "0.6.1" :exclusions [common-io]]
                 [durable-atom "0.0.3"]
                 [com.taoensso/sente "1.11.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [http-kit "2.2.0"]
                 [cljsjs/d3 "3.5.16-0"]]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.5"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler wtdash.handler/wrap-app
         :uberwar-name "wtdash.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "wtdash.jar"

  :main wtdash.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"
    "resources/public/js/compiled/site.min.js"
    ["src/cljs/js/external/adminlte.js"
     "src/cljs/js/external/datatables.min.js"
     "src/cljs/js/external/dataTables.select.min.js"
     "src/cljs/js/tubingpipe.js"]}}


  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "env/prod/cljs"]
             :compiler
             {:output-to "target/cljsbuild/public/js/compiled/app.js"
              :output-dir "target/uberjar"
              :externs ["src/cljs/js/externs/datatables.ext.js"
                        "src/cljs/js/externs/adminlte.ext.js"
                        "src/cljs/js/highchart.ext.js"
                        "src/cljs/js/externs/tubingpipe.ext.js"]
              :optimizations :advanced
              :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "env/dev/cljs"]
             :figwheel {:on-jsload "wtdash.core/mount-root"}
             :compiler
             {:main "wtdash.dev"
              :asset-path "/js/out"
              :output-to "resources/public/js/compiled/app.js"
              :output-dir "resources/public/js/out"
              :externs ["src/cljs/js/externs/datatables.ext.js"
                        "src/cljs/js/externs/adminlte.ext.js"
                        "src/cljs/js/highchart.ext.js"
                        "src/cljs/js/externs/tubingpipe.ext.js"]
              :source-map true
              :optimizations :none
              :pretty-print  true}}}}


  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]

   :css-dirs ["resources/public/css"]
   :ring-handler wtdash.handler/wrap-app}

  :profiles {:dev {:repl-options {:init-ns wtdash.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.6.1"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar "0.5.10"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.8.2"]]


                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.10"]]


                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {
                       :hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj" "env/prod/cljs"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "app"] ["minify-assets"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}}

  :aliases {"build-dev" ["do" ["minify-assets"] ["cljsbuild" "once" "app"]]
            "build-min" ["do" ["minify-assets"] ["cljsbuild" "once" "min"]]
            "run-dev"   ["do" ["run" "dev"]]
            "start-dev" ["build-dev" "run-dev"]})
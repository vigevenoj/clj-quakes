(defproject clj-quakes "0.1.0-SNAPSHOT"
  :description "Earthquakes and mqtt"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.8.0"]
                 [clj-http "3.7.0"]
                 [clojurewerkz/machine_head "1.0.0"]
                 [clojurewerkz/quartzite "2.0.0"]
                 [com.climate/geojson-schema "0.2.1"]
                 [http-kit "2.2.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.465"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.julienxx/clj-slack "0.5.5"]]
  :main ^:skip-aot clj-quakes.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

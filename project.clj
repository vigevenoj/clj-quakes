(defproject clj-quakes "0.1.0-SNAPSHOT"
  :description "Earthquakes and mqtt"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.8.1"]
                 [clj-http "3.9.1"]
                 [clojurewerkz/machine_head "1.0.0"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [com.climate/geojson-schema "0.2.1"]
                 [http-kit "2.3.0"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.julienxx/clj-slack "0.6.2"]
                 [io.riemann/riemann-java-client "0.5.0"]
                 [riemann-clojure-client "0.5.0"]]
  :main ^:skip-aot clj-quakes.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

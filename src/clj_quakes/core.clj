(ns clj-quakes.core
  (:gen-class)
  (:require [clj-quakes.quakes :as quakes]
            [clj-http.client :as client]
            [clojure.java.io :refer [resource file]] ;; for testing
            [clojure.core.async :as async  :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [com.climate.geojson-schema.core :refer [FeatureCollection GeoJSON]]
            [clojurewerkz.machine-head.client :as mh]
            [schema.core :as s]))

(def ^:const owntracks-topic "owntracks/#")
(def ^:const broker-url "tcp://sharkbaitextraordinaire.com:8885") ;; figure out how to use a TLS-secured connection

(defn parse-owntracks
  "Parse an mqtt payload into a map with keys"
  [^bytes payload]
  (let [update
        (cheshire.core/parse-string (String. payload "UTF-8") true)]))

;; TODO use command line arguments or parse env/yaml to load configuration
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
;  (let [id   (mh/generate-id)
;        conn (mh/connect broker-url id)]
;    (mh/subscribe conn {owntracks-topic 0}
;                  (fn [^String topic meta ^bytes payload]
;                    (parse-owntracks payload))))
  )


;; This outputs the distance from the test point (in km) for every earthquake present in the feed
;; needs improvement by also including the location and detail url
;; and next step is to determine if the elements are within a range to be considered interesting
;; (map distance-from-test (map :geometry (:features (fetch-quakes))))

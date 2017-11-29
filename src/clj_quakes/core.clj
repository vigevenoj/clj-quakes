(ns clj-quakes.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [clojure.java.io :refer [resource file]] ;; for testing
            [cheshire.core :refer :all]
            [com.climate.geojson-schema.core :refer [FeatureCollection GeoJSON]]
            [schema.core :as s]))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn test-feed []
  (s/validate com.climate.geojson-schema.core/FeatureCollection
  (cheshire.core/parse-string (slurp "/Users/vigevenoj/code/clojure/clj-quakes/hourly.json") true))
  )

;; use this as a test point for haversine and such
(def test-point
  {:type "Point"
   :coordinates [-122.58441925048828,
                 45.48420633926945]})

(defn get-coordinates [feature]
  "Get the coordinates of a feature"
  (:coordinates (:geometry feature))
  )

(defn get-longitude
  "Get the longitude from a GeoJSON Point"
  [point]
  (first (:coordinates point)))

(defn get-latitude
  "Get the latitude from a GeoJSON Point"
  [point]
  (first (rest (:coordinates point))))

(defn point-haversine
  "Haversine formula for two GeoJSON Point objects"
  [p1 p2]
  (let [R 6371 ; radius of arth in km
        delta-latitude (Math/toRadians (- (get-latitude p1) (get-latitude p2)))
        delta-longitude (Math/toRadians (- (get-longitude p1) (get-longitude p2)))
        latitude1 (Math/toRadians (get-latitude p1))
        latitude2 (Math/toRadians (get-latitude p2))
        a (+ (hav delta-latitude) (* (Math/cos latitude1) (Math/cos latitude2) (hav delta-longitude)))]
    (* R 2 (Math/atan2 (Math/sqrt a) (Math/sqrt (- 1 a)))))
  )

(defn- hav
  [val]
  (Math/pow(Math/sin(/ val 2)) 2))


(defn fetch-quakes []
  "Get the earthquake feed and parse it as a FeatureCollection. Return a "
  (let [response
        (cheshire.core/parse-string
         (:body (client/get "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson"))
         true )]
    (s/validate FeatureCollection response)
    )
  )
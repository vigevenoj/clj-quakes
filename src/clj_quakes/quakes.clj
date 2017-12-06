(ns clj-quakes.quakes
  (:require [clj-http.client :as client]
            [clojure.java.io :refer [resource file]] ;; for testing
            [clojure.tools.logging :as log]
            [com.climate.geojson-schema.core :refer [FeatureCollection GeoJSON]]
            [schema.core :as s]))

;; TODO extract to tests package
(defn test-feed []
  (s/validate com.climate.geojson-schema.core/FeatureCollection
              (cheshire.core/parse-string
                (slurp  "hourly.json") true)))

;; use this as a test point for haversine and such. It's in Portland.
;; TODO extract to tests package
(def test-point
  {:type        "Point"
   :coordinates [-122.58441925048828,
                 45.48420633926945]})

(defn get-coordinates [feature]
  "Get the coordinates of a feature"
  (:coordinates (:geometry feature)))

(defn get-longitude
  "Get the longitude from a GeoJSON Point"
  [point]
  (first (:coordinates point)))

(defn get-latitude
  "Get the latitude from a GeoJSON Point"
  [point]
  (first (rest (:coordinates point))))

(defn hav
  "Calculate a haversine, sin^2(θ/2)"
  [val]
  (Math/pow (Math/sin (/ val 2)) 2))

(defn point-haversine
  "Haversine formula to calculate great-circle distance between two GeoJSON Point objects"
  [p1 p2]
  (let [R               6371 ; radius of arth in km
        delta-latitude  (Math/toRadians (- (get-latitude p1) (get-latitude p2)))
        delta-longitude (Math/toRadians (- (get-longitude p1) (get-longitude p2)))
        latitude1       (Math/toRadians (get-latitude p1))
        latitude2       (Math/toRadians (get-latitude p2))
        a               (+ (hav delta-latitude)
                           (* (Math/cos latitude1) (Math/cos latitude2) (hav delta-longitude)))]
    (* R 2 (Math/atan2 (Math/sqrt a) (Math/sqrt (- 1 a))))))

(defn fetch []
  "Get the earthquake feed and parse it as a FeatureCollection"
  (let [response
        (cheshire.core/parse-string
         (:body
           (client/get "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson"))
         true)]
    (s/validate FeatureCollection response)))

;; TODO move this into test package?
(defn distance-from-test
  "Calculate how far a point is from the test point"
  [point]
  (point-haversine test-point point))

(defn newer?
  "Filter out quakes older than six minutes"
  [quakes]
  ;; should be true if newer than 1000*60*5
  (filter #(<
            (- (System/currentTimeMillis) (-> % :properties :time))
            (* 6 60 1000)) ; 6 minutes in milliseconds
          quakes))

;; this prints out the distance from the test point to each earthquake
;; we should use this for calculating if the earthquake is close enough to care about
(map clj-quakes.quakes/distance-from-test (->> (:features (clj-quakes.quakes/test-feed)) (map :geometry)))

;(defn millis-ago [timestamp] (- (System/currentTimeMillis) timestamp))
;(map millis-ago ((:features (clj-quakes.quakes/test-feed)) (map :properties) (map :time)))
; below: filter returns collection of points where points are less than 1000km from test-point
;(filter #(< (clj-quakes.quakes/distance-from-test %) 1000) (->> (:features (clj-quakes.quakes/test-feed)) (map :geometry)))
; below: filter returns collection of quakes (maps) where points are less than 1000km from test-point
;(filter #(< (clj-quakes.quakes/distance-from-test (:geometry %)) 1000) (:features (clj-quakes.quakes/test-feed)) )
(defn nearness-filter
  "filter for quakes closer than a specified distance"
  [quakes location distance]
  (filter #(<
            (clj-quakes.quakes/point-haversine location (:geometry %))
            distance)
          quakes ))

(defn magnitude-filter
  "filter for quakes bigger than a given magnitude"
  [quakes] [mag]
  (filter #(>
            (-> % :properties :mag)
            mag)
          quakes))

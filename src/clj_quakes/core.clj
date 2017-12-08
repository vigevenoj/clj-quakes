(ns clj-quakes.core
  (:gen-class)
  (:require [clj-quakes.quakes :as quakes]
            [clj-http.client :as client]
            [clj-slack.auth]
            [clj-slack.channels]
            [clj-slack.chat]
            [clj-slack.users]
            [clojure.java.io :refer [resource file]] ;; for testing
            [clojure.core.async :as async  :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.cron :refer [schedule cron-schedule]]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [cheshire.core :refer :all]
            [com.climate.geojson-schema.core :refer [FeatureCollection GeoJSON]]
            [clojurewerkz.machine-head.client :as mh]
            [schema.core :as s]))

(def config (edn/read-string(slurp "config-local.edn")))
(def connection {:api-url "https://slack.com/api" :token (-> config :slack :token)})

(def hood
  {:type        "Point"
   :coordinates [-121.695728,
                 45.37476]})
(def helens
  {:type "Point"
   :coordinates [-122.189941,
                  46.197419]})

(def monitored-locations (list hood helens))

(defn get-channel
  "Look up a slack channel by its name"
  [name]
  (let [channels (-> (clj-slack.channels/list connection) :channels)]
    (first (filter #(= (:name %) name) channels))))

(defn handle-quake
  [quake]
  (let [channel (get-channel (-> config :slack :channel))
        title (-> quake :properties :title)
        url (-> quake :properties :url)]
    (clj-slack.chat/post-message connection (-> channel :id)
                                 (str "New earthquake " title ". More info at " url))))

(defn parse-owntracks
  "Parse an mqtt payload into a map with keys"
  [^bytes payload]
  (let [update
        ; instead of printing this, we should update a ref
        ; which we then merge into the monitored-locations vector
        (cheshire.core/parse-string (String. payload "UTF-8") true)]))

(defjob FetchJob
  [ctx]
  (comment "Fetch quakes, filter interesting/worrisome ones, and deal with them")
  (let [quakes (:features (quakes/fetch))] ; when done testing, add back (quakes/newer? (:features (quakes/fetch))
    (let [interesting-quakes (filter #(<
                (quakes/closest monitored-locations %)
                                       (-> config :usgs :interesting-distance-threshold)) ; define this as a constant somewhere: interesting-quake-distance-km
              quakes)]
      (map handle-quake interesting-quakes)))

  ; This expression returns quakes that occurred within the past 6 minutes and within 1000km:
  ;(quakes/nearness-filter (quakes/newer? (:features (quakes/fetch))) quakes/test-point 1000)
  ; we should also filter the quakes using a closer nearness-filter and a magnitude filter?
  ; or do like in the java version and just drop the fetched earthquakes onto a queue and analyze them later
  ; analysis side would pop quakes off queue, determine closest monitored location to quake,
  ; and then determine if it was 'interesting' or 'worrisome'
  ; then drop the interesting/worrisome quakes onto a queue for outbound notifications
  (println "fetch job is running")) ; we should log/warn this instead of println

;; TODO use command line arguments or parse env/yaml to load configuration
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")

  ;; using quartzite to run the fetch job evey five minutes
  (let [s (-> (qs/initialize) qs/start)
        job (j/build
              (j/of-type FetchJob)
              (j/with-identity (j/key "jobs.quakes.fetcher")))
        trigger (t/build
                 (t/with-identity (t/key "trigger.fetch.1"))
                 (t/start-now)
                 (t/with-schedule (schedule
                                   (cron-schedule "0 0/5 * * * ?	"))))]
    (qs/schedule s job trigger))

  (let [id   (mh/generate-id)
        conn (mh/connect (-> config :mqtt :url)id)]
    (mh/subscribe conn {(-> config :mqtt :topic) 0}
                  (fn [^String topic meta ^bytes payload]
                    (parse-owntracks payload))))
  )


;; This outputs the distance from the test point (in km) for every earthquake present in the feed
;; needs improvement by also including the location and detail url
;; and next step is to determine if the elements are within a range to be considered interesting
;; (map distance-from-test (map :geometry (:features (fetch))))
(ns clj-quakes.owntracks
 (:require [cheshire.core :refer :all]))

(defn parse-update [json]
  (let [point (cheshire.core/parse-string (String. json "UTF-8") true)]
    {:type "Point"
     :coordinates [(-> point :lon), (-> point :lat)]
  }))

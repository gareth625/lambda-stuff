(ns lambda-stuff.core
  (:require [cljs-lambda.util :refer [async-lambda-fn]]
            [eulalie.creds]
            [hildebrand.channeled :refer [query!]]
            [hildebrand.core :refer [put-item!]]
            [cljs.core.async :refer [take! <!]])
    (:require-macros [cljs.core.async.macros :refer [go]]))

(def exercise->type
  {"barbell-deadlift" :weighted
   "sumo-deadlift" :weighted})

(def exercise-type->schema
  {:weighted #{:exercise :date-hour :weight-kg :reps}})

(defn exercise->schema
  [exercise]
  (exercise-type->schema (exercise->type exercise)))

(defn build-item
  [schema event]
  (into {} (map (fn [k] (hash-map k (event k))) schema)))

(defn read-all-from-query
  [query]
  (go
    (loop [item (<! query)
           items []]
      (println item "," items)
      (if item
        (recur (<! query) (conj items item))
        items))))

(def ^:export add-exercise
  (let [table :exercise]
    (async-lambda-fn
      (fn [{:keys [exercise date-hour] :as event} context]
        (go
          (let [creds (assoc (eulalie.creds/env) :region "eu-west-1")]
            (if (and exercise date-hour)
              (<! (put-item! creds table (build-item (exercise->schema exercise) event)))
              (js/Error (str "Sorry, you must specify an exercise and date-hour (time of exercise to the hour). Was given exercise: '" + exercise + "' and date-hour: '" + date-hour + "'.")))))))))

(def ^:export get-exercise
  (let [table :exercise]
    (async-lambda-fn
      (fn [{:keys [exercise]} context]
        (go
          (let [creds (assoc (eulalie.creds/env) :region "eu-west-1")]
            (if exercise
              (<! (read-all-from-query (query! creds table {:exercise [:= exercise]} {:limit 10})))
              (js/Error "No exercise was specified."))))))))
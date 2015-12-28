(ns lambda-stuff.core
  (:require [cljs-lambda.util :refer [async-lambda-fn]]
            [eulalie.creds]
            [hildebrand.channeled :refer [query!]]
            [hildebrand.core :refer [create-table! put-item! table-status!]]
            [cljs.core.async :refer [take! <!]])
    (:require-macros [cljs.core.async.macros :refer [go]]))

(def workout-schema
  "The required fields when submitting a workout.

  Data shouldn't be in code. Makes updating tedious ;)"
  #{:type :date-hour :workout-id :title :feelings :notes})

(def exercise->type
  {"barbell-deadlift" :weighted
   "sumo-deadlift" :weighted})

(def exercise-type->schema
  {:weighted #{:exercise :date-hour :workout-id :weight-kg :reps}})

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

(let [creds (assoc (eulalie.creds/env) :region "eu-west-1")
      exercise-table :exercise
      workout-table :workout]
    (def ^{:export true
           :doc "Creates the DynamoDB table (for now hardcoded name) to store workouts.

                If the table exists then no change is made. A workout is a
                collection of exercises and this table stores the workout type,
                date and any notes on the workout itself but not the individual
                exercises."}
         create-workout-table
      (async-lambda-fn
        (fn []
          (go
            (when-not (<! (table-status! creds workout-table))
              (<! (create-table! creds
                                 {:table workout-table
                                  :throughput {:read 1 :write 1}
                                  :attrs {:type :string
                                          :date-hour :string
                                          :workout-id :string}
                                  :keys  [:type :date-hour]
                                  :indexes {:local [{:name :workout-id-by-type
                                                     :keys [:type :workout-id]
                                                     :project [:keys-only]}]}})))))))

    (def ^{:export true
           :doc "Create the DynamoDB table (for now hardcoded name) to store exercises associated with a workout.

                If the table exists then no changes are made. An exercise is
                within a workout and there can be multiple exercises in a
                workout."}
         create-exercise-table
      (async-lambda-fn
        (fn []
          (go
            (when-not (<! (table-status! creds exercise-table))
              (<! (create-table! creds
                                 {:table exercise-table
                                  :throughput {:read 1 :write 1}
                                  :attrs {:exercise :string :date-hour :string :workout-id :string}
                                  :keys [:exercise :date-hour]
                                  :indexes {:local [{:name :workout-id-by-exericse
                                                     :keys [:exercise :workout-id]
                                                     :project [:keys-only]}]}})))))))
)

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
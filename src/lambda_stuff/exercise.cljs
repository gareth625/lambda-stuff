(ns lambda-stuff.exercise
  (:require [cljs.core.async :refer [take! <!]]
            [cljs-lambda.util :refer [async-lambda-fn]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [eulalie.creds]
            [hildebrand.channeled :refer [query!]]
            [hildebrand.core :refer [create-table! put-item! table-status!]]
            [schema.core :as s :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]))

; (def exercise->type
;   {"barbell-deadlift" :weighted
;   "sumo-deadlift" :weighted})

; (def exercise-type->schema
;   {:weighted #{:exercise :date-hour :workout-id :weight-kg :reps}})

; (defn exercise->schema
;   [exercise]
;   (exercise-type->schema (exercise->type exercise)))

; (defn build-item
;   [schema event]
;   (into {} (map (fn [k] (hash-map k (event k))) schema)))

; (defn read-all-from-query
;   [query]
;   (go
;     (loop [item (<! query)
;           items []]
;       (println item "," items)
;       (if item
;         (recur (<! query) (conj items item))
;         items))))

; (defn create-exercise-table
;   "Create the DynamoDB table to store exercises associated with a workout.

;   If the table exists then no changes are made. An exercise is within a workout
;   and there can be multiple exercises in a workout."
;   [creds table-name]
;   (go
;     (when-not (<! (table-status! creds table-name))
;       (<! (create-table! creds
;                         {:table table-name
;                           :throughput {:read 1 :write 1}
;                           :attrs {:exercise :string :date-hour :string :workout-id :string}
;                           :keys [:exercise :date-hour]
;                           :indexes {:local [{:name :workout-id-by-exericse
;                                             :keys [:exercise :workout-id]
;                                             :project [:keys-only]}]}})))))

; (s/defschema ClientExercise s/Any)
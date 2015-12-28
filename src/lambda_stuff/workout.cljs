(ns lambda-stuff.workout
  (:require [cljs.core.async :refer [take! <!]]
            [cljs-lambda.util :refer [async-lambda-fn]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [eulalie.creds]
            [hildebrand.channeled :refer [query!]]
            [hildebrand.core :refer [create-table! put-item! table-status!]]
            [schema.core :as s :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def workout-schema
  "The required fields when submitting a workout.

  Data shouldn't be in code. Makes updating tedious ;)"
  {:type s/Str
   :date-hour s/Str
   :workout-id s/Str
   :title s/Str
   :feelings s/Str
   :notes s/Str})

(defn create-workout-table
  "Creates the DynamoDB table to store workouts.

  If the table exists then no change is made. A workout is a collection of
  exercises and this table stores the workout type, date and any notes on the
  workout itself but not the individual exercises."
  [creds table-name]
  (go
    (when-not (<! (table-status! creds table-name))
      (<! (create-table! creds
                         {:table table-name
                          :throughput {:read 1 :write 1}
                          :attrs {:type :string
                                  :date-hour :string
                                  :workout-id :string}
                          :keys  [:type :date-hour]
                          :indexes {:local [{:name :workout-id-by-type
                                             :keys [:type :workout-id]
                                             :project [:keys-only]}]}})))))
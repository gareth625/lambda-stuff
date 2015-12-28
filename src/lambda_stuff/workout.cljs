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

(defn create-workout-table!
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

(def workout-schema
  "The required fields when submitting a workout.

  Data shouldn't be in code. Makes updating tedious ;)"
  {:workout s/Str
   :date-hour s/Str
   :title s/Str
   :feelings s/Str
   :notes s/Str})

; (s/defn add-workout!
;   ""
;   [creds :- {:access-key s/Str :secret-key s/Str}
;   table :- s/Keyword
;   {:keys {workout date-hour} :as event} :- workout-schema
;   context]
(defn add-workout!
  ""
  [creds table {:keys {workout date-hour} :as event} context]
  (go
    (if (and workout date-hour)
      (<! (put-item! creds table event)))
      (js/Error (str "Sorry, you must specify a workout and date-hour (time of the workout to the hour). Was given workout: '"
                     workout
                     "' and date-hour: '"
                     date-hour
                     "'."))))
(ns lambda-stuff.workout
  (:require [cljs.core.async :as async :refer [<!]]
            [cljs-lambda.util :refer [async-lambda-fn]]
            [cljs-time.core :as t]
            [cljs-time.format :as f]
            [eulalie.creds]
            [hildebrand.channeled :refer [query!]]
            [hildebrand.core :refer [create-table! put-item! table-status!]]
            [lambda-stuff.schema :refer [AwsCredentials Context DbWorkout UserWorkout Workout WorkoutSchemaVersion]]
            [schema.core :as s :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]))
            ;;- [lambda-stuff.exercise :as exercise]

(s/def db-workout-schema-version :- WorkoutSchemaVersion 1)

(s/defn ^:always-validate create-workout-table
  "Creates the DynamoDB table to store workouts.

   If the table exists then no change is made. A workout is a collection of
   exercises and this table stores the workout type, date and any notes on the
   workout itself but not the individual exercises."
   [creds :- AwsCredentials
    table-name :- s/Keyword
    context :- Context]
   (go
     (when-not (<! (table-status! creds table-name))
       (<! (create-table! creds
                          {:table table-name
                           :throughput {:read 1 :write 1}
                           :attrs {:workout :string
                                   :date-hour :string
                                   :workout-id :string}
                           :keys  [:workout :date-hour]
                           :indexes {:local [{:name :workout-id-by-workout
                                              :keys [:workout :workout-id]
                                              :project [:keys-only]}]}})))))

(s/defn user-workout->id :- s/Str
   "Given an input user workout it determines an ID that can be used in the DB workout."
   [{:keys [workout date-hour] :as workout :- UserWorkout}]
    (hash (str workout "-" date-hour)))

(s/defn user->db-workout :- DbWorkout
   "Converts from the user supplied workout to the one serialised in the DB."
   [user-workout :- UserWorkout]
    (merge {:id (user-workout->id user-workout)
            :version db-workout-schema-version}
           user-workout))

(s/defn ^:always-validate add-workout
  "Stores a user workout."
  [creds :- AwsCredentials
   table :- s/Keyword
   {:keys [workout date-hour] :as user-workout} :- UserWorkout
   context :- Context]
   (go (<! (put-item! creds table (user->db-workout user-workout)))))

(s/defn ^:always-validate get-workouts
  "Retrieves all the workouts for a given workout type.

  Can optionally filter the returned workouts."
  [creds :- AwsCredentials
   table :- s/Keyword
   {:keys [workout date-part] :as event} :- {:workout Workout :date-part s/Str}
   context :- Context]
  (async/into [] (query! creds
                         table
                         {:workout [:= workout]
                          :date-hour [:begins-with date-part]}
                         {:limit 100})))

(s/defn ^:always-validate get-workout :- [UserWorkout]
  "Retrieves a workout either by it's ID or type and log time."
  [creds :- AwsCredentials
   table :- s/Keyword
   event :- s/Any
   context :- Context]
  nil)

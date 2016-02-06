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
            ;;- [lambda-stuff.exercise :as exercise]

(let [dh-formatter (f/formatters :date-hour)]
  (s/defn date-hour? :- s/Bool
    "Returns true if we can construct a date time object with the date and hour set from the input string."
    [s :- s/Str]
    (not (nil? (f/parse dh-formatter s)))))

(s/defschema AwsCredentials
  "Expected credentials map form."
  {:access-key s/Str
   :secret-key s/Str
   (s/optional-key :token) s/Str
   (s/optional-key :region) s/Str})

(s/defschema Workout
  "Allowed keys for the workout field."
  (s/enum :field :playground :gym :run :swim :cycle :multisport))

(s/defschema UserWorkout
  "This is workout schema that user will create and typically is seen."
  {:workout Workout
   :date-hour (s/pred date-hour?)
   :title s/Str
   (s/optional-key :feelings) (s/maybe s/Str)
   (s/optional-key :notes) (s/maybe s/Str)
   (s/optional-key :exercises) [s/Any]}) ;; exercise/ClientExercise]})

(s/defschema WorkoutSchemaVersion
  "Version scheme for the workouts."
  s/Int)

(s/defschema DbWorkout
  "The schema for workouts as they are stored in the DB."
  {:id s/Str
   :version WorkoutSchemaVersion
   :workout Workout
   :date-hour (s/pred date-hour?)
   :title s/Str
   (s/optional-key :feelings) (s/maybe s/Str)
   (s/optional-key :notes) (s/maybe s/Str)})

(s/def db-workout-schema-version :- WorkoutSchemaVersion 1)

(s/defn ^:always-validate create-workout-table
  "Creates the DynamoDB table to store workouts.

   If the table exists then no change is made. A workout is a collection of
   exercises and this table stores the workout type, date and any notes on the
   workout itself but not the individual exercises."
   [creds :- AwsCredentials
    table-name :- s/Keyword]
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
  ""
  [creds :- AwsCredentials
   table :- s/Keyword
   {:keys [workout date-hour] :as user-workout} :- UserWorkout
    context :- s/Any]
   (go
     (if (and workout date-hour)
       (<! (put-item! creds table (user->db-workout user-workout)))
       (js/Error (str "Sorry, you must specify a workout and date-hour (time of the workout to the hour). Was given workout: '"
                      workout
                      "' and date-hour: '"
                      date-hour
                      "'.")))))
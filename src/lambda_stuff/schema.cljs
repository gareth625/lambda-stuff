(ns lambda-stuff.schema
  (:require [cljs-time.format :as f]
            [schema.core :as s :include-macros true]))

(let [dh-formatter (f/formatters :date-hour)]
  (s/defn date-hour? :- s/Bool
    "Returns true if we can construct a date time object with the date and hour set from the input string."
    [s :- s/Str]
    (not (nil? (f/parse dh-formatter s)))))

;; Not sure why I can't use these for the credentials schema but it doesn't
;; seem to like the s/constrained. Works in a Clojure REPL.
;; (defn required-length?
;;   [l s]
;;   (= (count s) l))

;; (defn required-length-secret-key?
;;   [s]
;;   (required-length? 40 s))

;; (s/constrained s/Str (partial required-length? 20) 'required-length-access-key?)
;; (s/constrained s/Str (partial required-length? 40) 'required-length-secret-key?)
(s/defschema AwsCredentials
  "Expected credentials map form."
  {:access-key s/Str
   :secret-key s/Str
   (s/optional-key :token) s/Str
   (s/optional-key :region) s/Str})

(s/defschema Workout
  "Allowed keys for the workout field."
  (s/enum "field" "playground" "gym" "run" "swim" "cycle" "multisport"))

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
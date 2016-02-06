(ns lambda-stuff.core
  (:require ;;- [lambda-stuff.exercise :as exercise]
            [lambda-stuff.workout :as workout]
            [cljs-lambda.util :refer [async-lambda-fn]]
            [eulalie.creds]))

(let [creds (assoc (eulalie.creds/env) :region "eu-west-1")
      workout-table :workout
      exercise-table :exercise]
    (def ^{:export true
           :doc "Creates the DynamoDB table (for now hardcoded name) to store workouts."}
         create-workout-table
      (async-lambda-fn
        (fn [] (workout/create-workout-table creds workout-table))))

    (def ^{:export true
           :doc "Create the DynamoDB table (for now hardcoded name) to store exercises associated with a workout."}
         create-exercise-table
      (async-lambda-fn
        (fn [] nil))) ;;- (exercise/create-exercise-table creds exercise-table))))

    (def ^{:export true
           :doc "Adds a new workout and exercise list."}
         add-workout
      (async-lambda-fn
        (fn [event context]
          (workout/add-workout creds workout-table event context))))
)

; (def ^:export add-exercise
;   (let [table :exercise]
;     (async-lambda-fn
;       (fn [{:keys [exercise date-hour] :as event} context]
;         (go
;           (let [creds (assoc (eulalie.creds/env) :region "eu-west-1")]
;             (if (and exercise date-hour)
;               (<! (put-item! creds table (build-item (exercise->schema exercise) event)))
;               (js/Error (str "Sorry, you must specify an exercise and date-hour (time of exercise to the hour). Was given exercise: '" + exercise + "' and date-hour: '" + date-hour + "'.")))))))))

; (def ^:export get-exercise
;   (let [table :exercise]
;     (async-lambda-fn
;       (fn [{:keys [exercise]} context]
;         (go
;           (let [creds (assoc (eulalie.creds/env) :region "eu-west-1")]
;             (if exercise
;               (<! (read-all-from-query (query! creds table {:exercise [:= exercise]} {:limit 10})))
;               (js/Error "No exercise was specified."))))))))
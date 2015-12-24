(defproject lambda-stuff "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "http://please.FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [org.clojure/core.async "0.2.371"]
                 [io.nervous/cljs-lambda "0.1.2"]
                 [io.nervous/eulalie "0.6.4"]
                 [io.nervous/hildebrand "0.4.3"]
                 ]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-npm "0.5.0"]
            [io.nervous/lein-cljs-lambda "0.2.4"]]
  :node-dependencies [[source-map-support "0.2.8"]]
  :source-paths ["src"]
  :cljs-lambda
  {:defaults {:role "arn:aws:iam::141786013431:role/lambda_role"}
   :functions
   [{:name   "add-exercise"
     :invoke lambda-stuff.core/add-exercise}
    {:name   "get-exercise"
     :invoke lambda-stuff.core/get-exercise}]}
  :cljsbuild
  {:builds [{:id "lambda-stuff"
             :source-paths ["src"]
             :compiler {:output-to "out/lambda_stuff.js"
                        :output-dir "out"
                        :target :nodejs
                        :optimizations :none
                        :source-map true}}]})

; (require 'cljs.repl)
; (require 'cljs.build.api)
; (require 'cljs.repl.node)

; (cljs.build.api/build "src"
;   {:main 'hello-world.core
;    :output-to "out/main.js"
;    :verbose true})

; (cljs.repl/repl (cljs.repl.node/repl-env)
;  :watch "src"
;  :output-dir "out")

(require '[cljs.repl.node :as node]
         '[cemerick.piggieback :as piggieback])

(piggieback/cljs-repl
 (node/repl-env)
 :output-dir "out"
 :optimizations :none
 :cache-analysis true
 :source-map true)
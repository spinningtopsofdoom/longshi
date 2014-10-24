(ns perf.monitor
  (:require [longshi.core :as fress]
            [sample.perf-data :as pd]))


(def sample pd/sample)
(enable-console-print!)


(defn gen-sample []
  (println "Fressian writing speed")
  (.profile js/console)
  (simple-benchmark [x sample] (fress/write x) 100)
  (.profileEnd js/console)

  (println "Fressian reading speed")
  (.profile js/console)
  (simple-benchmark [x (fress/write sample)] (fress/read x) 100)
  (.profileEnd js/console))

(ns benchmark.test
  (:require [longshi.core :as fress]
            [sample.perf-data :as pd]
            [cljs.reader :as reader]
            [cognitect.transit :as t]
            ))

(def sample pd/sample)

(enable-console-print!)

(println "EDN writing speed")
(simple-benchmark [x sample] (prn-str x) 100)

(println "EDN reading speed")
(simple-benchmark [x (prn-str sample)] (reader/read-string x) 100)

(println "Tranist writing speed")
(def tw (t/writer :json))
(simple-benchmark [x sample] (t/write tw x) 100)

(println "Tranist reading speed")
(def tr (t/reader :json))
(simple-benchmark [x (t/write tw sample)] (t/read tr x) 100)

(println "Fressian writing speed")
(simple-benchmark [x sample] (fress/write x) 100)

(println "Fressian reading speed")
(simple-benchmark [x (fress/write sample)] (fress/read x) 100)

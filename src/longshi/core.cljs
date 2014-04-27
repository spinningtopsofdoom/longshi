(ns longshi.core
  (:import [goog.math Long])
  (:require [longshi.fressian.byte-stream-protocols :as bsp]
            [longshi.fressian.protocols :as p]
            [longshi.fressian.byte-stream :as bs]
            [longshi.fressian.js :as bjs]))
;;BYte Stream creation functions
(def byte-input-stream bs/byte-input-stream)
(def byte-output-stream bs/byte-output-stream)

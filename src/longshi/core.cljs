(ns longshi.core
  (:require [longshi.fressian.protocols :as p]
            [longshi.fressian.ByteStream :as bs]
            [longshi.fressian.basejs :as bjs]))

(enable-console-print!)

(println "Hello world!")

(let [bos (bs/byteOutputStream 2)]
  (p/writeNull! bos)
  (p/writeBoolean! bos true)
  (p/writeBoolean! bos false)
  (let [bis (bs/byteInputStream (p/getBytes bos))]
    (println
     [(p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      ])))
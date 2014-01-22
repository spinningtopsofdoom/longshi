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
  (p/writeDouble! bos 0.0)
  (p/writeDouble! bos 1.0)
  (p/writeDouble! bos 1234.56789)
  (let [bis (bs/byteInputStream (p/getBytes bos))]
    (println
     [(p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      ])))
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
  (p/writeInt! bos 2147483647)
  (p/writeInt! bos 8589934591)
  (p/writeInt! bos 2199023255551)
  (p/writeInt! bos 281474976710655)
  (p/writeInt! bos 9007199254740991)
  (p/writeInt! bos -2147483648)
  (p/writeInt! bos -8589934592)
  (p/writeInt! bos -2199023255552)
  (p/writeInt! bos -562949953421312)
  (p/writeInt! bos -9007199254740991)
  (let [bis (bs/byteInputStream (p/getBytes bos))]
    (println
     [(p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      (p/readObject! bis)
      ])))

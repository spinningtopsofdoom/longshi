(ns longshi.core
  (:require [longshi.fressian.protocols :as p]
            [longshi.fressian.byte-stream :as bs]
            [longshi.fressian.js :as bjs]))

(enable-console-print!)

(println "Hello world!")

(let [bos (bs/byte-output-stream 2)]
  (p/write-null! bos)
  (p/write-boolean! bos true)
  (p/write-boolean! bos false)
  (p/write-double! bos 0.0)
  (p/write-double! bos 1.0)
  (p/write-double! bos 1234.56789)
  (p/write-int! bos 2147483647)
  (p/write-int! bos 8589934591)
  (p/write-int! bos 2199023255551)
  (p/write-int! bos 281474976710655)
  (p/write-int! bos 9007199254740991)
  (p/write-int! bos -2147483648)
  (p/write-int! bos -8589934592)
  (p/write-int! bos -2199023255552)
  (p/write-int! bos -562949953421312)
  (p/write-int! bos -9007199254740991)
  (let [bis (bs/byte-input-stream (p/get-bytes bos))]
    (println
     [(p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      (p/read-object! bis)
      ])))

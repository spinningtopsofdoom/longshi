(ns examples.js
  (:import [goog.math Long])
  (:use-macros [longshi.macros :only [make-byte-array]])
  (:require [longshi.fressian.byte-stream-protocols :as bsp]
            [longshi.fressian.protocols :as p]
            [longshi.fressian.byte-stream :as bs]
            [longshi.fressian.js :as bjs]))

(enable-console-print!)
;;Encoding / decoding null
(let [bos (bs/byte-output-stream 2)]
  (p/write-null! bos)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis))]
  (println ro)))
;;Encoding / decoding booleans
(let [bos (bs/byte-output-stream 2)]
  (p/write-boolean! bos true)
  (p/write-boolean! bos false)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis)
                   (p/read-object! bis))]
    (println ro)))
;;Encoding / decoding doubles
(let [bos (bs/byte-output-stream 2)]
  (p/write-double! bos 0.0)
  (p/write-double! bos 1.0)
  (p/write-double! bos 1234.56789)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis))]
    (println ro)))
;;Encoding / decoding integers
(let [bos (bs/byte-output-stream 2)]
  (p/write-int! bos 0)
  (p/write-int! bos 32768)
  (p/write-int! bos 2147483647)
  (p/write-int! bos 8589934591)
  (p/write-int! bos 2199023255551)
  (p/write-int! bos 281474976710655)
  (p/write-int! bos 9007199254740991)
  (p/write-int! bos -2)
  (p/write-int! bos -32768)
  (p/write-int! bos -2147483648)
  (p/write-int! bos -8589934592)
  (p/write-int! bos -2199023255552)
  (p/write-int! bos -562949953421312)
  (p/write-int! bos -9007199254740991)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis)
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
                   (p/read-object! bis))]
    (println ro)))
;;Encoding / decoding longs
(let [bos (bs/byte-output-stream 2)]
  (p/write-long! bos (Long.fromString "9223372036854775807"))
  (p/write-long! bos (Long.fromString "-9223372036854775808"))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis)
                   (p/read-object! bis))]
    (println ro)))
;;Encoding / decoding strings
(let [bos (bs/byte-output-stream 2)]
  (p/write-string! bos "你好，先生好日")
  (p/write-string! bos "foo")
  (p/write-string! bos "foobarness")
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis))]
    (println ro)))

(defn ta->seq [ta] (IndexedSeq. ta 0))
;;Encoding / decoding bytes
(let [bos (bs/byte-output-stream 2)]
  (p/write-bytes! bos (make-byte-array 2))
  (p/write-bytes! bos (make-byte-array 10))
  (p/write-bytes! bos (make-byte-array 30))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (map #(ta->seq %) (vector (p/read-object! bis)
                                     (p/read-object! bis)
                                     (p/read-object! bis)))]
    (println ro)))

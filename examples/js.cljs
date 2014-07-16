(ns examples.js
  (:import [goog.math Long])
  (:require [longshi.fressian.byte-stream-protocols :as bsp]
            [longshi.fressian.protocols :as p]
            [longshi.fressian.byte-stream :as bs]
            [longshi.fressian.handlers :as fh]
            [longshi.fressian.utils :refer [make-byte-array]]
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
        ro (vector (p/read-boolean! bis)
                   (p/read-boolean! bis))]
    (println ro)))
;;Encoding / decoding floats
(let [bos (bs/byte-output-stream 2)]
  (p/write-float! bos -14.5)
  (p/write-float! bos 30.5)
  (p/write-float! bos -1234.5)
  (p/write-float! bos 1234.5)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-float! bis)
                   (p/read-float! bis)
                   (p/read-float! bis)
                   (p/read-float! bis))]
    (println ro)))
;;Encoding / decoding doubles
(let [bos (bs/byte-output-stream 2)]
  (p/write-double! bos 0.0)
  (p/write-double! bos 1.0)
  (p/write-double! bos 1234.56789)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-double! bis)
                   (p/read-double! bis)
                   (p/read-double! bis))]
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
        ro (vector (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis)
                   (p/read-int! bis))]
    (println ro)))
;;Encoding / decoding longs
(let [bos (bs/byte-output-stream 2)]
  (p/write-long! bos (Long.fromString "9223372036854775807"))
  (p/write-long! bos (Long.fromString "-9223372036854775808"))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-int! bis)
                   (p/read-int! bis))]
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

;;Encoding / decoding write-as
(let [bos (bs/byte-output-stream 2)]
  (do
    (p/write-as! bos "null" nil)
    (p/write-as! bos "bool" true)
    (p/write-as! bos "int" 42)
    (p/write-as! bos "double" 3.14)
    (p/write-as! bos "string" "foobar")
    (p/write-as! bos "bytes" (js/Uint8Array. #js [-34 12 34]))
    (p/write-as! bos "bytes" (js/Int8Array. #js [-34 12 34]))
    (p/write-as! bos nil 2.718)
    (p/write-as! bos nil 1729))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            (p/read-object! bis)
            (p/read-object! bis))]
  (println ro)))
;;Encoding / decoding write-object
(let [bos (bs/byte-output-stream 2)]
  (do
    (p/write-object! bos nil)
    (p/write-object! bos true)
    (p/write-object! bos 42)
    (p/write-object! bos 3.14)
    (p/write-object! bos "foobar")
    (p/write-object! bos (js/Uint8Array. #js [-34 12 34]))
    (p/write-object! bos (js/Int8Array. #js [-34 12 34]))
    (p/write-object! bos 2.718)
    (p/write-object! bos 1729))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            (p/read-object! bis)
            (p/read-object! bis))]
  (println ro)))
;;Encoding / decoding arrays
(let [bos (bs/byte-output-stream 2)]
  (p/write-object! bos #js [true 12.45 56 "foobar"])
  (p/write-object! bos (fh/tagged-array "boolean[]" #js [true false false]))
  (p/write-object! bos (fh/tagged-array "long[]" #js [5674 23577 -764524 (Long.fromString "9223372036854775807") (Long.fromString "-9223372036854775808")]))
  (p/write-object! bos (js/Uint16Array. #js [23 -66 89]))
  (p/write-object! bos (js/Int16Array. #js [23 -66 89]))
  (p/write-object! bos (js/Uint32Array. #js [23 10000 89]))
  (p/write-object! bos (js/Int32Array. #js [23 -66 89]))
  (p/write-object! bos (js/Float32Array. #js [12.345 -66.9544 0.1]))
  (p/write-object! bos (js/Float64Array. #js [12.345 -66.9544 0.1]))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            )]
  (println ro)))
;;Encoding / decoding custom types
(defrecord Point [x y])
(defn point [x y] (->Point x y))

(defrecord Tuple2 [x1 x2])
(defn tuple2 [x1 x2] (->Tuple2 x1 x2))

(def my-write-handlers
  (fh/create-handler
    [[Point "user.Point"
      (fn [fw point]
        (do
          (p/write-tag! fw "user.Point" 2)
          (p/write-int! fw (.-x point))
          (p/write-int! fw (.-y point))))]
     [Tuple2 "user.Tuple2"
      (fn [fw tuple2]
        (do
          (p/write-tag! fw "user.Tuple2" 2)
          (p/write-int! fw (.-x1 tuple2))
          (p/write-int! fw (.-x2 tuple2))))]]))
(def my-read-handlers
  #js {
       "user.Tuple2"
       (fn [fr tag component-count]
         (do
           (assert (== 2 component-count))
           (tuple2 (p/read-int! fr) (p/read-int! fr))))
       })

(let [bos (bs/byte-output-stream 2 my-write-handlers)]
  (p/write-object! bos (point 12 34))
  (p/write-object! bos (point -455 456))
  (p/reset-caches! bos)
  (p/write-object! bos (tuple2 55 92))
  (p/write-object! bos (tuple2 -1288 9000))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos) my-read-handlers)
        ro (vector
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            )]
  (println ro)))
;;Encoding / decoding cached values
(def long-str (.join (make-array 1024) "a"))
(extend-type string
  ICloneable
  (-clone [s] (js/String. s)))
(def cache-str
  (specify long-str
   p/CachedObject
   (cache? [_]  true)))

(let [bos (bs/byte-output-stream 2 my-write-handlers)
      stream-sizes (atom {})
      add-stream-size
      (fn [name] (swap! stream-sizes #(into %1 [[name %2]]) (count bos)))]
  (p/write-object! bos long-str true)
  (add-stream-size :first-long-str-write)
  (p/write-object! bos long-str true)
  (add-stream-size :second-long-str-write)
  (p/reset-caches! bos)
  (p/write-object! bos long-str true)
  (add-stream-size :first-long-str-write-after-cache-reset)
  (p/write-object! bos long-str true)
  (add-stream-size :second-long-str-write-after-cache-reset)
  (p/reset-caches! bos)
  (p/write-object! bos cache-str)
  (add-stream-size :first-cached-str-write)
  (p/write-object! bos cache-str)
  (add-stream-size :second-cached-str-write)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos) my-read-handlers)
        ro (vector
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            )]
  (println ro)
  (println @stream-sizes)))
;;Encoding / decoding footers
(let [bos (bs/byte-output-stream 2)]
  (do
    (p/write-object! bos nil)
    (p/write-object! bos true)
    (p/write-object! bos 42)
    (p/write-footer! bos)
    (p/write-object! bos 3.14)
    (p/write-object! bos "foobar")
    (p/write-object! bos (js/Uint8Array. #js [-34 12 34]))
    (p/write-footer! bos)
    (p/write-object! bos (js/Int8Array. #js [-99 12 56]))
    (p/write-object! bos 2.718)
    (p/write-footer! bos)
    )
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (p/read-object! bis)
            (ta->seq (p/read-object! bis))
            (ta->seq (p/read-object! bis))
            (p/read-object! bis)
            )]
    (do
      (p/validate-footer! bis)
      (println ro))))
;;Encoding / decoding lists
(let [bos (bs/byte-output-stream 2)]
  (p/write-list! bos [12.4 "foo" nil false])
  (p/write-list! bos ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis)
                   (p/read-object! bis))]
    (println ro)))
;;Encoding / decoding fressain lists
(let [bos (bs/byte-output-stream 2)]
  (p/begin-open-list! bos)
  (p/write-object! bos true)
  (p/write-object! bos 42)
  (p/write-object! bos 3.14)
  (p/begin-closed-list! bos)
  (p/write-object! bos "roy")
  (p/write-object! bos "g")
  (p/write-object! bos "biv")
  (p/end-list! bos)
  (p/write-object! bos "full")
  (p/end-list! bos)
  (p/write-object! bos "stop")
  (p/write-footer! bos)
  (p/begin-open-list! bos)
  (p/write-object! bos 1)
  (p/write-object! bos 1)
  (p/write-object! bos 2)
  (p/write-object! bos 3)
  (p/write-object! bos 5)
  (p/write-object! bos 8)
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   )]
    (println ro)))
;;Encoding / decoding appending output bytestreams to each other
(let [bos (bs/byte-output-stream 2)
      full-bos (bs/byte-output-stream 2)]
  (p/write-object! bos 1)
  (p/write-object! bos 1)
  (p/write-object! bos 2)
  (p/write-object! bos 3)
  (p/write-object! bos 5)
  (p/write-object! bos 8)
  (p/write-object! full-bos "begin")
  (p/write-footer! full-bos)
  (p/write-footer-for! full-bos bos)
  (p/write-object! full-bos "end")
  (p/write-object! full-bos 9000)
  (let [bis (bs/byte-input-stream (bsp/get-bytes full-bos))
        ro (vector (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   (p/read-object! bis)
                   )]
    (println ro)))
;;Encoding / decoding inst, uuid, and regex data types
(let [bos (bs/byte-output-stream 2)]
  (p/write-object! bos (js/Date. 12))
  (p/write-object! bos (UUID. "ec9b1b11-74b0-48a4-989c-7e939fd37dec"))
  (p/write-object! bos (js/RegExp. "[0-9a-fA-F]{1,4}" "g"))
  (let [bis (bs/byte-input-stream (bsp/get-bytes bos))
        ro (vector  (p/read-object! bis)
                    (p/read-object! bis)
                    (p/read-object! bis)
                   )]
    (println ro)))

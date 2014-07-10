(ns longshi.fressian.byte-stream
  (:require [longshi.fressian.byte-stream-protocols :as bsp]
            [longshi.fressian.protocols :as p]
            [longshi.fressian.handlers :as fh]
            [longshi.fressian.hop-map :as hm]
            [longshi.fressian.utils :refer [make-byte-array make-data-view]]))

(defn adler32
  ([ba] (adler32 ba 1))
  ([ba adler]
    (let [ba-len (alength ba)
          sums (js/Uint32Array. 2)]
      (do
        (aset sums (bit-and adler 0xffff) 0)
        (aset sums (bit-and (unsigned-bit-shift-right adler 16) 0xffff) 1)
        (loop [i 0 adler-chunk 0]
          (when (< i ba-len)
            (do
              (aset sums 0 (+ (aget ba i) (aget sums 0)))
              (aset sums 1 (+ (aget sums 0) (aget sums 1)))
              (if (== adler-chunk 5552)
                (do
                  (aset sums 0 (js-mod (aget sums 0) 65521))
                  (aset sums 1 (js-mod (aget sums 1) 65521))
                  (recur (inc i) 0))
                (recur (inc i) (inc adler-chunk))))))
        (aset sums 0 (js-mod (aget sums 0) 65521))
        (aset sums 1 (js-mod (aget sums 1) 65521))
        (unsigned-bit-shift-right (bit-or (bit-shift-left (aget sums 1) 16) (aget sums 0)) 0)))))

(def ^:private little-endian false)
;;Integer buffer
(def ^:private i32a (make-byte-array 4))
(def ^:private i32adv (make-data-view i32a))
;;Double buffer
(def ^:private da (make-byte-array 8))
(def ^:private dadv (make-data-view da))
(deftype ByteOutputStream [^:mutable stream ^:mutable cnt handlers ^:mutable struct-cache ^:mutable priority-cache]
  Object
  (reset-caches! [bos]
    (do
      (set! struct-cache (hm/interleaved-index-hop-map 16))
      (set! priority-cache (hm/interleaved-index-hop-map 16))))
  bsp/WriteStream
  (write! [bos b]
    (let [new-count (inc cnt)]
      (if (< (alength stream) new-count)
        (let [new-stream (make-byte-array (max new-count (bit-shift-left cnt 1)))]
          (.set new-stream stream)
          (set! stream new-stream)))
      (aset stream cnt b)
      (set! cnt new-count)))
  (write-bytes! [bos b off len]
    (let [new-count (+ cnt len)]
      (if (< (alength stream) new-count)
        (let [new-stream (make-byte-array (max new-count (bit-shift-left cnt 1)))]
          (.set new-stream stream)
          (set! stream new-stream)))
      (.set stream (.subarray b off (+ off len)) cnt)
      (set! cnt new-count)))
   (get-bytes [bos]
     (let [new-stream (make-byte-array cnt)]
       (.set new-stream (.subarray stream 0 cnt))
       new-stream))
  bsp/IntegerWriteStream
  (write-int16! [bos i16]
    (do
      (.setInt16 i32adv 0 i16 little-endian)
      (bsp/write-bytes! bos i32a 0 2)))
  (write-int24! [bos i24]
    (do
      (.setInt32 i32adv 0 i24 little-endian)
      (bsp/write-bytes! bos i32a 0 3)))
  (write-int32! [bos i32]
    (do
      (.setInt32 i32adv 0 i32 little-endian)
      (bsp/write-bytes! bos i32a 0 4)))
  (write-unsigned-int16! [bos ui16]
    (do
      (.setUint16 i32adv 0 ui16 little-endian)
      (bsp/write-bytes! bos i32a 0 2)))
  (write-unsigned-int24! [bos ui24]
    (do
      (.setUint32 i32adv 0 ui24 little-endian)
      (bsp/write-bytes! bos i32a 0 3)))
  (write-unsigned-int32! [bos ui32]
    (do
      (.setUint32 i32adv 0 ui32 little-endian)
      (bsp/write-bytes! bos i32a 0 4)))
  bsp/FloatWriteStream
  (write-float! [bos f]
    (do
      (.setFloat32 dadv 0 f little-endian)
      (bsp/write-bytes! bos da 0 4)))
  bsp/DoubleWriteStream
  (write-double! [bos d]
    (do
      (.setFloat64 dadv 0 d little-endian)
      (bsp/write-bytes! bos da 0 8)))
  bsp/SeekStream
  (seek! [bos pos]
      (if (< cnt pos)
        (throw (js/Error. (str "Tried to seek to (" pos ").  Stream is of size (" cnt ")"))))
       (set! cnt pos))
  ICounted
  (-count [bos] cnt))

(defn byte-output-stream
  ([] (byte-output-stream 32))
  ([len] (byte-output-stream 32 #js {}))
  ([len user-handlers]
   (->ByteOutputStream
    (make-byte-array len)
    0
    (fh/write-lookup fh/core-write-handlers user-handlers)
    (hm/interleaved-index-hop-map 16)
    (hm/interleaved-index-hop-map 16))))

(deftype StructCache [tag fields])
(defn struct-cache
  [tag fields]
  (->StructCache tag fields))

(def under-construction #js {})

(deftype ByteInputStream [^:mutable stream ^:mutable cnt handlers standard-handlers ^:mutable struct-cache  ^:mutable priority-cache]
  Object
  (reset-caches! [bos]
    (do
      (set! struct-cache #js [])
      (set! priority-cache #js [])))
  (handle-struct [bis tag fields]
    (let [rh (or (aget handlers tag) (aget standard-handlers tag))]
      (if rh
        (rh bis tag fields)
        (let [values (make-array fields)]
          (do
            (dotimes [i fields]
              (aset values i (p/read-object! bis)))
            (fh/tagged-object tag values))))))
  (lookup-cache [bis cache index]
    (if (< index (alength cache))
      (let [result (aget cache index)]
        (if (identical? under-construction result)
          (throw (js/Error. "Unable to resolve circular refernce in cache"))
          result
          ))
       (throw (js/Error. "Requested object beyond end of cache: (" index ")"))))
  (read-and-cache-object [bis cache]
    (let [index (alength cache)]
      (do
        (.push cache under-construction)
        (let [o (p/read-object! bis)]
          (do
            (aset cache index o)
            o)))))
  bsp/ReadStream
  (read! [bis]
    (let [old-count cnt]
      (if (< (alength stream) cnt)
        (throw (js/Error. "Can not read (1) bytes, Input Stream only has (0) bytes available")))
      (set! cnt (inc cnt))
      (aget stream old-count)))
  (read-bytes! [bis b off len]
    (let [old-count cnt]
      (if (< (alength stream) (+ cnt len off))
        (throw (js/Error. (str "Can not read (" len ") bytes at offset (" off "), Input Stream only has (" (bsp/available bis)") bytes available"))))
      (set! cnt (+ cnt off len))
      (.set b (.subarray stream (+ old-count off) (+ old-count off len)))))
  (available [bis] (max 0 (- (alength stream) cnt)))
  bsp/IntegerReadStream
  (read-int16! [bis]
    (do
      (bsp/read-bytes! bis i32a 0 2)
      (.getInt16 i32adv 0 little-endian)))
  (read-int24! [bis]
    (do
      (bsp/read-bytes! bis (.subarray i32a 1 4) 0 3)
      (aset i32a 0 0)
      (.getInt32 i32adv 0 little-endian)))
  (read-int32! [bis]
    (do
      (bsp/read-bytes! bis i32a 0 4)
      (.getInt32 i32adv 0 little-endian)))
  (read-unsigned-int16! [bis]
    (do
      (bsp/read-bytes! bis i32a 0 2)
      (.getUint16 i32adv 0 little-endian)))
  (read-unsigned-int24! [bis]
    (do
      (bsp/read-bytes! bis (.subarray i32a 1 4) 0 3)
      (aset i32a 0 0)
      (.getUint32 i32adv 0 little-endian)))
  (read-unsigned-int32! [bis]
    (do
      (bsp/read-bytes! bis i32a 0 4)
      (.getUint32 i32adv 0 little-endian)))
  bsp/FloatReadStream
  (read-float! [bis]
    (do
      (bsp/read-bytes! bis da 0 4)
      (.getFloat32 dadv 0 little-endian)))
  bsp/DoubleReadStream
  (read-double! [bis]
    (do
      (bsp/read-bytes! bis da 0 8)
      (.getFloat64 dadv 0 little-endian)))
  bsp/SeekStream
  (seek! [bis pos]
      (if (< (alength stream) pos)
        (throw (js/Error. (str "Tried to seek to (" pos ").  Stream is of size (" (alength stream) ")"))))
       (set! cnt pos))
  ICounted
  (-count [bis] (alength stream)))

(defn byte-input-stream
  ([stream] (byte-input-stream stream #js {}))
  ([stream user-handlers] (->ByteInputStream stream 0 user-handlers fh/core-read-handlers #js [] #js [])))

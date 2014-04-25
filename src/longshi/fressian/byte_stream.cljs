(ns longshi.fressian.byte-stream
  (:use-macros [longshi.macros :only [make-byte-array make-data-view]])
  (:require [longshi.fressian.byte-stream-protocols :as bsp]))

(deftype ByteOutputStream [^:mutable stream ^:mutable cnt]
  bsp/WriteStream
  (write! [bos b]
    (let [new-count (inc cnt)]
      (if (< (.-length stream) new-count)
        (let [new-stream (make-byte-array (max new-count (bit-shift-left cnt 1)))]
          (.set new-stream stream)
          (set! stream new-stream)))
      (aset stream cnt b)
      (set! cnt new-count)))
  (write-bytes! [bos b off len]
    (let [new-count (+ cnt len)]
      (if (< (.-length stream) new-count)
        (let [new-stream (make-byte-array (max new-count (bit-shift-left cnt 1)))]
          (.set new-stream stream)
          (set! stream new-stream)))
      (.set stream (.subarray b off (+ off len)) cnt)
      (set! cnt new-count)))
   (get-bytes [bos]
     (let [new-stream (make-byte-array cnt)]
       (.set new-stream (.subarray stream 0 cnt))
       new-stream))
  bsp/SeekStream
  (seek! [bos pos]
      (if (< cnt pos)
        (throw (js/Error. (str "Tried to seek to (" pos ").  Stream is of size (" cnt ")"))))
       (set! cnt pos))
  ICounted
  (-count [bos] cnt))

(defn byte-output-stream
  ([] (byte-output-stream 32))
  ([len] (->ByteOutputStream (make-byte-array len) 0)))

(deftype ByteInputStream [^:mutable stream ^:mutable cnt]
  bsp/ReadStream
  (read! [bis]
    (let [old-count cnt]
      (if (< (.-length stream) cnt)
        (throw (js/Error. "Can not read (1) bytes, Input Stream only has (0) bytes available")))
      (set! cnt (inc cnt))
      (aget stream old-count)))
  (read-bytes! [bis b off len]
    (let [old-count cnt]
      (if (< (.-length stream) (+ cnt len off))
        (throw (js/Error. (str "Can not read (" len ") bytes at offset (" off "), Input Stream only has (" (bsp/available bis)") bytes available"))))
      (set! cnt (+ cnt off len))
      (.set b (.subarray stream (+ old-count off) (+ old-count off len)))))
  (available [bis] (max 0 (- (.-length stream) cnt)))
  bsp/SeekStream
  (seek! [bis pos]
      (if (< (.-length stream) pos)
        (throw (js/Error. (str "Tried to seek to (" pos ").  Stream is of size (" (.-length stream) ")"))))
       (set! cnt pos))
  ICounted
  (-count [bis] (.-length stream)))

(defn byte-input-stream [stream]
  (->ByteInputStream stream 0))

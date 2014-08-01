(ns longshi.fressian.js
  "Adding fressian writers and reader protocols to input and output bytestreams"
  (:import [goog.math Long]
           [goog.string StringBuffer])
  (:require [longshi.fressian.byte-stream-protocols :as bsp]
            [longshi.fressian.protocols :as p]
            [longshi.fressian.codes :as c]
            [longshi.fressian.utils :refer [make-byte-array make-data-view make-string-writer make-string-reader]]
            [longshi.fressian.byte-stream :as bs]))

;;Integer constants
(def ^:private max-neg-js-hb-int (bit-shift-left -1 21))
(def ^:private max-pos-js-hb-int (dec (bit-shift-left 1 21)))
(def ^:private two-power-16 (bit-shift-left 1 16))
(def ^:private two-power-24 (bit-shift-left 1 24))
(def ^:private two-power-32 (* two-power-16 two-power-16))
;;Integer helpers
(def zero-table #js [31 22 30 21 18 10 29 2 20 17 15 13 9 6 28 1 23 19 11 3 16 14 7 24 12 4 8 25 5 26 27 0])
(defn leading-zeros [v]
  "finds the number of zeros before the most significant bit

  x (int) - Positive integer to get the number of leading zeros

  This is based off of an algorithm of finding the most significant bit
  http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogDeBruijn
  "
  (let [x (loop [x v y 1]
            (if (<= y 16)
              (recur (bit-or x (bit-shift-right x y)) (bit-shift-left y 1))
              x))]
    (aget zero-table (unsigned-bit-shift-right (* x 0x07C4ACDD) 27))))

(defn bit-switch [x]
  "Gets the 64 minus the number of bits needed for the number

  x (Long) - Google Closure Long (for perf reasons) to get the bits of

  -1 and 0 return 64."
  (cond
    (== 0 (.-low_ x) (.-high_ x)) 64
    (== -1 (.-low_ x) (.-high_ x)) 64
    (neg? (.-high_ x))
    (let [low (bit-not (.-low_ x))
          high (bit-not (.-high_ x))
          high-zeros (if (zero? high) 32 0)
          leading-bits (if (zero? high) low high)]
      (+ high-zeros (leading-zeros leading-bits)))
   :else
   (let [high-zeros (if (zero? (.-high_ x)) 32 0)
         leading-bits (if (zero? (.-high_ x)) (.-low_ x) (.-high_ x))]
     (+ high-zeros (leading-zeros leading-bits)))))

(def string-writer (make-string-writer))

(defn- should-skip-cache
  "Check if the value is to small for cache to be effective

  o (object) - Value to check"
  [o]
  (cond
    (or (nil? o) (identical? js/Boolean (.-constructor o))) true
    (and (number? o) (== 1 (js-mod o 1)) (< -255 o 255)) true
    (and (string? o) (zero? (.-length o))) true
    :else false))

(deftype
  ^{:doc
   "Wrapper object for caching

   co (object) - Object to be cached in the fressian stream"}
  CachedObject [co]
  p/Cached
  (cached-value [_] co))

(defn cache
  "Creates a cached object"
  [o]
  (CachedObject. o))

(extend-type bs/ByteOutputStream
  p/StreamingWriter
  (begin-closed-list! [bos] (bsp/write! bos (.-BEGIN_CLOSED_LIST c/codes)))
  (end-list! [bos] (bsp/write! bos (.-END_COLLECTION c/codes)))
  (begin-open-list! [bos]
    (if-not (zero? (bsp/bytes-written bos))
      (throw (js/Error. "openList must be called from the top level, outside any footer context."))
      (do
        (bsp/reset! bos)
        (bsp/write! bos (.-BEGIN_OPEN_LIST c/codes))
        bos)))
  (write-footer-for! [bos byte-buffer]
    (let [source (bsp/duplicate-bytes byte-buffer)]
      (if-not (zero? (bsp/bytes-written bos))
        (throw (js/Error. "writeFooterFor can only be called at a footer boundary."))
        (do
          (bsp/write-bytes! bos source 0 (alength source))
          (let [length (bsp/bytes-written bos)]
            (do
              (bsp/write-int32! bos (.-FOOTER_MAGIC c/codes))
              (bsp/write-int32! bos length)
              (bsp/write-int32! bos (bsp/get-checksum bos))
              (bsp/reset! bos)
              (.clear-caches! bos)))
          bos))))
  p/FressianWriter
  (write-null! [bos]
    (do
      (bsp/write! bos (.-NULL c/codes))
      bos))
  (write-boolean! [bos b]
    (do
      (bsp/write! bos (if b (.-TRUE c/codes) (.-FALSE c/codes)))
      bos))
  (write-string! [bos s]
    (let [str-length (alength s)
          sba (.get-buffer string-writer)]
      (do
        (loop [str-pos 0]
          (let [sa (.string-chunk-utf8 string-writer s str-pos str-length)
                new-str-pos (+ str-pos (aget sa 0))
                buf-pos (aget sa 1)]
            (cond
              (< buf-pos (.-STRING_PACKED_LENGTH_END c/ranges)) (bsp/write! bos (+ (.-STRING_PACKED_LENGTH_START c/codes) buf-pos))
              (== new-str-pos str-length)
                (do
                  (bsp/write! bos (.-STRING c/codes))
                  (p/write-int! bos buf-pos))
              :else
                (do
                  (bsp/write! bos (.-STRING_CHUNK c/codes))
                  (p/write-int! bos buf-pos)))
            (bsp/write-bytes! bos sba 0 buf-pos)
            (if (< new-str-pos str-length)
              (recur new-str-pos))))
        bos)))
  (write-bytes! [bos b]
    (do
      (if (< (alength b) (.-BYTES_PACKED_LENGTH_END c/ranges))
        (do
          (bsp/write! bos (+ (alength b) (.-BYTES_PACKED_LENGTH_START c/codes)))
          (bsp/write-bytes! bos b 0 (alength b)))
        (loop [offset 0 length (alength b)]
          (if (> length (.-BYTE_CHUNK_SIZE c/ranges))
            (do
              (bsp/write! bos (.-BYTES_CHUNK c/codes))
              (p/write-int! bos (.-BYTE_CHUNK_SIZE c/ranges))
              (bsp/write-bytes! bos b offset (.-BYTE_CHUNK_SIZE c/ranges))
              (recur (+ offset (.-BYTE_CHUNK_SIZE c/ranges)) (- length (.-BYTE_CHUNK_SIZE c/ranges))))
            (do
              (bsp/write! bos (.-BYTES c/codes))
              (p/write-int! bos length)
              (bsp/write-bytes! bos b offset length)))))
      bos))
  (write-int! [bos i]
    (p/write-long!
     bos
     (let [high (bit-or (/ i (.-TWO_PWR_32_DBL_ Long)) 0)
        high (if (neg? i) (dec high) high)
        low (bit-or (js-mod i (.-TWO_PWR_32_DBL_ Long)) 0)]
      (Long. low high))))
  (write-long! [bos l]
    (do
      (let [lb (.getLowBits l)
            hb (.getHighBits l)
            bits (bit-switch l)]
        (cond
          (<= 1 bits 14)
          (do
            (bsp/write! bos (.-INT c/codes))
            (bsp/write-int32! bos hb)
            (bsp/write-int32! bos lb))
          (<= 15 bits 22)
          (let [ic (bit-shift-right hb 16)]
            (do
              (bsp/write! bos (+ (.-INT_PACKED_7_ZERO c/codes) ic))
              (bsp/write-int16! bos hb)
              (bsp/write-int32! bos lb)))
          (<= 23 bits 30)
          (let [ic (bit-shift-right hb 8)]
            (do
              (bsp/write! bos (+ (.-INT_PACKED_6_ZERO c/codes) ic))
              (bsp/write! bos hb)
              (bsp/write-int32! bos lb)))
          (<= 31 bits 38)
          (do
            (bsp/write! bos (+ (.-INT_PACKED_5_ZERO c/codes) hb))
            (bsp/write-int32! bos lb))
          (<= 39 bits 44)
          (do
            (bsp/write! bos (+ (.-INT_PACKED_4_ZERO c/codes) (bit-shift-right lb 24)))
            (bsp/write-int24! bos lb))
          (<= 45 bits 51)
          (do
            (bsp/write! bos (+ (.-INT_PACKED_3_ZERO c/codes) (bit-shift-right lb 16)))
            (bsp/write-int16! bos lb))
          (<= 52 bits 57)
          (do
            (bsp/write! bos (+ (.-INT_PACKED_2_ZERO c/codes) (bit-shift-right lb 8)))
            (bsp/write! bos lb))
          (<= 58 bits 64)
          (do
            (when (< lb -1)
              (bsp/write! bos (+ (.-INT_PACKED_2_ZERO c/codes) (bit-shift-right lb 8))))
            (bsp/write! bos lb))
          :else
          (throw (js/Error. (str "Long (" l ") can not be converted")))))
      bos))
  (write-float! [bos f]
    (do
      (bsp/write! bos (.-FLOAT c/codes))
      (bsp/write-float! bos f)
      bos))
  (write-double! [bos d]
    (do
      (cond
        (== d 0.0) (bsp/write! bos (.-DOUBLE_0 c/codes))
        (== d 1.0) (bsp/write! bos (.-DOUBLE_1 c/codes))
        :else
        (do
          (bsp/write! bos (.-DOUBLE c/codes))
          (bsp/write-double! bos d)))
      bos))
  (write-list! [bos l]
    (do
      (let [list-seq
            (cond
              (array? l) (prim-seq l)
              (seq? l) l
              (seqable? l) (seq l))
            length (count l)]
        (do
          (if (< length (.-LIST_PACKED_LENGTH_END c/ranges))
            (bsp/write! bos (+ length (.-LIST_PACKED_LENGTH_START c/codes)))
            (do
              (bsp/write! bos (.-LIST c/codes))
              (p/write-int! bos length)))
          (doseq [li l]
            (p/write-object! bos li))))
      bos))
  (write-tag! [bos tag component-count]
    (if-let [shortcut-code (aget c/tag-to-code tag)]
      (bsp/write! bos shortcut-code)
      (let [index (.old-index! (.-struct-cache bos) tag)]
        (cond
          (== -1 index)
          (do
            (bsp/write! bos (.-STRUCTTYPE c/codes))
            (p/write-object! bos tag)
            (p/write-int! bos component-count))
          (< index (.-STRUCT_CACHE_PACKED_END c/ranges))
          (bsp/write! bos (+ (.-STRUCT_CACHE_PACKED_START c/codes) index))
          :else
          (do
            (bsp/write! bos (.-STRUCT c/codes))
            (p/write-int! bos index))))))
  (write-object! [bos o]
    (p/write-object! bos o false))
  (write-object! [bos o cache]
    (p/write-as! bos nil o cache))
  (write-as! [bos tag o]
    (p/write-as! bos tag o false))
  (write-as! [bos tag o cache]
    (let [co (if (instance? CachedObject o) (p/cached-value o) o)
          is-cache (or cache (instance? CachedObject o))]
      (do
        (if is-cache
          (if (should-skip-cache co)
            (p/write-as! bos tag co false)
            (let [index (.old-index! (.-priority-cache bos) co)]
              (cond
                (== -1 index)
                (do
                  (bsp/write! bos (.-PUT_PRIORITY_CACHE c/codes))
                  (p/write-as! bos tag co false))
                (< index (.-PRIORITY_CACHE_PACKED_END c/ranges))
                (bsp/write! bos (+ (.-PRIORITY_CACHE_PACKED_START c/codes) index))
                :else
                (do
                  (bsp/write! bos (.-GET_PRIORITY_CACHE c/codes))
                  (p/write-int! bos index)))))
          ((.require-write-handler (.-handlers bos) tag co) bos co))
        bos)))
  (reset-caches! [bos]
    (do
      (.clear-caches! bos)
      (bsp/write! bos (.-RESET-CACHES c/codes))
      bos))
  (write-footer! [bos]
    (let [length (bsp/bytes-written bos)]
      (do
        (bsp/write-int32! bos (.-FOOTER_MAGIC c/codes))
        (bsp/write-int32! bos length)
        (bsp/write-int32! bos (bsp/get-checksum bos))
        (bsp/reset! bos)
        (.clear-caches! bos)
        bos))))

(def string-reader (make-string-reader))
(def ^:private string-buffer (StringBuffer.))
(defn read-string-buffer!
  "Reads a string from a byte array
  bis (ByteInputStream) - Fressian input stream to get bytes from
  string-buffer (StringBuffer) - Buffer to append string to
  byte-len (int) - The number of bytes to read from the bytestream"
  [bis string-buffer byte-len]
    (do
      (bsp/read-bytes! bis (.get-buffer string-reader) 0 byte-len)
      (.read-utf8-chars string-reader string-buffer 0 byte-len)))

(extend-type bs/ByteInputStream
  p/FressianReader
  (read-boolean! [bis]
    (let [code (bsp/read! bis)]
      (cond
        (== code (.-TRUE c/codes)) true
        (== code (.-FALSE c/codes)) false
       :else
      (throw (js/Error. (str "Expected boolean got (" code ")"))))))
  (read-float! [bis]
    (let [code (bsp/read! bis)]
      (if (== code (.-FLOAT c/codes))
        (bsp/read-float! bis)
        (throw (js/Error. (str "Expected float got (" code ")"))))))
  (read-double! [bis]
    (let [code (bsp/read! bis)]
      (cond
        (== code (.-DOUBLE_0 c/codes)) 0.0
        (== code (.-DOUBLE_1 c/codes)) 1.0
        (== code (.-DOUBLE c/codes)) (bsp/read-double! bis))))
  (read-int! [bis]
    (let [code (bsp/read! bis)]
      (cond
        (== code 0xFF) -1
        (<= 0x00 code 0x3F)
        (bit-and code 0xFF)
        (<= 0x40 code 0x5F)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_2_ZERO c/codes)) 8)
          (bsp/read! bis))
        (<= 0x60 code 0x6F)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_3_ZERO c/codes)) 16)
          (bsp/read-unsigned-int16! bis))
        (<= 0x70 code 0x73)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_4_ZERO c/codes)) 24)
          (bsp/read-unsigned-int24! bis))
        (<= 0x74 code 0x77)
        (let [i32 (bsp/read-unsigned-int32! bis)]
          (cond
            (== code 0x74) (+ i32 (* -2 two-power-32))
            (== code 0x75) (+ i32 (* -1 two-power-32))
            (== code 0x76) i32
            (== code 0x77) (+ i32 two-power-32)))
        (<= 0x78 code 0x7B)
        (let [ih8 (bsp/read! bis)
              il32 (bsp/read-unsigned-int32! bis)
              i40 (+ (* ih8 two-power-32) il32)]
          (cond
            (== code 0x78) (+ i40 (* -512 two-power-32))
            (== code 0x79) (+ i40 (* -256 two-power-32))
            (== code 0x7A) i40
            (== code 0x7B) (+ i40 (* 256 two-power-32))))
        (<= 0x7C code 0x7F)
        (let [ih16 (bsp/read-unsigned-int16! bis)
              il32 (bsp/read-unsigned-int32! bis)
              i48 (+ (* ih16 two-power-32) il32)]
          (cond
            (== code 0x7C) (+ i48 (* -2 two-power-16 two-power-32))
            (== code 0x7D) (+ i48 (* -1 two-power-16 two-power-32))
            (== code 0x7E) i48
            (== code 0x7F) (+ i48 (* two-power-16 two-power-32))))
        (== code (.-INT c/codes))
        (let [ih32 (bsp/read-int32! bis)
              il32 (bsp/read-unsigned-int32! bis)]
          (if (or (> ih32 max-pos-js-hb-int) (< ih32 max-neg-js-hb-int))
            (Long. il32 ih32)
            (+ (* ih32 two-power-32) il32)))
       :else
        (throw (js/Error. (str "Expected int64 got (" code ")"))))))
  (read-object! [bis]
    (let [code (bsp/read! bis)]
      (cond
        (== code (.-NULL c/codes)) nil
        (== code (.-TRUE c/codes)) true
        (== code (.-FALSE c/codes)) false
        (== code 0xFF) -1
        (<= 0x00 code 0x3F)
        (bit-and code 0xFF)
        (<= 0x40 code  0x5F)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_2_ZERO c/codes)) 8)
          (bsp/read! bis))
        (<= 0x60 code 0x6F)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_3_ZERO c/codes)) 16)
          (bsp/read-unsigned-int16! bis))
        (<= 0x70 code 0x73)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_4_ZERO c/codes)) 24)
          (bsp/read-unsigned-int24! bis))
        (<= 0x74 code 0x77)
        (let [i32 (bsp/read-unsigned-int32! bis)]
          (cond
            (== code 0x74) (+ i32 (* -2 two-power-32))
            (== code 0x75) (+ i32 (* -1 two-power-32))
            (== code 0x76) i32
            (== code 0x77) (+ i32 two-power-32)))
        (<= 0x78 code 0x7B)
        (let [ih8 (bsp/read! bis)
              il32 (bsp/read-unsigned-int32! bis)
              i40 (+ (* ih8 two-power-32) il32)]
          (cond
            (== code 0x78) (+ i40 (* -512 two-power-32))
            (== code 0x79) (+ i40 (* -256 two-power-32))
            (== code 0x7A) i40
            (== code 0x7B) (+ i40 (* 256 two-power-32))))
        (<= 0x7C code 0x7F)
        (let [ih16 (bsp/read-unsigned-int16! bis)
              il32 (bsp/read-unsigned-int32! bis)
              i48 (+ (* ih16 two-power-32) il32)]
          (cond
            (== code 0x7C) (+ i48 (* -2 two-power-16 two-power-32))
            (== code 0x7D) (+ i48 (* -1 two-power-16 two-power-32))
            (== code 0x7E) i48
            (== code 0x7F) (+ i48 (* two-power-16 two-power-32))))
        (== code (.-INT c/codes))
        (let [ih32 (bsp/read-int32! bis)
              il32 (bsp/read-unsigned-int32! bis)]
          (if (or (> ih32 max-pos-js-hb-int) (< ih32 max-neg-js-hb-int))
            (Long. il32 ih32)
            (+ (* ih32 two-power-32) il32)))
        (== code (.-STRING c/codes))
        (let [string-buffer string-buffer]
          (do
            (.set string-buffer "")
            (read-string-buffer! bis string-buffer (p/read-object! bis))
            (.toString string-buffer)))
        (<= 0xDA code 0xE1)
        (let [string-buffer string-buffer]
          (do
            (.set string-buffer "")
            (read-string-buffer! bis string-buffer (- code (.-STRING_PACKED_LENGTH_START c/codes)))
            (.toString string-buffer)))
        (== code (.-STRING_CHUNK c/codes))
        (let [string-buffer string-buffer]
          (do
            (.set string-buffer "")
            (read-string-buffer! bis string-buffer (p/read-object! bis))
            (loop []
              (let [next-code (bsp/read! bis)]
                (cond
                  (== next-code (.-STRING c/codes))
                  (read-string-buffer! bis string-buffer (p/read-object! bis))
                  (<= 0xDA next-code 0xE1)
                  (read-string-buffer! bis string-buffer (- next-code (.-STRING_PACKED_LENGTH_START c/codes)))
                  (== next-code (.-STRING_CHUNK c/codes))
                    (do
                      (read-string-buffer! bis string-buffer (p/read-object! bis))
                      (recur))
                  :else
                  (throw (js/Error. (str "Expected chunked string (" next-code ")"))))))
            (.toString string-buffer)))
        (== code (.-BYTES c/codes))
        (let [length (p/read-object! bis)
              ba (make-byte-array length)]
          (do
            (bsp/read-bytes! bis ba 0 length)
            ba))
        (<= 0xD0 code 0xD7)
        (let [length (- code (.-BYTES_PACKED_LENGTH_START c/codes))
              ba (make-byte-array length)]
          (do
            (bsp/read-bytes! bis ba 0 length)
            ba))
        (== code (.-BYTES_CHUNK c/codes))
        (let [chunks (array)]
          (do
            (loop [code code]
              (cond
                (== code (.-BYTES_CHUNK c/codes))
                (let [length (p/read-object! bis)
                      ba (make-byte-array length)]
                  (do
                    (bsp/read-bytes! bis ba 0 length)
                    (.push chunks ba)
                    (recur (bsp/read! bis))))
                (== code (.-BYTES c/codes))
                (let [length (p/read-object! bis)
                      ba (make-byte-array length)]
                  (do
                    (bsp/read-bytes! bis ba 0 length)
                    (.push chunks ba)))
                :else
                (throw (js/Error. (str "conclusion of chunked bytes (" code ")")))))
            (let [length (reduce #(+ %1 (alength %2)) 0 (seq chunks))
                  ba (make-byte-array length)]
               (do
                 (loop [pos 0 chunk 0]
                   (when (< chunk (alength chunks))
                     (.set ba (aget chunks chunk) pos)
                     (recur (+ pos (alength (aget chunks chunk))) (inc chunk))))
                 ba))))
        (== code (.-BOOLEAN-ARRAY c/codes))
        (.handle-struct bis "boolean[]" 2)
        (== code (.-INT-ARRAY c/codes))
        (.handle-struct bis "int[]" 2)
        (== code (.-FLOAT-ARRAY c/codes))
        (.handle-struct bis "float[]" 2)
        (== code (.-DOUBLE-ARRAY c/codes))
        (.handle-struct bis "double[]" 2)
        (== code (.-LONG-ARRAY c/codes))
        (.handle-struct bis "long[]" 2)
        (== code (.-OBJECT-ARRAY c/codes))
        (.handle-struct bis "Object[]" 2)
        (== code (.-MAP c/codes))
        (.handle-struct bis "map" 1)
        (== code (.-SET c/codes))
        (.handle-struct bis "set" 1)
        (== code (.-UUID c/codes))
        (.handle-struct bis "uuid" 2)
        (== code (.-REGEX c/codes))
        (.handle-struct bis "regex" 1)
        (== code (.-URI c/codes))
        (.handle-struct bis "uri" 1)
        (== code (.-BIGINT c/codes))
        (.handle-struct bis "bigint" 1)
        (== code (.-BIGDEC c/codes))
        (.handle-struct bis "bigdec" 2)
        (== code (.-INST c/codes))
        (.handle-struct bis "inst" 1)
        (== code (.-SYM c/codes))
        (.handle-struct bis "sym" 2)
        (== code (.-KEY c/codes))
        (.handle-struct bis "key" 2)
        (== code (.-BEGIN_CLOSED_LIST c/codes))
        (let [oa #js []]
          (loop []
            (if (== (.-END_COLLECTION c/codes) (.peek-read bis))
              (do
                (bsp/read! bis)
                oa)
              (do
                (.push oa (p/read-object! bis))
                (recur)))))
        (== code (.-BEGIN_OPEN_LIST c/codes))
        (let [oa #js []]
          (loop [code (.peek-read bis)]
            (if (or (== (.-END_COLLECTION c/codes) code) (nil? code))
              (do
                (bsp/read! bis)
                oa)
              (do
                (.push oa (p/read-object! bis))
                (recur (.peek-read bis))))))
        (<= 0xE4 code 0xEB)
        (let [oa #js []
              length (- code (.-LIST_PACKED_LENGTH_START c/codes))]
          (do
            (dotimes [i length]
              (.push oa (p/read-object! bis)))
            oa))
        (== code (.-LIST c/codes))
        (let [oa #js []
              length (p/read-object! bis)]
          (do
            (dotimes [i length]
              (.push oa (p/read-object! bis)))
            oa))
        (== code (.-STRUCTTYPE c/codes))
        (let [tag (p/read-object! bis)
              fields (p/read-int! bis)]
          (do
            (.push (.-struct-cache bis) (bs/struct-cache tag fields))
            (.handle-struct bis tag fields)))
        (<= 0xA0 code 0xAF)
        (let [st (.lookup-cache bis (.-struct-cache bis) (- code (.-STRUCT_CACHE_PACKED_START c/codes)))]
          (.handle-struct bis (.-tag st) (.-fields st)))
        (== code (.-STRUCT c/codes))
        (let [st (.lookup-cache bis (.-struct-cache bis) (p/read-int! bis))]
          (.handle-struct bis (.-tag st) (.-fields st)))
        (<= 0x80 code 0x9F)
        (.lookup-cache bis (.-priority-cache bis) (- code (.-PRIORITY_CACHE_PACKED_START c/codes)))
        (== code (.-PUT_PRIORITY_CACHE c/codes))
        (.read-and-cache-object bis (.-priority-cache bis))
        (== code (.-GET_PRIORITY_CACHE c/codes))
        (.lookup-cache bis (.-priority-cache bis) (p/read-int! bis))
        (== code (.-RESET-CACHES c/codes))
        (do
          (.clear-caches! bis)
          (p/read-object! bis))
        (== code (.-FOOTER c/codes))
        (let [calculated-length (dec (bsp/bytes-read bis))
              magic-from-stream (+ (bit-shift-right-zero-fill (bit-shift-left code 24) 0) (bsp/read-int24! bis))]
          (do
            (.validate-footer! bis calculated-length magic-from-stream)
            (bsp/reset! bis)
            (.clear-caches! bis)
            (p/read-object! bis)))
        (== code (.-FLOAT c/codes)) (bsp/read-float! bis)
        (== code (.-DOUBLE_0 c/codes)) 0.0
        (== code (.-DOUBLE_1 c/codes)) 1.0
        (== code (.-DOUBLE c/codes)) (bsp/read-double! bis))))
  (validate-footer! [bis]
    (let [calculated-length (bsp/bytes-read bis)
          magic-from-stream (bsp/read-unsigned-int32! bis)]
      (do
        (.validate-footer! bis calculated-length magic-from-stream)
        (bsp/reset! bis)
        (.clear-caches! bis)))))

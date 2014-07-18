(ns longshi.fressian.utils
  "Utility data and functions for byte arrays")

(deftype StringWriter [code-pipe string-buffer]
  Object
  (string-to-codes [_ s start-pos end-pos]
    (loop [i 0 str-pos start-pos]
      (when (< str-pos end-pos)
        (do
          (aset code-pipe i (.charCodeAt s str-pos))
          (recur (inc i) (inc str-pos))))))

 (write-str-bytes [_ str-length buf-length]
   (loop [i 0 buf-pos 0]
     (let [code (aget code-pipe i)
           byte-size (cond (<= code 0x007f) 1 (> code 0x07ff) 3 :else 2)]
       (if (and (< i str-length) (<= (+ buf-pos byte-size) buf-length))
         (do
           (cond
             (== byte-size 1)
             (aset string-buffer buf-pos code)
             (== byte-size 2)
             (do
                 (aset string-buffer buf-pos (bit-or (bit-and (bit-shift-right code 6) 0x1f) 0xc0))
                 (aset string-buffer (inc buf-pos) (bit-or (bit-and (bit-shift-right code 0) 0x3f) 0x80)))
             (== byte-size 3)
            (do
                (aset string-buffer buf-pos (bit-or (bit-and (bit-shift-right code 12) 0x0f) 0xe0))
                (aset string-buffer (inc buf-pos) (bit-or (bit-and (bit-shift-right code 6) 0x3f) 0x80))
                (aset string-buffer (+ 2 buf-pos) (bit-or (bit-and (bit-shift-right code 0) 0x3f) 0x80))))
          (recur (inc i) (+ buf-pos byte-size)))
          #js [i buf-pos]))))

  (string-chunk-utf8
    [sw s start total-str-length buffer]
    (let [str-length (Math/min 21846 (- total-str-length start))
          buf-length (Math/min 65536 (* 3 str-length))]
      (do
        (.string-to-codes sw s start (+ start str-length))
        (.write-str-bytes sw str-length buf-length))))

  (get-buffer [_] string-buffer))

(defn make-string-writer []
  (StringWriter. (make-array 16384) (js/Uint8Array. 65536)))

(def ^{:doc "Marker for the endianess of bytestreams"}
  little-endian false)

(defn make-byte-array [n]
  "Creates a unsigned bytes array of size n

  n (int) - The size of the byte array to be created"
  (js/Uint8Array. n))

(defn make-data-view [ba]
  "Creates a data view from a typed array

  ba (typed array) - Typed array to get a data view from"
  (js/DataView. (.-buffer ba)))

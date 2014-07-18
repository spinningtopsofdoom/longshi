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

(deftype StringReader [buffer read-codes]
  Object
  (bytes-to-codes [_ offset length]
    (loop [i 0 pos offset]
      (if (< pos length)
        (let [ch (aget buffer pos)]
          (let [bsch (bit-shift-right ch 4)]
            (cond
              (<= 0 bsch 7)
              (do
                (aset read-codes i ch)
                (recur (inc i) (inc pos)))
              (<= 12 bsch 13)
              (let [ch1 (aget buffer (inc pos))]
                (do
                  (aset read-codes i
                    (bit-or (bit-and ch1 0x3f) (bit-shift-left (bit-and ch 0x1f) 6)))
                  (recur (inc i) (+ 2 pos))))
              (== bsch 14)
              (let [ch1 (aget buffer (inc pos))
                    ch2 (aget buffer (+ 2 pos))]
                (do
                  (aset read-codes i
                    (bit-or
                      (bit-and ch2 0x3f)
                      (bit-shift-left (bit-and ch1 0x3f) 6)
                      (bit-shift-left (bit-and ch 0x0f) 12)))
                  (recur (inc i) (+ 3 pos))))
              :else
              (throw (js/Error. (str "Invalid UTF Character (" ch ")"))))))
        i)))

  (chars-to-str [_ string-buffer chars-length]
    (loop [i 0]
      (when (< i chars-length)
        (do
          (.append string-buffer (.fromCharCode js/String (aget read-codes i)))
          (recur (inc i))))))

  (get-buffer [_] buffer)

  (read-utf8-chars [sr string-buffer offset length]
    (let [str-length (.bytes-to-codes sr offset length)]
      (.chars-to-str sr string-buffer str-length))))

(defn make-string-reader []
  (StringReader. (js/Uint8Array. 65536) (make-array 21846)))

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

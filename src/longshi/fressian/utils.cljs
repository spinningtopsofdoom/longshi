(ns longshi.fressian.utils
  "Utility data and functions for byte arrays")

(deftype
  ^{:doc
    "Object for grouping string writing methods and data together

    code-pipe ([int]) - Pre allocated buffer for holding character codes
    string-buffer ([bytes]) - Pre allocated buffer holding the converted character codes"}
  StringWriter [code-pipe string-buffer]

  Object
  (string-to-codes [_ s start-pos end-pos]
    "Converts a string into an array of character codes

    s (string) - String to convert to character codes
    start-pos (int) - The starting position to start reading from the string
    end-pos (int) - The position to stop reading from the string"
    (loop [i 0 str-pos start-pos]
      (when (< str-pos end-pos)
        (aset code-pipe i (.charCodeAt s str-pos))
        (recur (inc i) (inc str-pos)))))

 (write-str-bytes [_ str-length buf-length]
   "Writes out character codes to bytes

    str-length (int) - The length of the portion of the string we are reading
    buf-length (int) - The length of the buffer we are writing to"
   (loop [i 0 buf-pos 0]
     (let [code (aget code-pipe i)
           byte-size (cond (<= code 0x007f) 1 (> code 0x07ff) 3 :else 2)]
       (if (and (< i str-length) (<= (+ buf-pos byte-size) buf-length))
         (do
           (case byte-size
             1
             (aset string-buffer buf-pos code)
             2
             (do
                 (aset string-buffer buf-pos (bit-or (bit-and (bit-shift-right code 6) 0x1f) 0xc0))
                 (aset string-buffer (inc buf-pos) (bit-or (bit-and (bit-shift-right code 0) 0x3f) 0x80)))
             3
            (do
                (aset string-buffer buf-pos (bit-or (bit-and (bit-shift-right code 12) 0x0f) 0xe0))
                (aset string-buffer (inc buf-pos) (bit-or (bit-and (bit-shift-right code 6) 0x3f) 0x80))
                (aset string-buffer (+ 2 buf-pos) (bit-or (bit-and (bit-shift-right code 0) 0x3f) 0x80))))
          (recur (inc i) (+ buf-pos byte-size)))
          #js [i buf-pos]))))

  (string-chunk-utf8 [sw s start total-str-length buffer]
    "Converts a string to a buffer with a maximum size of 64k

     s (string) - The string we are converting to bytes
     start (int) - The index to start reading from the string
     total-str-length (int) - The total length of the string
     buffer ([byte]) - The buffer we are writing to"
    (let [str-length (Math/min 21846 (- total-str-length start))
          buf-length (Math/min 65536 (* 3 str-length))]
      (.string-to-codes sw s start (+ start str-length))
      (.write-str-bytes sw str-length buf-length)))

  (get-buffer [_]
    "Gets the string buffer"
    string-buffer))

(defn make-string-writer
  "Convience method for making a string writer"
  []
  (StringWriter. (array) (js/Uint8Array. 65536)))

(deftype
  ^{:doc
    "Object for grouping string reading methods and data together

    buffer ([bytes]) - Pre allocated buffer for reading in string buffers
    read-codes ([int]) - Pre allocated buffer holding the character codes"}
  StringReader [buffer read-codes]

  Object
  (bytes-to-codes [_ offset length]
    "Converts string buffer to character codes

     offset (int) - Starting point to read the buffer
     length (int) - The number of bytes to read"
    (loop [i 0 pos offset]
      (if (< pos length)
        (let [ch (aget buffer pos)]
          (let [bsch (bit-shift-right ch 4)]
            (case bsch
              (1 2 3 4 5 6 7)
              (do
                (aset read-codes i ch)
                (recur (inc i) (inc pos)))
              (12 13)
              (let [ch1 (aget buffer (inc pos))]
                (aset read-codes i
                      (bit-or (bit-and ch1 0x3f) (bit-shift-left (bit-and ch 0x1f) 6)))
                (recur (inc i) (+ 2 pos)))
              14
              (let [ch1 (aget buffer (inc pos))
                    ch2 (aget buffer (+ 2 pos))]
                (aset read-codes i
                      (bit-or
                        (bit-and ch2 0x3f)
                        (bit-shift-left (bit-and ch1 0x3f) 6)
                        (bit-shift-left (bit-and ch 0x0f) 12)))
                (recur (inc i) (+ 3 pos)))
              (throw (js/Error. (str "Invalid UTF Character (" ch ")"))))))
        i)))

  (chars-to-str [_ string-buffer chars-length]
    "Converts character codes to characters and appends them to a string buffer

     string-buffer (StringBuffer) - Google String buffer to append characters to
     chars-length (int) - The number of characters we are appending"
    (loop [i 0]
      (when (< i chars-length)
        (.append string-buffer (.fromCharCode js/String (aget read-codes i)))
        (recur (inc i)))))

  (get-buffer [_]
    "Gets the string buffer"
    buffer)

  (read-utf8-chars [sr string-buffer offset length]
    "Reads in a string buffer and appends the characters to a string

     string-buffer (StringBuffer) Google string buffer we'll append characters to
     offset (int) - Starting point to read the string buffer
     length (int) - The number of bytes to read"
    (let [str-length (.bytes-to-codes sr offset length)]
      (.chars-to-str sr string-buffer str-length))))

(defn make-string-reader []
  "Convience method for making a string reader"
  (StringReader. (js/Uint8Array. 65536) (array)))

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

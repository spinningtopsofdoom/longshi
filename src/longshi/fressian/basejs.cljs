(ns longshi.fressian.basejs
  (:require [longshi.fressian.protocols :as p]
            [longshi.fressian.ByteStream :as bs]))


(def codes
  #js {
       :NULL 0xF7
       :TRUE 0xF5
       :FALSE 0xF6
       :DOUBLE 0xFA
       :DOUBLE_0 0xFB
       :DOUBLE_1 0xFC
       :INT_PACKED_2_ZERO 0x50
       :INT_PACKED_3_ZERO 0x68
       :INT_PACKED_4_ZERO 0x72
       :INT_PACKED_5_ZERO 0x76
       :INT_PACKED_6_ZERO 0x7A
       :INT_PACKED_7_ZERO 0x7E
       })

(def ^:private little-endian false)
;;Double buffer
(def ^:private da (js/Uint8Array. 8))
(def ^:private dadv (js/DataView. (.-buffer da)))
;;Integer constants
(def ^:private two-power-16 (bit-shift-left 1 16))
(def ^:private two-power-24 (bit-shift-left 1 24))
(def ^:private two-power-32 (* two-power-16 two-power-16))
;;Integer buffer
(def ^:private i64a (js/Uint8Array. 8))
(def ^:private i64adv (js/DataView. (.-buffer i64a)))
;;Integer helpers
(defn bit-switch [x]
  (case x
    0 64
    -1 64
    (if (neg? x)
      (- 66 (.-length (.toString x 2)))
      (- 64 (.-length (.toString x 2))))))

(defn is-int [x]
  (zero? (js-mod x 1)))

(extend-type bs/ByteOutputStream
  p/FressianWriter
  (writeNull! [bos] (p/write! bos (.-NULL codes)))
  (writeBoolean! [bos b] (p/write! bos (if b (.-TRUE codes) (.-FALSE codes))))
  (writeInt! [bos i]
    (let [bits (bit-switch i)]
      (case bits
        (15 16 17 18 19 20 21 22)
        (let [hb (bit-shift-right (bit-or (/ i two-power-24) 0) 8)
              ic (bit-shift-right hb 16)]
          (do
            (p/write! bos (+ (.-INT_PACKED_7_ZERO codes) ic))
            (.setInt16 i64adv 0 hb)
            (.setInt32 i64adv 2 i little-endian)
            (p/writeBytes! bos i64a 0 6)))
        (23 24 25 26 27 28 29 30)
        (let [hb (bit-shift-right (bit-or (/ i two-power-16) 0) 16)
              ic (bit-shift-right hb 8)]
          (do
            (p/write! bos (+ (.-INT_PACKED_6_ZERO codes) ic))
            (.setInt8 i64adv 0 hb)
            (.setInt32 i64adv 1 i little-endian)
            (p/writeBytes! bos i64a 0 5)))
        (31 32 33 34 35 36 37 38)
        (let [ic (bit-shift-right (bit-or (/ i two-power-16) 0) 16)]
          (do
            (p/write! bos (+ (.-INT_PACKED_5_ZERO codes) ic))
            (.setInt32 i64adv 0 i little-endian)
            (p/writeBytes! bos i64a 0 4)))
        (39 40 41 42 43 44)
        (do
          (p/write! bos (+ (.-INT_PACKED_4_ZERO codes) (bit-shift-right i 24)))
          (.setInt32 i64adv 0 i little-endian)
          (p/writeBytes! bos i64a 0 3))
        (45 46 47 48 49 50 51)
        (do
          (p/write! bos (+ (.-INT_PACKED_3_ZERO codes) (bit-shift-right i 16)))
          (.setInt16 i64adv 0 i little-endian)
          (p/writeBytes! bos i64a 0 2))
        (52 53 54 55 56 57)
        (do
          (p/write! bos (+ (.-INT_PACKED_2_ZERO codes) (bit-shift-right i 8)))
          (p/write! bos i))
        (58 59 60 61 62 63 64)
        (do
          (if (< -1 i)
          (p/write! bos (+ (.-INT_PACKED_2_ZERO codes) (bit-shift-right i 8))))
          (p/write! bos i)))))
  (writeDouble! [bos d]
    (case d
      0.0 (p/write! bos (.-DOUBLE_0 codes))
      1.0 (p/write! bos (.-DOUBLE_1 codes))
      (do
        (p/write! bos (.-DOUBLE codes))
        (.setFloat64 dadv 0 d little-endian)
        (p/writeBytes! bos da 0 8)))))

(extend-type bs/ByteInputStream
  p/FressianReader
  (readDouble! [bis]
     (p/readBytes! bis da 0 8)
     (.getFloat64 dadv 0 little-endian))
  (readObject! [bis]
    (let [code (p/read! bis)]
      (case code
        ((.-NULL codes)) nil
        ((.-TRUE codes)) true
        ((.-FALSE codes)) false
        0xFF -1
        (0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07
         0x08 0x09 0x0A 0x0B 0x0C 0x0D 0x0E 0x0F
         0x10 0x11 0x12 0x13 0x14 0x15 0x16 0x17
         0x18 0x19 0x1A 0x1B 0x1C 0x1D 0x1E 0x1F
         0x20 0x21 0x22 0x23 0x24 0x25 0x26 0x27
         0x28 0x29 0x2A 0x2B 0x2C 0x2D 0x2E 0x2F
         0x30 0x31 0x32 0x33 0x34 0x35 0x36 0x37
         0x38 0x39 0x3A 0x3B 0x3C 0x3D 0x3E 0x3F)
        (bit-and code 0xFF)
        (0x40 0x41 0x42 0x43 0x44 0x45 0x46 0x47
         0x48 0x49 0x4A 0x4B 0x4C 0x4D 0x4E 0x4F
         0x50 0x51 0x52 0x53 0x54 0x55 0x56 0x57
         0x58 0x59 0x5A 0x5B 0x5C 0x5D 0x5E 0x5F)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_2_ZERO codes)) 8)
          (p/read! bis))
        (0x60 0x61 0x62 0x63 0x64 0x65 0x66 0x67
         0x68 0x69 0x6A 0x6B 0x6C 0x6D 0x6E 0x6F)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_3_ZERO codes)) 16)
          (do
            (p/readBytes! bis i64a 0 2)
            (.getInt16 i64adv 0 little-endian)))
        (0x70 0x71 0x72 0x73)
        (bit-or
          (bit-shift-left (- code (.-INT_PACKED_4_ZERO codes)) 24)
          (do
            (p/readBytes! bis i64a 0 3)
            (aset i64a 3 0)
            (.getInt32 i64adv 0 little-endian)))
        (0x74 0x75 0x76 0x77)
        (let [i32 (do
                    (p/readBytes! bis i64a 0 4)
                    (.getUint32 i64adv 0 little-endian))]
          (case code
            0x74 (+ i32 (* -2 two-power-32))
            0x75 (+ i32 (* -1 two-power-32))
            0x76 i32
            0x77 (+ i32 two-power-32)))
        (0x78 0x79 0x7A 0x7B)
        (let [ih8 (p/read! bis)
              il32 (do
                    (p/readBytes! bis i64a 0 4)
                    (.getUint32 i64adv 0 little-endian))
              i40 (+ (* ih8 two-power-32) il32)]
          (case code
            0x78 (+ i40 (* -512 two-power-32))
            0x79 (+ i40 (* -256 two-power-32))
            0x7A i40
            0x7B (+ i40 (* 256 two-power-32))))
        (0x7C 0x7D 0x7E 0x7F)
        (let [ih16 (do
                    (p/readBytes! bis i64a 0 2)
                    (.getUint16 i64adv 0 little-endian))
              il32 (do
                    (p/readBytes! bis i64a 0 4)
                    (.getUint32 i64adv 0 little-endian))
              i48 (+ (* ih16 two-power-32) il32)]
          (case code
            0x7C (+ i48 (* -2 two-power-16 two-power-32))
            0x7D (+ i48 (* -1 two-power-16 two-power-32))
            0x7E i48
            0x7F (+ i48 (* two-power-16 two-power-32))))
        ((.-DOUBLE_0 codes)) 0.0
        ((.-DOUBLE_1 codes)) 1.0
        ((.-DOUBLE codes)) (p/readDouble! bis)))))
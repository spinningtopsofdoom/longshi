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
       })

(def ^:private little-endian false)

(def ^:private da (js/Uint8Array. 8))
(def ^:private dadv (js/DataView. (.-buffer da)))

(extend-type bs/ByteOutputStream
  p/FressianWriter
  (writeNull! [bos] (p/write! bos (.-NULL codes)))
  (writeBoolean! [bos b] (p/write! bos (if b (.-TRUE codes) (.-FALSE codes))))
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
        ((.-DOUBLE_0 codes)) 0.0
        ((.-DOUBLE_1 codes)) 1.0
        ((.-DOUBLE codes)) (p/readDouble! bis)))))
(ns longshi.fressian.basejs
  (:require [longshi.fressian.protocols :as p]
            [longshi.fressian.ByteStream :as bs]))


(def codes
  #js {
       :NULL 0xF7
       :TRUE 0xF5
       :FALSE 0xF6
       })

(extend-type bs/ByteOutputStream
  p/FressianWriter
  (writeNull! [bos] (p/write! bos (.-NULL codes)))
  (writeBoolean! [bos b] (p/write! bos (if b (.-TRUE codes) (.-FALSE codes)))))


(extend-type bs/ByteInputStream
  p/FressianReader
  (readObject! [bis]
    (let [code (p/read! bis)]
      (case code
        ((.-NULL codes)) nil
        ((.-TRUE codes)) true
        ((.-FALSE codes)) false))))
(ns longshi.fressian.utils)

(defn make-byte-array [n]
  (js/Uint8Array. n))

(defn make-data-view [ba]
  (js/DataView. (.-buffer ba)))

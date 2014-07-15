(ns longshi.fressian.utils
  "Utility data and functions for byte arrays")

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

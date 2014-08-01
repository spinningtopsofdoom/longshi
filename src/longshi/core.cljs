(ns longshi.core
  "Public API for the Fressian port of ClojureScript"
  (:refer-clojure :exclude (read))
  (:require [longshi.fressian.byte-stream-protocols :as bsp]
            [longshi.fressian.protocols :as p]
            [longshi.fressian.handlers :as fh]
            [longshi.fressian.byte-stream :as bs]
            [longshi.fressian.js :as bjs]))
;;Fressian Writer protocol namespace aliasing
(def write-footer p/write-footer!)
(def write-object p/write-object!)
(def write-tag p/write-tag!)
(def write-list p/write-list!)
(def write-boolean p/write-boolean!)
(def write-null p/write-null!)
(def write-int p/write-int!)
(def write-long p/write-long!)
(def write-float p/write-float!)
(def write-double p/write-double!)
(def write-bytes p/write-bytes!)
;;Fressian Reader protocol namespace aliasing
(def read-boolean p/read-boolean!)
(def read-int p/read-int!)
(def read-float p/read-float!)
(def read-double p/read-int!)
(def read-object p/read-object!)
;;Fressian Streaming protocol namespace aliasing
(def begin-open-list p/begin-open-list!)
(def begin-closed-list p/begin-closed-list!)
(def end-list p/end-list!)
;;Fressian Caching namespace aliasing
(def cache bjs/cache)
;;Fressian Tagged namespace aliasing
(def tagged-object fh/tagged-object)
(def tagged-array fh/tagged-array)

(defn write-record
  "Generic writer for ClojureScript Records

  writer (FressianWriter) - Fressian Writer for the record
  name (string) - Fressian name for the record
  record (object) - ClojureScript record to be written

  Currently the ClojureScript port of Fressian can't get the type name
  of ClojureScript records so the name of the record has to be passed in"
  [writer name record]
  (write-tag writer "record" 2)
  (write-object writer name true)
  (write-tag writer "map" 1)
  (begin-closed-list writer)
  (reduce-kv
    (fn [writer k v]
      (write-object writer k true)
      (write-object writer v))
    writer
    record)
  (end-list writer))

(defn map-write-handler [writer cljs-map]
  "Writes out a ClojureScript map to fressian

  writer (FressianWriter) - Fressian Writer for the record
  cljs-map (IMap) - ClojureScript map to be written

  Freesian maps are written internally as one list of alternating keys
  and values.  This method flattens the ClojureScript map sequence of key value
  pairs to a single list."
  (let [map-list (make-array (* 2 (count cljs-map)))]
    (do
      (write-tag writer "map" 1)
      (loop [map-seq (seq cljs-map) i 0]
        (if (empty? map-seq)
          map-list
          (let [[k v] (first map-seq)]
            (do
              (aset map-list i k)
              (aset map-list (inc i) v)
              (recur (rest map-seq) (+ i 2))))))
      (write-list writer map-list))))

(def list-write-handler
  {"list"
   (fn [writer cljs-seq]
     (do
      (write-tag writer "list" 1)
      (write-list writer cljs-seq)))})

(def vector-write-handler
  {"vector"
   (fn [writer cljs-seq]
     (do
      (write-tag writer "vector" 1)
      (write-list writer cljs-seq)))})

(def set-write-handler
  {"set"
   (fn [writer cljs-seq]
     (do
      (write-tag writer "set" 1)
      (write-list writer cljs-seq)))})
;;ClojureScript base writers
(def clojure-write-handlers
  {Keyword
   {"key"
    (fn [writer k]
      (do
        (write-tag writer "key" 2)
        (write-object writer (.-ns k) true)
        (write-object writer (.-name k) true)))}

   Symbol
   {"sym"
    (fn [writer k]
      (do
        (write-tag writer "sym" 2)
        (write-object writer (.-ns k) true)
        (write-object writer (.-name k) true)))}

   Range
   list-write-handler

   List
   list-write-handler

   Cons
   list-write-handler

   EmptyList
   list-write-handler

   LazySeq
   list-write-handler

   RSeq
   list-write-handler

   IndexedSeq
   list-write-handler

   ChunkedCons
   list-write-handler

   ChunkedSeq
   list-write-handler

   PersistentQueueSeq
   list-write-handler

   PersistentQueue
   list-write-handler

   PersistentArrayMapSeq
   list-write-handler

   PersistentTreeMapSeq
   list-write-handler

   NodeSeq
   list-write-handler

   ArrayNodeSeq
   list-write-handler

   KeySeq
   list-write-handler

   ValSeq
   list-write-handler

   PersistentHashMap
   {"map" map-write-handler}

   PersistentArrayMap
   {"map" map-write-handler}

   PersistentTreeMap
   {"map" map-write-handler}

   PersistentHashSet
   set-write-handler

   PersistentTreeSet
   set-write-handler

   PersistentVector
   vector-write-handler

   Subvec
   vector-write-handler
   })

(defn create-writer [& {:keys [handlers]}]
  "Creates a fressian writer

   Optional parameters;
     handlers (ILookup) - Lookup table for finding the appropiate handler for a given
                          type"
  (let [write-handlers (or handlers clojure-write-handlers)
        handler-seq (mapv (fn [[k v]] (into [k] (first v))) write-handlers)]
    (bs/byte-output-stream 32 (fh/create-handler handler-seq))))
(defn write
  "Convience function to write out a single object

  obj (object) - object to be wriiten out
  options (ILookup) - Optional paramters
    footer? (boolean) - Write out a footer after the object is written"
  [obj & options]
  (let [{:keys [footer?]} (when options (apply hash-map options))
        writer (apply create-writer options)]
    (do
      (write-object writer obj)
      (when footer?
        (write-footer writer))
      writer)))
;;ClojureScript base readers
(def clojure-read-handlers
  {"key"
   (fn [reader tag component-count]
     (keyword (read-object reader) (read-object reader)))
  "sym"
   (fn [reader tag component-count]
     (symbol (read-object reader) (read-object reader)))
   "char"
   (fn [reader tag component-count]
     (read-object reader))
   "byte"
   (fn [reader tag component-count]
     (read-object reader))
   "map"
   (fn [reader tag component-count]
     (cljs.core/PersistentArrayMap.fromArray (read-object reader)))
   "set"
   (fn [reader tag component-count]
     (cljs.core/PersistentHashSet.fromArray (read-object reader)))
   "list"
   (fn [reader tag component-count]
     (into '() (reverse (read-object reader))))
   "vector"
   (fn [reader tag component-count]
     (cljs.core/PersistentVector.fromArray (read-object reader)))})
(defn create-reader [input-stream & {:keys [handlers checksum?]}]
  "Creates a fressian reader

   input-stream ([byte]|ByteBuffer) - Array of bytes or byte buffer containg a fressian stream
   Optional parameters:
     handlers (ILookup) - Lookup table for the read handler of a given type
     checksum? (boolean) - Should the checksum be validated when the footer is validated"
  (let [byte-buffer (if (satisfies? bsp/ByteBuffer input-stream) (bsp/duplicate-bytes input-stream) input-stream)]
    (bs/byte-input-stream byte-buffer (or handlers clojure-read-handlers) (boolean checksum?))))
(defn read [readable & options]
  "Convience function to read in a single obecjt from a fressian stream

   readable ([byte]|ByteBuffer) - Array of bytes or byte buffer containg a fressian stream
   options (ILookup) - Optional parameters to be passed into the fressian reader"
  (read-object (apply create-reader readable options)))

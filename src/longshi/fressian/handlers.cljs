(ns longshi.fressian.handlers
  "Read and Write handlers and handler data structures"
  (:import [goog.math Long])
  (:require [longshi.fressian.protocols :as p]
            [longshi.fressian.utils :refer [make-byte-array make-data-view little-endian]]))

(def js-int-type #js{}) ;;Internal marker for JavaScript numbers that are integers
(def js-int-type-id (.getUid js/goog js-int-type)) ;;Marker code for js-int-type
(def js-number-type-id (.getUid js/goog js/Number))
(def js-string-type-id (.getUid js/goog js/String))
(def js-boolean-type-id (.getUid js/goog js/Boolean))
(def js-array-type-id (.getUid js/goog js/Array))

(deftype
  ^{:doc
    "Default value for read values that don't have a handler

    tag (string) - Name of type of value
    value ([Object]) - Object array containing value fields
    meta (IMap) - Value metadata (not currently used)"}
  TaggedObject [tag value meta]
  IPrintWithWriter
  (-pr-writer [_ writer _]
    (-write writer (str tag " : [" value "]"))))
(defn tagged-object [tag value]
  "Constructor for tagged object"
  (->TaggedObject tag value nil))
(deftype
  ^{:doc
    "Type for writitng out non native JavaScript Arrays

    tag (string) - Name of type of the array
    value (array) - Array containing values that are the same type

    This is for boolean and long arrays"}
  TaggedArray [tag value]
  IPrintWithWriter
  (-pr-writer [_ writer _]
    (-write writer (str tag " : [" value "]"))))
(defn tagged-array [tag value]
  "Constructor for tagged array"
  (->TaggedArray tag value))
(deftype
  ^{:doc
    "Lookup value for a fressian tag and handler

    tag (string) - Freesian value Name
    handler (IFn) - Handler for fressian data of the tags type"}
  TaggedHandler [tag handler]
  ILookup
  (-lookup [th k]
    (-lookup th k nil))
  (-lookup [_ k not-found]
    (if (= k tag) handler not-found)))

(deftype
  ^{:doc
    "Caching lookup for fressian write handlers

    handlers ([ILookup]) - Array of handler lookups
    cnt (int) - Number of handlers
    cache (Object) - Cache for quick handler lookup"}
  WriteLookup [handlers cnt ^:mutable cache]
  Object
  (require-write-handler [wl tag o]
    (let [js-type-id (cond
                       (nil? o) nil
                       (string? o) js-string-type-id
                       (number? o) (if (zero? (js-mod o 1)) js-int-type-id js-number-type-id)
                       (array? o) js-array-type-id
                       :else (.getUid js/goog (.-constructor o)))]
      (if-let [handler (-lookup wl js-type-id nil)]
        (if (or (nil? tag) (= tag (.-tag handler)) (= "any" (.-tag handler)))
          (.-handler handler)
          (throw (js/Error. (str "There is no handler for object (" o ") and tag (" tag ")"))))
        (throw (js/Error. (str "There is no handler for object (" o ")"))))))
  ILookup
  (-lookup
    [wl k]
    (-lookup wl k nil))
  (-lookup
    [wl k not-found]
    (if-let [cache-handler (aget cache k)]
      cache-handler
      (when-let [chain-handler
                 (loop [i 0]
                   (if-let [v (get (aget handlers i) k)]
                     v
                     (when (< i cnt)
                       (recur (inc i)))))]
        (do
          (aset cache k chain-handler)
          chain-handler)))))

(defn write-lookup
  "Constructor for WriteLookup

   handler-list (ISeq) - Sequence of fressian write handler"
  ([& handler-list]
   (let [handlers (into-array handler-list)]
     (WriteLookup. handlers (alength handlers) #js {}))))

(defn get-class-id
  "Converts a JavaScript constructor function to an unique id

  klass (IFn) - Constructor function"
  [klass]
  (when-not (nil? klass) (.getUid js/goog klass)))
(defn obj->lookup [obj]
  (reify ILookup
      (-lookup [obj k]
        (-lookup obj k nil))
      (-lookup [_ k not-found]
        (or (aget obj k) not-found))))
(defn create-handler
  "Creates lookup value for write handlers
  handler-data (ISeq) - Sequence of tuples to generate lookups with

  The handler data is of this form
  [[<constructor function> <tag name> <write handler>]]"
  [handler-data]
  (let [handlers #js {}]
    (doseq [h handler-data]
      (let [[js-type tag handler] h]
        (aset handlers (get-class-id js-type) (TaggedHandler. tag handler))))
    (obj->lookup handlers)))

(defn- int-array-write-handler
  "Write handler for JavaScript integer typed arrays

  fw (FreesianWriter) - Fressian write stream the integers will be written to
  ia (typed array) - Integer typed array to write to the fressian stream"
  [fw ia]
  (let [cnt (alength ia)]
    (do
      (p/write-tag! fw "int[]" 2)
      (p/write-int! fw cnt)
      (dotimes [i cnt]
        (p/write-int! fw (aget ia i))))))
(def
  ^{:doc
    "Write handlers for primitive types, arrays of primitive types, and TaggedObject"}
  core-write-handlers
  (create-handler
    [[nil "null" (fn [fw _] (p/write-null! fw))]
     [js/Boolean "bool" (fn [fw b] (p/write-boolean! fw b))]
     [js-int-type "int" (fn [fw i] (p/write-int! fw i))]
     [js/Number "double" (fn [fw d] (p/write-double! fw d))]
     [Long "int" (fn [fw l] (p/write-long! fw l))]
     [js/String "string" (fn [fw l] (p/write-string! fw l))]
     [js/Uint8Array "bytes" (fn [fw b] (p/write-bytes! fw b))]
     [js/Int8Array "bytes" (fn [fw b] (p/write-bytes! fw b))]
     [js/Uint16Array "int[]" int-array-write-handler]
     [js/Int16Array "int[]" int-array-write-handler]
     [js/Uint32Array "int[]" int-array-write-handler]
     [js/Int32Array "int[]" int-array-write-handler]
     [js/Float32Array "float[]"
      (fn [fw fa]
        (let [cnt (alength fa)]
          (do
            (p/write-tag! fw "float[]" 2)
            (p/write-int! fw cnt)
            (dotimes [i cnt]
              (p/write-float! fw (aget fa i))))))]
     [js/Float64Array "double[]"
      (fn [fw da]
        (let [cnt (alength da)]
          (do
            (p/write-tag! fw "double[]" 2)
            (p/write-int! fw cnt)
            (dotimes [i cnt]
              (p/write-double! fw (aget da i))))))]

     [js/Array "Object[]"
      (fn [fw oa]
        (let [cnt (alength oa)]
          (do
            (p/write-tag! fw "Object[]" cnt)
            (p/write-int! fw cnt)
            (dotimes [i cnt]
              (p/write-object! fw (aget oa i))))))]
     [TaggedObject "any"
      (fn [fw to]
        (let [cnt (alength (.-value to))]
          (do
            (p/write-tag! fw (.-tag to) cnt)
            (dotimes [i cnt]
              (p/write-object! fw (aget (.-value to) i))))))]

     [TaggedArray "any"
      (fn [fw ta]
        (let [cnt (alength (.-value ta))]
          (do
            (p/write-tag! fw (.-tag ta) 2)
            (p/write-int! fw cnt)
            (dotimes [i cnt]
              (p/write-object! fw (aget (.-value ta) i))))))]]))
;;UUID byte array for transformations from string to byte array
(def ^:private uuid-ba (make-byte-array 16))
(def ^:private uuid-dv (make-data-view uuid-ba))
;;Regex Alias since Google Closure can't optimize using js/RegExp
(def ^:private regex-alias (.-constructor #""))
(def
  ^{:doc
    "Extended freesian write handlers

    The handlers are for native javascript types (Date and RegExp) and native ClojureScript types (UUID)"}
  extended-write-handlers
  (create-handler
    [[js/Date "inst"
     (fn [fw d]
       (do
         (p/write-tag! fw "inst" 1)
         (p/write-int! fw (.getUTCMilliseconds d))))]
     [UUID "uuid"
     (fn [fw uuid]
       (do
         (p/write-tag! fw "uuid" 1)
         (let [uuid-str (.-uuid uuid)
               uuid-chunks (.match uuid-str (js/RegExp. "[0-9a-fA-F]{1,4}" "g"))]
           (do
             (dotimes [i (alength uuid-chunks)]
               (.setUint16 uuid-dv (* i 2) (js/parseInt (aget uuid-chunks i) 16) little-endian))
             (p/write-bytes! fw uuid-ba)))))]
     [regex-alias "regex"
     (fn [fw re]
       (do
         (p/write-tag! fw "regex" 1)
         (p/write-string! fw (.-source re))))]]))

(defn- int-check
  "Check if number is within int range (-2 ^ 32 to (2 ^ 32) - 1)

  i (number) - JavaScript number to check"
  [i]
  (do
    (when-not (and (number? i) (== i (bit-or i 0)))
      (throw (js/Error. (str "Value out of range for int: (" i ")"))))
      i))
(def
  ^{:doc
    "Read handlers for arrays of primitive types and extended types"}
  core-read-handlers
  (obj->lookup
    #js {
         "boolean[]"
         (fn [fr tag component-count]
           (let [size (int-check (p/read-int! fr))
                 ba (make-array size)]
             (do
               (dotimes [i size]
                 (aset ba i (p/read-boolean! fr)))
               ba)))
         "int[]"
         (fn [fr tag component-count]
           (let [size (int-check (p/read-int! fr))
                 ia (js/Int32Array. size)]
             (do
               (dotimes [i size]
                 (aset ia i (int-check (p/read-int! fr))))
               ia)))
         "float[]"
         (fn [fr tag component-count]
           (let [size (int-check (p/read-int! fr))
                 fa (js/Float32Array. size)]
             (do
               (dotimes [i size]
                 (aset fa i (p/read-float! fr)))
               fa)))
         "double[]"
         (fn [fr tag component-count]
           (let [size (int-check (p/read-int! fr))
                 da (js/Float64Array. size)]
             (do
               (dotimes [i size]
                 (aset da i (p/read-double! fr)))
               da)))
         "long[]"
         (fn [fr tag component-count]
           (let [size (int-check (p/read-int! fr))
                 la (make-array size)]
             (do
               (dotimes [i size]
                 (aset la i (p/read-int! fr)))
               la)))
         "Object[]"
         (fn [fr tag component-count]
           (let [size (int-check (p/read-int! fr))
                 oa (make-array size)]
             (do
               (dotimes [i size]
                 (aset oa i (p/read-object! fr)))
               oa)))
         "inst"
         (fn [fr tag component-count]
           (let [utc-millis (p/read-int! fr)]
             (js/Date. utc-millis)))
         "uuid"
         (fn [fr tag component-count]
           (let [uuid-bytes (p/read-object! fr)
                 uuid-chunks #js []]
             (do
               (dotimes [i 8]
                 (aset uuid-chunks  i (.toString (.getUint16 uuid-dv (* i 2) false) 16)))
               (UUID. (.replace (.join uuid-chunks "") #"(.{8})(.{4})(.{4})(.{4})(.{8})" "$1-$2-$3-$4-$5") nil))))
         "regex"
         (fn [fr tag component-count]
           (let [pattern (p/read-object! fr)]
             (js/RegExp. pattern)))
       }))

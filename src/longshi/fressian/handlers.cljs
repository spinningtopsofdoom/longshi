(ns longshi.fressian.handlers
  (:import [goog.math Long])
  (:require [longshi.fressian.protocols :as p]))

(def js-int-type #js{})
(def js-int-type-id (.getUid js/goog js-int-type))

(deftype TaggedObject [tag value meta])
(defn tagged-object [tag value]
  (->TaggedObject tag value nil))
(deftype TaggedHandler [tag handler])

(deftype WriteLookup [handlers cnt ^:mutable cache]
  Object
  (require-write-handler [wl tag o]
    (let [js-type-id (cond
                       (nil? o) nil
                       (and (number? o) (zero? (js-mod o 1))) js-int-type-id
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
                   (if-let [v (aget (aget handlers i) k)]
                     v
                     (when (< cnt i)
                       (recur (inc i)))))]
        (do
          (aset cache k chain-handler)
          chain-handler)))))

(defn write-lookup
  ([& handler-list]
   (let [handlers (into-array handler-list)]
     (WriteLookup. handlers (alength handlers) #js {}))))

(defn get-class-id [klass]
  (when-not (nil? klass) (.getUid js/goog klass)))
(defn create-handler [handler-data]
  (let [handlers #js {}]
    (doseq [h handler-data]
      (let [[js-type tag handler] h]
        (aset handlers (get-class-id js-type) (TaggedHandler. tag handler))))
    (.freeze js/Object handlers)))

(defn- int-array-write-handler [fw ia]
  (let [cnt (alength ia)]
    (do
      (p/write-tag! fw "int[]" 2)
      (p/write-int! fw cnt)
      (dotimes [i cnt]
        (p/write-int! fw (aget ia i))))))
(def core-write-handlers
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
            (p/write-int! fw cnt)
            (dotimes [i cnt]
              (p/write-object! fw (aget (.-value to) i))))))]
     ]))

(defn- int-check [i]
  (do
    (when-not (and (number? i) (== i (bit-or i 0)))
      (throw (js/Error. (str "Value out of range for int: (" i ")"))))
      i))
(def core-read-handlers
  (.freeze js/Object
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
       }))

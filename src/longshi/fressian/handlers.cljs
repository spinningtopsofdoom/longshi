(ns longshi.fressian.handlers
  (:import [goog.math Long])
  (:require [longshi.fressian.protocols :as p]))

(def js-int-type #js{})
(def js-int-type-id (.getUid js/goog js-int-type))
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

(def core-write-handlers
  (create-handler
    [[nil "null" (fn [fw _] (p/write-null! fw))]
     [js/Boolean "bool" (fn [fw b] (p/write-boolean! fw b))]
     [js-int-type "int" (fn [fw i] (p/write-int! fw i))]
     [js/Number "double" (fn [fw d] (p/write-double! fw d))]
     [Long "int" (fn [fw l] (p/write-long! fw l))]
     [js/String "string" (fn [fw l] (p/write-string! fw l))]
     [js/Uint8Array "bytes" (fn [fw b] (p/write-bytes! fw b))]
     [js/Int8Array "bytes" (fn [fw b] (p/write-bytes! fw b))]]))

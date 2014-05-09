(ns examples.hop-map
  (:require [longshi.fressian.hop-map :as h]))

(def int-seq (range 0 500000))

(def hop-map (h/interleaved-index-hop-map 1024))

(defn hop-lookup-test [hop-map test-seq]
    (doseq [i test-seq]
      (when-not (== i (.get hop-map (.toString i)))
        (println (.get hop-map (.toString i)))
        (throw (js/Error. (str "(" i ") was not found"))))))

(defn hop-test [hop-map test-seq]
    (doseq [i test-seq]
      (.intern! hop-map (.toString i))))

(enable-console-print!)
(simple-benchmark [int-seq int-seq] (hop-test hop-map int-seq) 1)
(simple-benchmark [int-seq int-seq] (hop-test hop-map int-seq) 1)
(simple-benchmark [int-seq int-seq] (hop-lookup-test hop-map int-seq) 1)
(.old-index! hop-map (.toString "400000")) ;; 400000
(.old-index! hop-map (.toString "500000")) ;; -1
(.old-index! hop-map (.toString "500000")) ;; 500001
(.get hop-map (.toString "1")) ;; 1
(.empty? hop-map) ;; false
(.clear! hop-map)
(.empty? hop-map) ;; true
(.get hop-map (.toString "1")) ;; -1

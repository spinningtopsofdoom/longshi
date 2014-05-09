(ns longshi.fressian.hop-map)
;; map from a value to a 32 bit integer
(deftype InterleavedIndexHopMap [^:mutable cap ^:mutable cnt ^:mutable hop-idx ^:mutable ks]
  Object
  (hash [_ obj]
     (let [h (hash obj)]
       (if (== h 0)
         42
         h)))
  (find-slot [_ h]
    (let [mask (dec cap)
          bkt (bit-and h mask)
          bhash (aget hop-idx (bit-shift-left bkt 2))]
      (if (zero? bhash)
        (bit-shift-left bkt 2)
        (loop [bkt bkt]
          (if (zero? (aget hop-idx (+ 2 (bit-shift-left bkt 2))))
            (+ 2 (bit-shift-left bkt 2))
            (recur (bit-and mask (inc bkt))))))))
  (resize [this]
    (let [old-hop-idx hop-idx
          old-hop-idx-len (.-length old-hop-idx)
          old-keys ks]
      (do
        (set! hop-idx (js/Int32Array. (* 2 (.-length hop-idx))))
        (set! ks (.concat ks (js/Array. cap)))
        (set! cap (bit-shift-left cap 1))
        (loop [slot 0]
          (when (< slot old-hop-idx-len)
            (let [new-slot (.find-slot this (aget old-hop-idx slot))]
              (do
                (.set hop-idx (.subarray old-hop-idx slot (+ 2 slot)) new-slot)
                (recur (+ 2 slot)))))))))
  (intern! [this k]
    (let [h (.hash this k)
          mask (dec cap)
          bkt (bit-and h mask)
          bhash (aget hop-idx (bit-shift-left bkt 2))]
      (if (== 0 bhash)
        (let [slot (bit-shift-left bkt 2)
              i cnt]
            (do
              (.set hop-idx (array h i) slot)
              (aset ks i k)
              (set! cnt (inc cnt))
              (when (== cnt cap)
                (.resize this))
              i))
        (if (== h bhash)
          (let [i (aget hop-idx (inc (bit-shift-left bkt 2)))
                bkey (aget ks i)]
            (when (= k bkey)
              i))
          (loop [bkt bkt]
            (let [bhash (aget hop-idx (+ 2 (bit-shift-left bkt 2)))]
              (if-not (== 0 bhash)
                (if (== h bhash)
                  (let [i (aget hop-idx (+ 3 (bit-shift-left bkt 2)))
                        bkey (aget ks i)]
                    (if (= k bkey)
                      i
                      (recur (bit-and (inc bkt) mask))))
                  (recur (bit-and (inc bkt) mask)))
                (let [slot (+ 2 (bit-shift-left bkt 2))
                      i cnt]
                    (do
                      (.set hop-idx (array h i) slot)
                      (aset ks i k)
                      (set! cnt (inc cnt))
                      (when (== cnt cap)
                        (.resize this))
                      i)))))))))
  (old-index! [this k]
    (let [cnt-before cnt
          idx (.intern! this k)]
      (if (== cnt cnt-before)
        idx
        -1)))
  (clear! [_]
    (let [cap2 (bit-shift-left cap 2)]
     (do
      (set! cnt 0)
      (loop [n 0]
        (when (< n cap)
          (do
            (aset ks n nil)
            (recur (inc n))))))
      (loop [n 0]
        (when (< n cap2)
          (do
            (aset hop-idx n 0)
            (recur (inc n)))))))
  (get [this k]
    (let [h (.hash this k)
          mask (dec cap)
          bkt (bit-and h mask)
          bhash (aget hop-idx (bit-shift-left bkt 2))]
      (if (== 0 bhash)
        -1
        (if (== h bhash)
          (let [i (aget hop-idx (inc (bit-shift-left bkt 2)))
                bkey (aget ks i)]
            (when (= k bkey)
              i))
          (loop [bkt bkt]
            (let [bhash (aget hop-idx (+ 2 (bit-shift-left bkt 2)))]
              (if-not (== 0 bhash)
                (if (== h bhash)
                  (let [i (aget hop-idx (+ 3 (bit-shift-left bkt 2)))
                        bkey (aget ks i)]
                    (if (= k bkey)
                      i
                      (recur (bit-and (inc bkt) mask))))
                  (recur (bit-and (inc bkt) mask)))
                -1)))))))
  (empty? [_]
    (== 0 cnt))
  ICounted
  (-count [_] cnt))

(defn interleaved-index-hop-map
  ([] (interleaved-index-hop-map 1024))
  ([capacity]
    (let [cap
           (loop [cap 1]
             (if (< cap capacity)
               (recur (bit-shift-left cap 1))
               cap))
          hop-idx (js/Int32Array. (bit-shift-left cap 2))
          ks (js/Array. cap)]
      (->InterleavedIndexHopMap cap 0 hop-idx ks))))

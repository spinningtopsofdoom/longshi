(ns longshi.fressian.hop-map
  "Caching map optimized for converting a value to an integer")
;; map from a value to a 32 bit integer
(deftype
  ^{:doc
    "Caching map that maps keys to positive integers

    cap (int) - Capacity of the map.  When it's full the map will resize
    cnt (int) - Number of items in the map
    hop-idx (Uint32Array) - Array of object hashes
    ks ([Object]) Array of keys for the map"}
  InterleavedIndexHopMap [^:mutable cap ^:mutable cnt ^:mutable hop-idx ^:mutable ks]
  Object
  (hash [_ obj]
    "Gets a hash of an Object

     The hash is an unsigned 32 bit integer"
     (let [h (bit-shift-right-zero-fill (hash obj) 0)]
       (if (== h 0)
         42
         h)))
  (find-slot [_ h]
    "Finds a new slot in hop-idx during resize"
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
    "Resizes the map"
    (let [old-hop-idx hop-idx
          old-hop-idx-len (.-length old-hop-idx)
          old-keys ks]
      (set! hop-idx (js/Uint32Array. (* 2 (.-length hop-idx))))
      (set! ks (.concat ks (js/Array. cap)))
      (set! cap (bit-shift-left cap 1))
      (loop [slot 0]
        (when (< slot old-hop-idx-len)
          (let [new-slot (.find-slot this (aget old-hop-idx slot))]
            (.set hop-idx (.subarray old-hop-idx slot (+ 2 slot)) new-slot)
            (recur (+ 2 slot)))))))
  (intern! [this k]
    "Associates the key with a positive integer

     k (object) - Value to be mapped to a positive integer

     If k is in the map the positive integer it's assigned to is returned.  Otherwise
     k is associated with the current value of cnt and cnt is incremented."
    (let [h (.hash this k)
          mask (dec cap)
          bkt (bit-and h mask)
          bhash (aget hop-idx (bit-shift-left bkt 2))]
      (if (== 0 bhash)
        (let [slot (bit-shift-left bkt 2)
              i cnt]
          (.set hop-idx (array h i) slot)
          (aset ks i k)
          (set! cnt (inc cnt))
          (when (== cnt cap)
            (.resize this))
          i)
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
                  (.set hop-idx (array h i) slot)
                  (aset ks i k)
                  (set! cnt (inc cnt))
                  (when (== cnt cap)
                    (.resize this))
                  i))))))))
  (old-index! [this k]
    "Associates the key with a positive integer

     k (object) - Value to be mapped to a positive integer

     If k is in the map then the positive integer associated with it is returned.  Otherwise
     k is added to the map and -1 is returned."
    (let [cnt-before cnt
          idx (.intern! this k)]
      (if (== cnt cnt-before)
        idx
        -1)))
  (clear! [_]
    "Removes all associations from the map and sets cnt to zero"
    (let [cap2 (bit-shift-left cap 2)]
      (set! cnt 0)
      (loop [n 0]
        (when (< n cap)
          (aset ks n nil)
          (recur (inc n))))
      (loop [n 0]
        (when (< n cap2)
          (aset hop-idx n 0)
          (recur (inc n))))))
  (get [this k]
    "Get the value of key or -1 if it isn't in the map"
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
  "Constructor for InterleavedIndexHopMap

  capacity (int) - The capacity of the map (defauls to 1024)"
  ([] (interleaved-index-hop-map 1024))
  ([capacity]
    (let [cap
           (loop [cap 1]
             (if (< cap capacity)
               (recur (bit-shift-left cap 1))
               cap))
          hop-idx (js/Uint32Array. (bit-shift-left cap 2))
          ks (js/Array. cap)]
      (InterleavedIndexHopMap. cap 0 hop-idx ks))))

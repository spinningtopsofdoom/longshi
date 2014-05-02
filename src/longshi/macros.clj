(ns longshi.macros)

(defmacro local
  ([]
    `(make-array 1))
  ([x]
    `(cljs.core/array ~x)))

(defmacro set-local [x v]
  `(aset ~x 0 ~v))

(defmacro get-local [x]
  `(aget ~x 0))

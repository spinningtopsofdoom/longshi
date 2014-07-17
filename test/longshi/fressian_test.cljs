(ns longshi.fressian-test
  (:import [goog.math Long])
  (:require [longshi.fressian.byte-stream-protocols :as bsp]
            [longshi.core :as fress]))

(enable-console-print!)

(println "Longshi Fressian tests")

(extend-type Long
  IEquiv
  (-equiv [this other] (.equals this other)))

(defn run-tests []
  (do
    ;;Nil checks
    (assert (= nil (fress/read (fress/write nil))))
    ;;boolean checks
    (assert (= true (fress/read (fress/write true))))
    (assert (= false (fress/read (fress/write false))))
    ;;Integer checks
    (assert (= 0 (fress/read (fress/write 0))))
    (assert (= 32768 (fress/read (fress/write 32768))))
    (assert (= 2147483647 (fress/read (fress/write 2147483647))))
    (assert (= 8589934591 (fress/read (fress/write 8589934591))))
    (assert (= 2199023255551 (fress/read (fress/write 2199023255551))))
    (assert (= 281474976710655 (fress/read (fress/write 281474976710655))))
    (assert (= 9007199254740991 (fress/read (fress/write 9007199254740991))))
    (assert (= -2 (fress/read (fress/write -2))))
    (assert (= -32768 (fress/read (fress/write -32768))))
    (assert (= -2147483648 (fress/read (fress/write -2147483648))))
    (assert (= -8589934592 (fress/read (fress/write -8589934592))))
    (assert (= -2199023255552 (fress/read (fress/write -2199023255552))))
    (assert (= -562949953421312 (fress/read (fress/write -562949953421312))))
    (assert (= -9007199254740991 (fress/read (fress/write -9007199254740991))))
    ;;Double checks
    (assert (= 1.0 (fress/read (fress/write 1.0))))
    (assert (= 0.0 (fress/read (fress/write 0.0))))
    (assert (= 3.1415 (fress/read (fress/write 3.1415))))
    ;;Long checks
    (assert (= (Long.fromString "9223372036854775807") (fress/read (fress/write (Long.fromString "9223372036854775807")))))
    (assert (= (Long.fromString "-9223372036854775808") (fress/read (fress/write (Long.fromString "-9223372036854775808")))))
    ;;String checks
    (assert (= "A man a plan panama" (fress/read (fress/write "A man a plan panama"))))
    (assert (= "Now is the time for all good men" (fress/read (fress/write "Now is the time for all good men"))))
    ;;ClojureScript data structures checks
    (assert (= {:city "Leng" :temp 104.5 :pop 3000} (fress/read (fress/write {:city "Leng" :temp 104.5 :pop 3000}))))
    (assert (= #{:red :blue :green} (fress/read (fress/write #{:red :blue :green}))))
    (assert (= [42 "quiz" :notes] (fress/read (fress/write [42 "quiz" :notes]))))
    ))
(run-tests)

(println "test completed")

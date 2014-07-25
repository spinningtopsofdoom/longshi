(ns examples.core
  "Examples using the public api"
  (:require [longshi.core :as fress]))
;;Showing the awesomeness of fressian's domain aware caching

;;UUID creator gotten from https://github.com/davesann/cljs-uuid
(defn make-random-uuid
  "Returns a new randomly generated (version 4) cljs.core/UUID,
like: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
as per http://www.ietf.org/rfc/rfc4122.txt.
Usage:
(make-random) => #uuid \"305e764d-b451-47ae-a90d-5db782ac1f2e\"
(type (make-random)) => cljs.core/UUID"
  []
  (letfn [(f [] (.toString (rand-int 16) 16))
          (g [] (.toString (bit-or 0x8 (bit-and 0x3 (rand-int 15))) 16))]
    (UUID. (.toString
             (goog.string.StringBuffer.
               (f) (f) (f) (f) (f) (f) (f) (f) "-" (f) (f) (f) (f)
               "-4" (f) (f) (f) "-" (g) (f) (f) (f) "-"
               (f) (f) (f) (f) (f) (f) (f) (f) (f) (f) (f) (f))))))

;;Our user type
(defrecord User [id slug first-name last-name])
(defn user [first-name last-name]
  (->User (make-random-uuid) (.toLowerCase (str first-name "-" last-name)) first-name last-name))
;;Get the users id
(def get-user-id #(:id %))
;;Get the users id but with cachiness!
(def get-cached-user-id #(fress/cache (:id %)))

;;Super powerful users
(def creator (user "Matt" "Greoning"))
(def admin (user "Super" "User"))
;;Simpsons
(def homer (user "Homer" "Simpson"))
(def marge (user "Marge" "Simpson"))
(def lisa (user "Lisa" "Simpson"))
(def bart (user "Bart" "Simpson"))
(def maggie (user "Maggie" "Simpson"))
;;Flanders
(def ned (user "Ned" "Flanders"))
(def maude (user "Maude" "Flanders"))
(def rod (user "Rod" "Flanders"))
(def todd (user "Todd" "Flanders"))
;;Powerplant people
(def burns (user "Montgomery" "Burns"))
(def smithers (user `"Wallen" "Smithers"))
;;All of or users
(def users [creator
            admin
            homer
            marge
            lisa
            bart
            maggie
            ned
            maude
            rod
            todd
            burns
            smithers
            ])
;;Relationships the users have
(def relationships
  {
   :all-powerful [creator admin]
   :works-for
   [{(get-user-id burns) [(get-user-id homer) (get-user-id smithers)]}
    {(get-user-id new) [(get-user-id ned)]}]
   :has-catch-phrase
   [homer marge lisa bart maggie burns]
   :works-at-power-plant
   [homer smithers burns]
   :last-names
   (distinct (map #(:last-name %) users))})
;;The basic write handler with no caching
(def write-handler
  {User
   {
    "examples.core/User"
    (fn [writer user]
        (fress/write-tag writer "examples.core/User" 4) ;;Write the types name and the number of fields
        (fress/write-object writer (:id user)) ;;The uuid idenifier should be cached
        (fress/write-object writer (:name user))
        (fress/write-object writer (:first-name user))
        (fress/write-object writer (:last-name user)) ;;Also the last name should be cached
      )}})

;;Write handler with field caching
(def cache-write-handler
  {User
   {
    "examples.core/User"
    (fn [writer user]
        (fress/write-tag writer "examples.core/User" 4) ;;Write the types name and the number of fields
        (fress/write-object writer (:id user) true) ;;The uuid idenifier should be cached
        (fress/write-object writer (:slug user))
        (fress/write-object writer (:first-name user))
        (fress/write-object writer (:last-name user) true) ;;Also the last name should be cached
      )}})
;;Making all the user objects cached objects
(def cached-users (mapv #(fress/cache %) users))
;;Caching everything possible in the relationships
(def cached-relationships
  {
   :all-powerful
   (mapv #(fress/cache %) [creator admin])
   :works-for
   [{(get-cached-user-id burns) [(get-cached-user-id homer) (get-cached-user-id smithers)]}
    {(get-cached-user-id ned) [(get-cached-user-id ned)]}]
   :has-catch-phrase
   (mapv #(fress/cache %) [homer marge lisa bart maggie burns])
   :works-at-power-plant
   (mapv #(fress/cache %) [homer smithers burns])

   :last-names
   (mapv #(fress/cache %) (distinct (map #(:last-name %) users)))})

(enable-console-print!)
;;Get the byte sizes for edn, basic fressian and the various levels of caching
;;Note - Since the data we are using is relatively small the caching writer actually increases the size.
;;       With bigger payloads and writing more than two values in a stream the caching will pay off sigificantly
(def byte-size-comparison
  (let [non-caching-writer (fress/create-writer :handlers (merge fress/clojure-write-handlers write-handler))
        caching-writer (fress/create-writer :handlers (merge fress/clojure-write-handlers cache-write-handler))
        caching-writer-objs (fress/create-writer :handlers (merge fress/clojure-write-handlers cache-write-handler))]
    {
     :edn
     (+ (alength (prn-str users)) (alength (prn-str relationships)))
     :basic-fressian
     (count
       (do
         (fress/write-object non-caching-writer users)
         (fress/write-object non-caching-writer relationships)))
     :caching-writer-fressian
     (count
       (do
         (fress/write-object caching-writer users)
         (fress/write-object caching-writer relationships)))
     :caching-writer-and-objects-fressian
     (count
       (do
         (fress/write-object caching-writer-objs cached-users)
         (fress/write-object caching-writer-objs cached-relationships)))}))
(println "Byte size comparisons")
(println byte-size-comparison)

(let [non-caching-writer (fress/create-writer :handlers (merge fress/clojure-write-handlers write-handler))
        caching-writer (fress/create-writer :handlers (merge fress/clojure-write-handlers cache-write-handler))
        caching-writer-objs (fress/create-writer :handlers (merge fress/clojure-write-handlers cache-write-handler))]
     (count
       (do
         (fress/write-object caching-writer users)
         (fress/write-object caching-writer users)
         (fress/write-object caching-writer relationships))))

(println "Original ClojureScript objects")
(println [users relationships])
;;Fressain Read Handler the fields order for the user type is determined by the writer
(def read-handler
  {"examples.core/User"
   (fn [reader tag component-count]
     (->User
         (fress/read-object reader) ;;UUID - id
         (fress/read-object reader) ;;String - slug
         (fress/read-object reader) ;;String - first-name
         (fress/read-object reader) ;;String - last-name
       ))})
;;How to read fressian streams
(def pass-through
  (let [caching-writer (fress/create-writer :handlers (merge fress/clojure-write-handlers cache-write-handler))]
    (do
      (fress/write-object caching-writer cached-users)
      (fress/write-object caching-writer cached-relationships)
      (let [reader (fress/create-reader caching-writer :handlers (merge fress/clojure-read-handlers read-handler))]
        [(fress/read-object reader) (fress/read-object reader)]))))

(println "Reading out the ClojureScript objects from the fressian stream")
(println pass-through)

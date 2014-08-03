(ns benchmark.perf
  (:require [longshi.core :as fress]
            [cljs.reader :as reader]
            [cognitect.transit :as t]
            ))

;;sanple data from https://github.com/MichaelDrogalis/traffic-sim/blob/master/resources/weighted-directions.edn
(def sample
  [{:lane/weight 5
  :lane/name "in-1"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["Juniper Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["Juniper Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-3"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["Juniper Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in"
  :street/tag "north"
  :street/name "Juniper Street"
  :intersection/of ["Juniper Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out"
  :street/tag "south"
  :street/name "Juniper Street"
  :intersection/of ["Juniper Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["Juniper Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-3"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["Juniper Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-3"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "south"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "south"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "out-1"
  :street/tag "north"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "out-2"
  :street/tag "north"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-3"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "north"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "north"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out-1"
  :street/tag "south"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out-2"
  :street/tag "south"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-3"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "south"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "south"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "out-1"
  :street/tag "north"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "out-2"
  :street/tag "north"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "east"
  :street/name "Walnut Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in-3"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name " in-2"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 2
  :lane/name "in-3"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out-1"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out-2"
  :street/tag "west"
  :street/name "Walnut Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out-1"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out-2"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 0
  :lane/name "out-3"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "out-3"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Walnut Street"]}
 {:lane/weight 5
  :lane/name "in"
  :street/tag "east"
  :street/name "Sansom Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "in-3"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in-3"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 0
  :lane/name "out"
  :street/tag "west"
  :street/name "Sansom Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "out-1"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "out-2"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out-3"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out-3"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-3"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-3"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-1"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-2"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-3"
  :street/tag "south"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-1"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-2"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-3"
  :street/tag "north"
  :street/name "Broad Street"
  :intersection/of ["Broad Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["Juniper Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["Juniper Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in"
  :street/tag "north"
  :street/name "Juniper Street"
  :intersection/of ["Juniper Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["Juniper Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["Juniper Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "out"
  :street/tag "south"
  :street/name "Juniper Street"
  :intersection/of ["Juniper Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["13th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["13th Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "south"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "south"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-1"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["13th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["13th Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out"
  :street/tag "north"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Chestnut Street"]}
 {:lane/weight 1
  :lane/name "in-1"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "north"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "north"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 1
  :lane/name "out-1"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 20
  :lane/name "out-1"
  :street/tag "south"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 20
  :lane/name "out-2"
  :street/tag "south"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "west"
  :street/name "Chestnut Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "south"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "south"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-1"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "out-2"
  :street/tag "east"
  :street/name "Chestnut Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-1"
  :street/tag "north"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 0
  :lane/name "out-2"
  :street/tag "north"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Chestnut Street"]}
 {:lane/weight 5
  :lane/name "in"
  :street/tag "east"
  :street/name "Sansom Street"
  :intersection/of ["11th Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "in-1"
  :street/tag "south"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "in-2"
  :street/tag "south"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out"
  :street/tag "west"
  :street/name "Sansom Street"
  :intersection/of ["11th Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "out-1"
  :street/tag "north"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "out-2"
  :street/tag "north"
  :street/name "11th Street"
  :intersection/of ["11th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in"
  :street/tag "east"
  :street/name "Sansom Street"
  :intersection/of ["12th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in-1"
  :street/tag "north"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in-2"
  :street/tag "north"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out"
  :street/tag "west"
  :street/name "Sansom Street"
  :intersection/of ["12th Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "out-1"
  :street/tag "south"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "out-2"
  :street/tag "south"
  :street/name "12th Street"
  :intersection/of ["12th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in"
  :street/tag "east"
  :street/name "Sansom Street"
  :intersection/of ["13th Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "in"
  :street/tag "south"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out"
  :street/tag "west"
  :street/name "Sansom Street"
  :intersection/of ["13th Street" "Sansom Street"]}
 {:lane/weight 0
  :lane/name "out"
  :street/tag "north"
  :street/name "13th Street"
  :intersection/of ["13th Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "in"
  :street/tag "east"
  :street/name "Sansom Street"
  :intersection/of ["Juniper Street" "Sansom Street"]}
 {:lane/weight 2
  :lane/name "in"
  :street/tag "north"
  :street/name "Juniper Street"
  :intersection/of ["Juniper Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out"
  :street/tag "west"
  :street/name "Sansom Street"
  :intersection/of ["Juniper Street" "Sansom Street"]}
 {:lane/weight 5
  :lane/name "out"
  :street/tag "south"
  :street/name "Juniper Street"
  :intersection/of ["Juniper Street" "Sansom Street"]}])

(enable-console-print!)

(println "EDN writing speed")
(simple-benchmark [x sample] (prn-str x) 100)

(println "EDN reading speed")
(simple-benchmark [x (prn-str sample)] (reader/read-string x) 100)

(println "Tranist writing speed")
(def tw (t/writer :json))
(simple-benchmark [x sample] (t/write tw x) 100)

(println "Tranist reading speed")
(def tr (t/reader :json))
(simple-benchmark [x (t/write tw sample)] (t/read tr x) 100)

(println "Fressian writing speed")
(simple-benchmark [x sample] (fress/write x) 100)

(println "Fressian reading speed")
(simple-benchmark [x (fress/write sample)] (fress/read x) 100)

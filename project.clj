(defproject longshi "0.1.3"
  :description "Fressian for ClojureScript"
  :url "http://github.com/spinningtopsofdoom/longshi"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2234" :scope "provided"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :jvm-opts ["-Xmx1g" "-XX:+UseConcMarkSweepGC"]
  :source-paths ["src"]
  :cljsbuild {
              :builds [{:id "benchmark"
                        :source-paths ["src" "benchmark"]
                        :compiler {:output-to "target/bench/longshi_bench.js"
                                   :optimizations :advanced}}
                       {:id "longshi"
                        :source-paths ["src"]
                        :compiler {:output-to "longshi.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :source-map true}}
                      {:id "tests"
                       :source-paths ["src" "test"]
                       :compiler {:optimizations :none
                                  :source-map true
                                  :output-dir "target/test"
                                  :output-to "target/test/longshi_test.js"}}]})

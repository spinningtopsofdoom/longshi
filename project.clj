(defproject longshi "0.1.4"
  :description "Fressian for ClojureScript"
  :url "http://github.com/spinningtopsofdoom/longshi"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]]
  :profiles {:bench {:dependencies [[com.cognitect/transit-cljs "0.8.188"]]}}
  :plugins [[lein-cljsbuild "1.0.3"]]
  :jvm-opts ["-Xmx1g" "-XX:+UseConcMarkSweepGC"]
  :source-paths ["src"]
  :cljsbuild {
              :builds [{:id "benchmark"
                        :source-paths ["src" "sample" "benchmark"]
                        :compiler {:output-to "target/bench/longshi_bench.js"
                                   :optimizations :advanced}}
                       {:id "perfomance"
                        :source-paths ["src" "sample" "perf"]
                        :compiler {:output-to "target/perf/longshi_perf.js"
                                   :optimizations :whitespace
                                   :source-map "target/perf/longshi_perf.js.map"
                                   :output-dir "target/perf"
                                   :static-fns true
                                   }}
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

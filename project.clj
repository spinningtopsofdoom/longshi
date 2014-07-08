(defproject longshi "0.1.0-SNAPSHOT"
  :description "Fressian for ClojureScript"
  :url "http://github.com/devn/longshi"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2202" :scope "provided"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :jvm-opts ["-Xmx1g" "-XX:+UseConcMarkSweepGC"]
  :source-paths ["src"]
  :cljsbuild {
              :builds [{:id "longshi"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "longshi.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :source-map true}}]})

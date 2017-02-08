(defproject com.codahale/guava-cache-clj "0.1.2"
  :description "A Clojure wrapper for Guava's caches."
  :url "https://github.com/codahale/guava-cache-clj"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-time "0.13.0"]
                 [com.google.guava/guava "21.0"]]
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev           [:project/dev :profiles/dev]
             :test          [:project/test :profiles/test]
             :profiles/dev  {:dependencies [[org.clojure/clojure "1.8.0"]
                                            [criterium "0.4.4"]]}
             :profiles/test {}
             :project/dev   {:source-paths ["dev"]
                             :repl-options {:init-ns user}}
             :project/test  {:dependencies []}})

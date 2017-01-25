# guava-cache-clj

A Clojure wrapper for Guava caches.

## Usage

```clojure
(require '[guava-cache-clj.core :as guava-cache])

(defn- expensive
  [k]
  ;; do something expensive based on `k` and return it
  (name k)
  )

(def cache (guava-cache/build expensive {:maximum-size 100}))

;; get or load the value for `:blah` (`"blah"`)
(cache :blah)

;; get the cached value for `:blah`, if any, or return 100
(cache :blah 100)
```

## License

Copyright © 2017 Coda Hale

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.

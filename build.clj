(ns build
  (:require [clojure.tools.build.api :as b]))

(def app 'app/laliga-fantasy-tool)
(def class-dir "target/classes")
(def uber-file (format "target/%s.jar" (name app)))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
  (clean nil)
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (println "Compiling...")
  (b/compile-clj {:basis      @basis
                  :ns-compile '[laliga-fantasy.main]
                  :class-dir  class-dir})
  (println "Building uberjar" (str uber-file "..."))
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis     @basis
           :main      'laliga-fantasy.main}))

(defproject djoko "0.1.0-SNAPSHOT"
  :description "A small terminal tennis game simulator written in Clojure."
  :url "https://github.com/gusutabo/djoko"
  :license {:name "MIT License"
          :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]]
  :main ^:skip-aot djoko.core
  :repl-options {:init-ns djoko.core})

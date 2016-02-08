(ns bohr.eval
  (:require [clojure.tools.logging :as log]))

(defn- wrap-with-parentheses [string]
  (str "(" string ")"))

(defn- string-to-list [string]
  (try
    (read-string string)
    (catch java.lang.RuntimeException e
      (throw
       (ex-info
        (format "Malformed input: %s" (.getMessage e))
        { :bohr true :type :bohr-malformed-input })))))

(defn- wrap-with-do [block]
  (conj block 'do))

(defn- setup-script-namespace []
  (binding [*ns* *ns*]
      (in-ns 'bohr.script)
      (refer-clojure)
      (require '[clojure.tools.logging :as log])
      (require '[clojure.string :as string])
      (require '[clj-time.core :as time])
      (require '[clj-time.format :as time-format])
      (require '[clojure.java.io :as io])
      (use 'clojure.java.shell)
      (use 'bohr.helpers)
      (use 'bohr.parsers)
      (use 'bohr.observers)
      (use 'bohr.notebook)
      (use 'bohr.journals)
      (use 'bohr.dsl)))

(defn- eval-in-script-namespace [form]
  (setup-script-namespace)
  (binding [*ns* *ns*]
    (in-ns 'bohr.script)
    (refer-clojure)
    (eval form)))

(defn eval-script-content! [dsl-string]
  (log/trace "Evaluating script")
  (-> dsl-string wrap-with-parentheses string-to-list wrap-with-do eval-in-script-namespace))
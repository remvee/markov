(defproject markov-chain "0.2.0"
  :description "A Clojure library to generate data using Markov chain probabilities."
  :url "https://git.sr.ht/~rwv/markov"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.generators "1.0.0"]]
  :repl-options {:init-ns remworks.markov-chain})

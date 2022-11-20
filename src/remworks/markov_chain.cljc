(ns remworks.markov-chain
  "Implementation of Markov Chain to generate, for instance, text."
  (:require #?(:clj [clojure.data.generators :as gen])
            [clojure.string :as s]))

(defn- add-word [s word]
  (update s word (fnil inc 0)))

(defn- merge-words [s1 s2]
  (merge-with + s1 s2))

(defn- analyse-line [words lookback]
  (reduce (fn [m i]
            (let [k (if (<= i lookback)
                      (->> words (take i))
                      (->> words (drop (- i lookback)) (take lookback)))]
              (update m (vec k) add-word (when (< i (count words))
                                           (nth words i)))))
          {}
          (range (+ (count words) lookback))))

(defn weighted
  "Pick a random value from a weighted set given a map for values and
  weights.  The CLJ implementation of this function uses
  `clojure.data.generators`, to get reproducible results use it's
  `*rnd*` binding ."
  [m]
  #?(:clj (gen/weighted m)
     :cljs ;; adapted from clojure.data.generators
     (let [weights (reductions + (vals m))
           total   (last weights)
           choices (map vector (keys m) weights)
           choice (.floor js/Math (* total (.random js/Math)))]
       (loop [[[c w] & more] choices]
         (when w
           (if (< choice w)
             c
             (recur more)))))))

(defn analyse
  "Analyse `corpus` and return a state space.  Option `lookback` determines the
  strength of the lookup key and defaults to 2.  `max-length` will be set to
  largest row length in the corpus if no value is supplied."
  [corpus & {:keys [lookback max-length] :or {lookback 2}}]
  {:space      (reduce (fn [m line]
                         (merge-with merge-words m
                                     (analyse-line line lookback)))
                       {}
                       corpus)
   :lookback   lookback
   :max-length (or max-length (apply max (map count corpus)))})

(defn generate
  "Generate new chains from given `state-space`.  When the generated
  chain exceeds `max-length` it will contain `::et-cetera` it the last
  position.  This function uses `weighted` to do its random picking."
  [{:keys [space lookback max-length]}]
  (loop [r [], n 0]
    (let [c (->> r reverse (take lookback) reverse)
          w (weighted (get space c))]
      (if (< n max-length)
        (if w
          (recur (conj r w) (inc n))
          r)
        (if w
          (into r [::et-cetera])
          r)))))

(defn split-sentences
  "Split `text` into sentences."
  [text]
  (map s/trim (re-seq #".*?[.?!](?:\s|$)" (s/replace text #"\s+" " "))))

(defn split-words
  "Split `sentence` into words."
  [sentence]
  (s/split sentence #"\s+"))

(defn analyse-text
  "Analyse `text` as a corpus of lines and words."
  [text & opts]
  (apply analyse (->> text split-sentences (map split-words)) opts))

(defn generate-text
  "Generate a new sentence from are `state-space`."
  [state-space]
  (->> state-space
       generate
       (map #(if (= % ::et-cetera) "…" %))
       (s/join " ")))

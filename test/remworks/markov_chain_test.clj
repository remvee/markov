(ns remworks.markov-chain-test
  (:require [clojure.data.generators :as gen]
            [clojure.string :as s]
            [clojure.test :refer [deftest is testing]]
            [remworks.markov-chain :as sut]))

(def sample-size 100)

(deftest analyse
  (testing "one step lookback"
    (is (= {:space      {[]  {1 1
                              2 1
                              3 1}
                         [1] {2 1}
                         [2] {3 1
                              4 1}
                         [3] {nil 1
                              5   1}
                         [4] {5 1}
                         [5] {nil 1
                              6   1}
                         [6] {7 1}
                         [7] {nil 1}}
            :lookback   1
            :max-length 4}
           (sut/analyse [[1 2 3] [2 4 5] [3 5 6 7]] :lookback 1))))
  (testing "multiple steps lookback"
    (is (= {:space      {[]    {1 1
                                2 1
                                3 1}
                         [1]   {2 1}
                         [1 2] {3 1}
                         [2 3] {nil 1}
                         [2]   {4 1}
                         [2 4] {5 1}
                         [4 5] {nil 1}
                         [5]   {nil 1}
                         [3]   {nil 1
                                5   1}
                         [3 5] {6 1}
                         [5 6] {7 1}
                         [6 7] {nil 1}
                         [7]   {nil 1}}
            :lookback   2
            :max-length 4}
           (sut/analyse [[1 2 3] [2 4 5] [3 5 6 7]] :lookback 2)))))

(deftest generate
  (let [gen (fn [text & opts]
              (sut/generate (apply sut/analyse text opts)))]
    (testing "some examples"
      (is (every? #{[1 2 3]}
                  (repeatedly sample-size
                              #(gen [[1 2 3]]))))
      (is (every? #{[1 2 3] [2 3 4] [2 3] [1 2 3 4]}
                  (repeatedly sample-size
                              #(gen [[1 2 3] [2 3 4]]
                                    :max-length 4)))))
    (testing "et-cetera"
      (is (= ::sut/et-cetera
             (->> (repeatedly sample-size
                              #(gen [[1 2 3] [2 3 3]]))
                  (drop-while #(<= (count %) 3))
                  first
                  last))
          "running until length exceeded max length of input causes ::et-cetera at the end")
      (is (every? #(= (last %) ::sut/et-cetera)
                  (repeatedly sample-size
                              #(gen [[1 2 3] [2 3 3]]
                                    :max-length 1)))
          "setting max-length too tight causes ::et-cetera at the end"))))

(deftest generate-text
  (let [gen (fn [text & opts]
              (sut/generate-text (apply sut/analyse-text text opts)))]
    (testing "some examples"
      (is (every? #{"Fred kisses Wilma."}
                  (repeatedly sample-size
                              #(gen "Fred kisses Wilma.")))
          "one sample, one possible result")
      (is (every? #{"Fred kisses Wilma."
                    "Barney kisses Betty."}
                  (repeatedly sample-size
                              #(gen (s/join "\n" ["Fred kisses Wilma."
                                                  "Barney kisses Betty."]))))
          "key too tight")
      (is (every? #{"Fred kisses Wilma."
                    "Barney kisses Betty."
                    "Fred kisses Betty."
                    "Barney kisses Wilma."}
                  (repeatedly sample-size
                              #(gen (s/join "\n" ["Fred kisses Wilma."
                                                  "Barney kisses Betty."])
                                    :lookback 1)))
          "loosen key a bit")))

  (testing "reproducable results"
    (let [seed        (gen/int)
          state-space (sut/analyse [[1 2 3] [2 3 3]])]
      (is (= (binding [gen/*rnd* (java.util.Random. seed)]
               (doall (repeatedly sample-size #(sut/generate state-space))))
             (binding [gen/*rnd* (java.util.Random. seed)]
               (doall (repeatedly sample-size #(sut/generate state-space)))))))))

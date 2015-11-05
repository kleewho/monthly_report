(ns monthly-report.holidays
  (:require [clj-time.core :as t]
            [clj-time.predicates :as pr]))

(def A 24)
(def B 5)

(defn march22nd [year]
  (t/date-time year 3 22))

(defn easter [year]
  (let* [a (rem year 19)
         b (rem year 4)
         c (rem year 7)
         d (rem (+ A (* 19 a)) 30)
         e (rem (+ (* 2 b) (* 4 c) (* 6 d) B) 7)]
    (t/plus (march22nd year) (t/days (+ e d)))))

(defn holidays [year]
  (let* [easter (easter year)
         easter-related #{easter
                          (t/plus easter (t/days 1))
                          (t/plus easter (t/days 49))
                          (t/plus easter (t/days 60))}
         date-related #{'(1 1)
                        '(1 6)
                        '(5 1)
                        '(5 3)
                        '(8 15)
                        '(11 1)
                        '(11 11)
                        '(12 25)
                        '(12 26)}]
        (clojure.set/union easter-related
                           (map #(apply t/date-time (conj % year)) date-related))))

(defn get-holidays-in-month [month]
  (filter #(= (t/month %) (t/month month)) (holidays (t/year month))))

(defn get-weekends-in-month [month]
  (->> (iterate #(t/plus % (t/days 1)) month)
       (take 31)
       (filter pr/weekend?)))

(defn get-workfree-days-in-month [month]
  (into [] (sort (into #{} (concat (get-weekends-in-month month)
                                   (get-holidays-in-month month))))))

(get-workfree-days-in-month (t/date-time 2015 12))

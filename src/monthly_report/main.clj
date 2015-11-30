(ns monthly-report.main
  (:require [monthly-report.jira :as jira]
            [monthly-report.holidays :as holidays]
            [clj-time.core :as t]
            [monthly-report.report :as report]))

(def work-free-days (holidays/get-workfree-days-in-month (t/date-time 2015 11)))

(def tasks (jira/get-tasks-in-month (t/date-time 2015 11) "lklich"))


(defn generate-days-in-month [month]
  (let [days (iterate #(t/plus % (t/days 1)) (t/first-day-of-the-month month))]
    (take-while #(= (t/month %) (t/month month)) days)))

(defn to-work-free-type [work-free-days]
  (fn [day]
    (when (work-free-days day)
      {:type :non-work
       :date day})))

(defn to-work-type [tasks]
  (fn [day]
    (conj (first (filter #(t/within? (:interval %) day) tasks))
          [:type :work])))

(map #(or ((to-work-free-type work-free-days) %)
          ((to-work-type tasks) %))
      (generate-days-in-month (t/date-time 2015 11)))

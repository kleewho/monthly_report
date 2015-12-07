(ns monthly-report.date-utils
  (:require [clj-time.core :as t]))

(defn generate-days-in-month [month]
  (let [days (iterate #(t/plus % (t/days 1)) (t/first-day-of-the-month month))]
    (take-while #(= (t/month %) (t/month month)) days)))

(defn difference-in-days [date1 date2]
  (t/in-days (t/interval date1 date2)))

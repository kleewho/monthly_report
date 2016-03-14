(ns monthly-report.user
  (:require [monthly-report.date-utils :as u]
            [monthly-report.vacations :as v]
            [clj-time.core :as t]
            [environ.core :refer [env]]))

(defn- empty-calendar [month]
  (let [days (u/generate-days-in-month month)]
    (reduce #(conj %1 [%2 {}]) {} days)))

(u/generate-days-in-month (t/date-time 2016 2) )

(calendar (t/date-time 2016 2))


(defn user [username month]
  (-> (get-in env [:users (keyword username)])
      (conj [:calendar (empty-calendar month)])
      ))

(v/add-vacations (user "jgruszka" (t/date-time 2015 12))
               (t/date-time 2015 12))

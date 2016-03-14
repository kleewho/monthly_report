(ns monthly-report.vacations
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clj-time.format :as f]
            [clj-time.core :as t]))

(def formatter (f/formatter "YYYY-MM-dd"))
(defn date-mapper [date]
  (f/parse formatter date))

(def columns [{:column "TXTPRACOWNIK" :field :name :mapper identity}
              {:column "DATETERMINROZPURLOPU" :field :vacation-start :mapper date-mapper}
              {:column "DATETERMINZAKURLOPU" :field :vacation-end :mapper date-mapper}
              {:column "TXTRODZAJURLOPU" :field :type :mapper identity}])

(defn csv [filename]
  (with-open [in-file (io/reader filename)]
    (doall
     (csv/read-csv in-file))))

(defn with-indices [first-row columns]
  (let [s (map #(conj % [:index (.indexOf first-row (:column %))]) columns)]
    s))

(defn value [row column]
  (let [{idx :index
         mapper :mapper
         field :field} column]
    [field (mapper (get-in row [idx]))]))

(defn values [columns row]
  (->> columns
       (map #(value row %))
       (into {})))

(defn vacations [username month]
  (let [m (t/month month)
        y (t/year month)
        filename (str "/home/lklich/Downloads/urlopy_CTO WEB DEV.csv")
        [header & data] (csv filename)
        columns-description (with-indices header columns)]
    (->> data
         (map (partial values columns-description))
         (filter #(= username (:name %))))))

(defn within-any? [vacations day]
  (some #(t/within? (:vacation-start %)  (:vacation-end %)  day) vacations))

(defn add-vacations [user month]
  (let [vs (vacations user month)]
    (reduce #(if (within-any? vs %2) (assoc-in %1 [:calendar %2 :type] :vacation) (assoc-in %1 [:whatever ] "shit"))
            user (:calendar user))))

(monthly-report.user/user "jgruszka" (t/date-time 2015 12))

(add-vacations (monthly-report.user/user "bkepa" (t/date-time 2015 12)) (t/date-time 2015 12))

(within-any?  (vacations "jgruszka" (t/now)) (t/date-time 2015 12 31))
(if (within-any?  (vacations "jgruszka" (t/now)) (t/date-time 2015 12 31)) (println "yes") (println "no"))

(ns monthly-report.vacations
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def columns '("TXTPRACOWNIK" "TXTDECYZJA" "DATETERMINROZPURLOPU" "DATETERMINZAKURLOPU" "TXTRODZAJURLOPU"))

(defn csv []
  (with-open [in-file (io/reader "/home/lklich/Downloads/urlopy_CTO_WEB_DEV.csv")]
    (doall
     (csv/read-csv in-file))))

(defn indices [first-row columns]
  (map #(.indexOf first-row %) columns))

(defn vacations [username]
  (let [[header & vacations] (csv)
        column-indices (indices header columns)]
    (filter #(= username (first %)) (map (partial values column-indices) vacations))))

(defn values [indices row]
  (map #(get-in row [%]) indices))

(vacations "bkepa")

(indices (first (csv)) columns)

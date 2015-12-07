(ns monthly-report.report
  (:require [clj-time.core :as t]
            [dk.ative.docjure.spreadsheet :as xl]
            [monthly-report.date-utils :as du]))

(def header-beginning [3 3])
(def work-beginning [4 3])

(defn sheet-cell [sheet row col] (-> sheet (.getRow row) (.getCell col)))

(defn prepare-header [month]
  (map #(du/difference-in-days (t/date-time 1899 12 30) %) (du/generate-days-in-month month)))

(defn sheet-task [sheet row month {key :key summary :summary date :date}]
  (let [col (+ (second work-beginning) (du/difference-in-days month date))]
    (do (xl/set-cell! (sheet-cell sheet row 0) key)
        (xl/set-cell! (sheet-cell sheet row 2) summary)
        (xl/set-cell! (sheet-cell sheet row col) 4)
        (xl/set-cell! (sheet-cell sheet (+ row 1) col) 3)
        (xl/set-cell! (sheet-cell sheet (+ row 2) col) 1))))

; Load a template spreadsheet from a resource, fill it with data and save it
(defn save-to-excel [month monthly-tasks file-name]
  (let [wb (xl/load-workbook-from-resource "WebDev_Monthly_template.xls")
        sheet (xl/select-sheet "TIME REPORT" wb)

        tasks-by-type (group-by :type monthly-tasks)
        ;non-work-days (:non-work tasks-by-type)
        work-days (:work tasks-by-type)
        work-days-by-task-indexed (map vector (range) (group-by :key work-days))]
    (do

      ; creating the header
      (doseq [[idx value] (map vector (range) (prepare-header month))]
                        (xl/set-cell! (sheet-cell sheet (first header-beginning) (+ (second header-beginning) idx)) value))

      ; creating work-days
      (doseq [[idx [key tasks]] work-days-by-task-indexed]
        (doseq [task tasks]
          (sheet-task sheet (+ (first work-beginning) (* idx 4)) month task)))

      ; saving workbook
      (xl/save-workbook! file-name wb))))

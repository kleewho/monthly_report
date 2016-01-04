(ns monthly-report.report
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [dk.ative.docjure.spreadsheet :as xl]
            [monthly-report.date-utils :as du])
  (:import (org.apache.poi.ss.usermodel CellStyle)))

(def header-beginning [3 3])
(def work-beginning [4 3])
(def sheet-last-row 46)


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

(defn set-cell-background! [cell color wb]
  (let [old-cell-style (.getCellStyle cell)
        new-cell-style (xl/create-cell-style! wb {})]
    (do (.cloneStyleFrom new-cell-style old-cell-style)
        (.setFillForegroundColor new-cell-style (xl/color-index color))
        (.setFillPattern new-cell-style CellStyle/SOLID_FOREGROUND)
        (xl/set-cell-style! cell new-cell-style))))

(def english-formatter (f/formatter "MMMM yyyy"))

; Load a template spreadsheet from a resource, fill it with data and save it
(defn save-to-excel [month monthly-tasks file-name name date title work-free-days]
  (let [wb (xl/load-workbook-from-resource "WebDev_Monthly_template.xls")
        sheet (xl/select-sheet "TIME REPORT" wb)
        tasks-by-type (group-by :type monthly-tasks)
        non-work-days (:non-work tasks-by-type)
        work-days (:work tasks-by-type)
        work-days-by-task-indexed (map vector (range) (group-by :key work-days))
        header-row (first (xl/row-seq sheet))]

    (do
      ; signing report
      (xl/set-cell! (sheet-cell sheet 0 1) (str name "\n" (f/unparse english-formatter date) "\n" title))
      ; creating the header
      (doseq [[idx value] (map vector (range) (prepare-header month))]
                        (xl/set-cell! (sheet-cell sheet (first header-beginning) (+ (second header-beginning) idx)) value))

      ; coloring work free days
      (doseq [non-work-day non-work-days
              row (range (first header-beginning) sheet-last-row)]
        (let [column (+ (t/day (:date non-work-day)) (- (first header-beginning) 1))
              cell (sheet-cell sheet row column)]
          (set-cell-background! cell :yellow wb)))

      ; creating work-days
      (doseq [[idx [key tasks]] work-days-by-task-indexed]
        (doseq [task tasks]
          (sheet-task sheet (+ (first work-beginning) (* idx 4)) month task)))

      ; saving workbook
      (xl/save-workbook! file-name wb))))

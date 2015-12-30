(ns monthly-report.main
  (:require [monthly-report.jira :as jira]
            [monthly-report.holidays :as holidays]
            [clj-time.core :as t]
            [monthly-report.report :as report]
            [monthly-report.date-utils :as du]
            ))

(def month (t/date-time 2015 11))

(def user {:jira-user "lklich"
           :name "Łukasz Klich"
           :position "Java Developer"})

(def report-name "Monthly_report")

(def work-free-days (holidays/get-workfree-days-in-month month))

(def tasks (jira/get-tasks-in-month month (:jira-user user)))

(defn to-work-free-type [work-free-days]
  (fn [day]
    (when (work-free-days day)
      {:type :non-work
       :date day})))

(defn to-work-type [tasks]
  (fn [day]
    (conj (first (filter #(t/within? (:interval %) day) tasks))
          [:type :work]
          [:date day])))

(def monthly-tasks
  (map #(or ((to-work-free-type work-free-days) %)
            ((to-work-type tasks) %))
       (du/generate-days-in-month month)))

(do
    (report/save-to-excel month monthly-tasks (str report-name "_" (t/year month) "_" (t/month month) "_" (:jira-user user) ".xls")))

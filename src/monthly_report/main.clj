(ns monthly-report.main
  (:use ring.util.io)
  (:require [monthly-report.jira :as jira]
            [monthly-report.holidays :as holidays]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [monthly-report.report :as report]
            [monthly-report.date-utils :as du]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [environ.core :refer [env]]))

(def report-name "Monthly_report")

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

(defn generate-report [user month]
  (let [tasks (jira/get-tasks-in-month month (:jira-user user))
        work-free-days (holidays/get-workfree-days-in-month month)
        monthly-tasks (map #(or ((to-work-free-type work-free-days) %)
                                ((to-work-type tasks) %))
                           (du/generate-days-in-month month))
        file-name (str report-name "_" (t/year month) "_" (t/month month) "_" (:jira-user user) ".xls")]
    (report/create-report month monthly-tasks file-name (:name user) month (:title user) work-free-days)))
(map #(or ((to-work-free-type (holidays/get-workfree-days-in-month (t/date-time 2015 12))) %)
          nil)
     (du/generate-days-in-month (t/date-time 2015 12)))

(def date-to-filename-formatter (f/formatter "MMM_YYYY"))

(defn filename [username month]
  (let [usernameUnderscored (clojure.string/replace username " " "_")
        filename (list "WebDev_Monthly" usernameUnderscored (f/unparse date-to-filename-formatter month))]
    (clojure.string/join "_" filename)))

(defn return-report [user year month]
  (let [user (get-in env [:users (keyword user)])
        month (t/date-time year month)
        wb (generate-report user month)]

    {:headers {"Content-Type" "application/vnd.ms-excel"
               "Content-Disposition" (str "inline; filename=\"" (filename (:name user) month) ".xls\"")}
     :body (piped-input-stream (report/write-report wb))}))


(defroutes app-routes
  (GET "/report/:year/:month/:user" [year month user]
    (return-report user (Integer/parseInt year) (Integer/parseInt month)))
  (route/not-found "Not Found"))

(defn init [])

(def app
  (->
   (routes app-routes)
   (wrap-defaults api-defaults)))

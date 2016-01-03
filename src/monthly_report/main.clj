(ns monthly-report.main
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

(defn generate-report [user year month]
  (let [user (get-in env [:users (keyword user)])
        month (t/date-time year month)
        tasks (jira/get-tasks-in-month month (:jira-user user))
        work-free-days (holidays/get-workfree-days-in-month month)
        monthly-tasks (map #(or ((to-work-free-type work-free-days) %)
                                ((to-work-type tasks) %))
                           (du/generate-days-in-month month))
        file-name (str report-name "_" (t/year month) "_" (t/month month) "_" (:jira-user user) ".xls")]
    (report/save-to-excel month monthly-tasks file-name (:name user) month (:title user))))

(.toString (t/now))

(def custom-formatter (f/formatter "MMMM yyyy"))
(f/unparse custom-formatter (t/date-time 2015 12))

(keyword "lklich")
(get-in env [:users (keyword "lklich")])
;; (def cli-options
;;   ;; An option with a required argument
;;   [["-u" "--user USER" "User for which generate report"
;;     :parse-fn #(str %)
;;     :validate [#(get-in env [:users (symbol %)]) "Must be known user"]]
;;    ;; A non-idempotent option
;;    ["-m" "--month MONTH" "Month for which generate report"
;;     :default (t/month (t/now))
;;     :validate [#(< 1 % 12) "I know only 12 months"]]])

;; (defn -main [& args]
;;   (let [{user :user
;;          month :month} (:options (parse-opts args cli-options))]))

(defroutes app-routes
  (GET "/report/:user/:year/:month" [user year month]
    (generate-report user (Integer/parseInt year) (Integer/parseInt month)))
  (route/not-found "Not Found"))

(defn init []
  )

(def app
  (->
   (routes app-routes)
   (wrap-defaults api-defaults)))

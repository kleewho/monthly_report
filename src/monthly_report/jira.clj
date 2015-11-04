(ns monthly-report.jira
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(def jira-api "https://jira.upc.biz/rest/api/2/")

(def year 2015)

(defn jira-query [jira-url]
  (str jira-url "search"))

(defn tasks-body [month year user]
  (let [date (start-of-the-month month year)]
    {:jql (str "project = TGD"
               " AND status was Done by " user " after \"" date "\""
               " AND (status was \"To do\" after \"" date "\""
               " OR status was \"In progress\" after \"" date "\")")
     :fields [:id :key :summary]}))

(defn start-of-the-month [month year]
  (str year "/" month "/01"))

(defn get-tasks-in-month [month year user passwd]
  (let [result (client/post
                (jira-query jira-api)
                {:basic-auth [user passwd]
                 :form-params (tasks-body month year user)
                 :content-type :json
                 :as :json
                 :insecure? true})]
    (get-in result [:body :issues])))

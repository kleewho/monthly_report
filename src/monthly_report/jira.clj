(ns monthly-report.jira
  (:require [clj-http.client :as client]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [environ.core :refer [env]]))

(def jira-api "https://jira.upc.biz/rest/api/2/")

(def jira-date-formatter (f/formatter "\"YYYY/MM/dd\""))

(defn jira-query [jira-url]
  (str jira-url "search"))

(defn tasks-body [month user]
  (let [start (f/unparse jira-date-formatter month)
        end (f/unparse jira-date-formatter (t/plus month (t/months 1)))]
    {:jql (str "assignee=" user
               " AND resolved >= " start
               " AND resolved <= " end)
     :fields [:id :key :summary]}))

(defn get-tasks-in-month [month user]
  (let [result (client/post
                (jira-query jira-api)
                {:headers {:authorization (str "Basic " (env :jira-token))}
                 :form-params (tasks-body month user)
                 :content-type :json
                 :as :json
                 :insecure? true})]
    (get-in result [:body :issues])))

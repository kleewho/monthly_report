(ns monthly-report.jira
  (:require [clj-http.client :as client]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.predicates :as pr]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def jira-api "https://jira.upc.biz/rest/api/2/")

(def jira-date-formatter (f/formatter "\"YYYY/MM/dd\""))

(defn- jira-query [jira-url]
  (str jira-url "search"))

(defn- tasks-body [month user]
  (let [start (f/unparse jira-date-formatter month)
        end (f/unparse jira-date-formatter (t/plus month (t/months 1)))]
    {:jql (str "assignee=" user
               " AND resolved >= " start
               " AND resolved <= " end)
     :fields [:id :key :summary]
     :expand [:changelog :transitions]
     }))

(defn- changelog-filter [value changelog]
  (= value (get-in changelog [:items 0 :toString])))

(defn- transition [changelog]
  (or
   (changelog-filter "Done" changelog)
   (changelog-filter "Canceled" changelog)
   (changelog-filter "In Progress" changelog)))

(defn- from-first [history]
  (let [start (f/parse (:created history))
        end (f/parse (:created history))
        new-state (get-in history [:items 0 :toString])
        old-state (get-in history [:items 0 :fromString])]
    (vector
     (conj {} [:start start]
           [:end end]
           [:new-state new-state]
           [:old-state old-state]))))

(defn- fetch-transition [histories history]
  (let [last (last histories)
        [a] (from-first history)]
    (conj histories (assoc a :start (:end last)))))

(defn- get-task-transitions [task]
  (let [histories (->> task :changelog :histories)
        sorted-histories (sort-by :created histories)
        [first & rest] (filter transition sorted-histories)]
    (reduce fetch-transition (from-first first) rest)))

(defn- find-task-end [transitions]
  (:end (or (first (filter #(= (:new-state %) "Done") transitions))
            (first (filter #(= (:new-state %) "Canceled")) transitions))))

(defn- to-groomed-task [last-day-of-the-month]
  (fn [task]
    (let [transitions (get-task-transitions task)
          start (:start (first transitions))
          end (find-task-end transitions)]
      {:key (:key task)
       :summary (get-in task [:fields :summary])
       :interval (t/interval start (or end last-day-of-the-month))})))

(defn get-tasks-in-month [month user]
  (let [result (client/post
                (jira-query jira-api)
                {:headers {:authorization (str "Basic " (env :jira-token))}
                 :form-params (tasks-body month user)
                 :content-type :json
                 :as :json
                 :insecure? true})
        raw-tasks (get-in result [:body :issues])]
    (map (to-groomed-task (t/last-day-of-the-month month)) raw-tasks)))

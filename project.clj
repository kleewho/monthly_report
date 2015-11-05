(defproject monthly_report "0.1.0-SNAPSHOT"
  :description "monthly report: creates hour reports querying jira"
  :url "https://github.com/kleewho/monthly_report"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [clj-time "0.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.infolace/excel-templates "0.3.1"]])

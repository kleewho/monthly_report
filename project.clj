(defproject monthly_report "0.1.0-SNAPSHOT"
  :description "monthly report: creates hour reports querying jira"
  :url "https://github.com/kleewho/monthly_report"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [clj-time "0.11.0"]
                 [environ "1.0.1"]
                 [org.clojure/data.json "0.2.6"]
                 [dk.ative/docjure "1.9.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]]
                 ;; [org.clojure/tools.cli "0.3.3"]]
  :plugins [[lein-environ "1.0.1"]
            [lein-gorilla "0.3.5"]
            [lein-ring "0.9.7"]
            ]
  :ring {:handler monthly-report.main/app
         :init monthly-report.main/init
         :start true})

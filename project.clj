(defproject facet "0.2.0"
  :description "Sony Remote Camera API Wrapper."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [xmlpull/xmlpull "1.1.3.4d_b4_min"]
                 [org.ogce/xpp3 "1.1.6"]
                 [org.json/json "20140107"]]
  :java-source-paths ["src/java/com/example/sony/cameraremote"]
  :jar-exclusions [#"\.swp$" #"\.swo$" #"\.java$"]
  )

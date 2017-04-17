(ns finder.handler

  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zfl]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]))

(def yaurl "https://blogs.yandex.ru/search.rss?text=")

(defn get-feed [query]
 (zip/xml-zip (xml/parse (str yaurl query "&p=10"))))

(defn extr-links [query]
   (map #(re-find #"\/\/(.*?)\/" %) (zfl/xml-> (get-feed query) :channel :item :link zfl/text )))

(defn list-with-dup [query] (mapcat rest (extr-links [query])))

(defn get-response [query]
 (println (str "lalala" query))
 (apply merge-with + (map #(frequencies (list-with-dup [%])) query)))

(defn create-json-response [request]
  (response (get-response (:query (:params request)))))

(defn request-handler [query]
  (wrap-json-response create-json-response {:pretty true} ))

(defroutes app-routes
  (GET "/" [] "Hello world!")
  (GET "/search" [query] request-handler)
  (GET "/search" [] "Try to use params.")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(ns facet.core
;  (:import com.example.sony.cameraremote.ServerDevice.ApiService)
;  (:import com.example.sony.cameraremote.SimpleRemoteApi)
  (:import com.example.sony.cameraremote.SimpleSsdpClient)
;  (:import com.example.sony.cameraremote.SimpleCameraEventObserver)
)


(def SonyDevice (atom nil))


(def SonyCamInfo {:name            (atom nil)
               :action-list-url (atom nil)
               :endpoint-url    (atom nil)
               :friendly-name   (atom nil)
               :ddurl           (atom nil)
               :model-name      (atom nil)
               :udn             (atom nil)
               :icon-url        (atom nil)
               :ip-address      (atom nil)
               :api-services    (atom nil)
               })

(defn apiService
  "service-name:
     \"camera\""
  [service-name]
  (let [
        device @SonyDevice
        name  (-> device (.getApiService service-name) (.getName))
        aurl  (-> device (.getApiService service-name) (.getActionListUrl))
        eurl  (-> device (.getApiService service-name) (.getEndpointUrl))
        ]
    (reset! (SonyCamInfo :name) name)
    (reset! (SonyCamInfo :action-list-url) aurl)
    (reset! (SonyCamInfo :endpoint-url) eurl)
    ))


(defn updateSonyCamInfo!
  []
  (let [
        device @SonyDevice
        fname (-> device (.getFriendlyName))
        ddurl (-> device (.getDDUrl))
        mname (-> device (.getModelName))
        udn   (-> device (.getUDN))
        iurl  (-> device (.getIconUrl))
        ip    (-> device (.getIpAddres))
        services (-> device (.getApiServices))
        ]
    (reset! (SonyCamInfo :friendly-name) fname)
    (reset! (SonyCamInfo :ddurl) ddurl)
    (reset! (SonyCamInfo :model-name) mname)
    (reset! (SonyCamInfo :udn) udn)
    (reset! (SonyCamInfo :icon-url) iurl)
    (reset! (SonyCamInfo :ip-address) ip)
    (reset! (SonyCamInfo :api-services) services)
    ))


(defn search-result-handler
  "This function returns SimpleSsdpClient"
  []
  (proxy [com.example.sony.cameraremote.SimpleSsdpClient$SearchResultHandler] []
    (onDeviceFound   [^com.example.sony.cameraremote.ServerDevice device]
      (let [dev device]
       (reset! SonyDevice dev)))
    (onFinished      []
      (println "onFinished!"))
    (onErrorFinished []
      (println "onErrorFinished!"))))


(defn init
  "Set up camera device. This Device Discovery sequence reffers CameraRemoteSampleApp.java"
  []
  (let [ssdp (new SimpleSsdpClient)]
    (if (not (.isSearching ssdp)) (.search ssdp (search-result-handler)))
    ))


#_(defn takeAndFetchPicture
  "This function reffers takeAndFetchPicture of SampleCameraActivity.java"
  []
  (let [replyJson (-> (new SimpleRemoteApi) (.actTakePicture))
        resultObj (-> replyJson (.getJSONArray "result"))
        iamgeUrlsObj (-> resultObj (.getJSONArray 0))
        postImageUrl (if (<= 1 (.length imageUrlsObj)) (-> imageUrlsObj (.getString 0)))
        ]
    postImageUrl
    ))

#_(
;; Usage
;; Namespace setting and load files.
(ns facet.core)
(use 'facet.core)

;; Device discovery start.
(init)

;; Update camera information.
(updateSonyCamInfo)

;; Then, you can access to camera information.
SonyCamInfo
)
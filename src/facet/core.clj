(ns facet.core
  (:import com.example.sony.cameraremote.SimpleRemoteApi)
  (:import com.example.sony.cameraremote.SimpleSsdpClient)

  (:import com.example.sony.cameraremote.utils.SimpleLiveviewSlicer)
  (:import java.util.concurrent.ArrayBlockingQueue)
  (:import java.util.concurrent.BlockingQueue)

  (:import javax.imageio.ImageIO)
  (:import java.io.File)
  (:import java.io.ByteArrayInputStream)
;  (:import com.example.sony.cameraremote.SimpleCameraEventObserver)
)

;; ###TODO: Returned JSON data handling.

;; Server device.
(def ^:private SonyDevice (atom nil))
(def ^:private RemoteApi (atom nil))

(def LiveView (atom nil))

;; Contains static information of the camera.
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


;; ##JSON data handling for takeAndFetchPicture
(defn- getResultValue
  [json]
  ;;(-> json (.get "result") (.get 0) (.optString 0))
  (-> json (.getJSONArray "result") (.get 0) (.getString 0)))

(defn- getIdValue
  [json]
  (-> json (.get "id")))


;; ##JSON data handling for startLiveView
(defn getLiveviewUrl
  "Returns liveview url string."
  [json]
  (-> json (.getJSONArray "result") (.getString 0)))

;; This is hacky declare to avoid Compiler Exception.
(declare startLiveview)
(declare stopLiveview)

(defn retrieveLiveView
  "
  ;; This function takes an agent which hold image data, and returns a function to stop image-retrieving-thread.
  (def foo-agent (agent nil))
  (def foo-stopper (retrieveLiveView foo-agent))

  ;; Save current liveview image like below:
  (ImageIO/write (ImageIO/read (ByteArrayInputStream. @foo-agent)) \"jpg\" (File. \"foo.jpg\"))
  ;; Each time, this will save current liveview image.
  (ImageIO/write (ImageIO/read (ByteArrayInputStream. @foo-agent)) \"jpg\" (File. \"foo.jpg\"))
  (ImageIO/write (ImageIO/read (ByteArrayInputStream. @foo-agent)) \"jpg\" (File. \"foo.jpg\"))

  ;; At last, stop the image-retrieving-thread.
  (foo-stopper true)
  "
  [imgDataAgent]
  (let [fetching?   (atom true)
        replyJson   (startLiveview)
        liveviewUrl (getLiveviewUrl replyJson)
        slicer      (new com.example.sony.cameraremote.utils.SimpleLiveviewSlicer)
        ;; Image retrieving thread.
        retriever   (Thread. (fn []
                               (try
                                 (.open slicer liveviewUrl)
                                 (if (= slicer nil) nil
                                     (while @fetching?
                                       (do 
                                         (let [payload (.nextPayload slicer)]
                                           (send-off imgDataAgent (fn [_] (.jpegData payload)))))))
                                 (catch Exception e nil)
                                 (finally
                                   (.close slicer)
                                   (stopLiveview)))))
        ]
    (.start retriever)
    ;; Return a closure
    (fn [stop?]
      (if stop? (reset! fetching? false))
      fetching?)))

    
;; This is hacky declare to avoid Compiler Exception.
;; Todo: Manage name-space properly
(declare actTakePicture)

(defn takeAndFetchPicture
  "This function take a picture, and returns map of its URL and id."
  []
  (when-not (= @RemoteApi nil)
    (let [json   (actTakePicture)
          id     (getIdValue json)
          result (getResultValue json)]
      {:id id, :result result}
      )))


#_(defn apiService
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


(defn- updateSonyCamInfo!
  [device]
  (let [
        dev @SonyDevice
        fname (-> dev (.getFriendlyName))
        ddurl (-> dev (.getDDUrl))
        mname (-> dev (.getModelName))
        udn   (-> dev (.getUDN))
        iurl  (-> dev (.getIconUrl))
        ip    (-> dev (.getIpAddres))
        services (-> dev (.getApiServices))
        ]
    (reset! (SonyCamInfo :friendly-name) fname)
    (reset! (SonyCamInfo :ddurl) ddurl)
    (reset! (SonyCamInfo :model-name) mname)
    (reset! (SonyCamInfo :udn) udn)
    (reset! (SonyCamInfo :icon-url) iurl)
    (reset! (SonyCamInfo :ip-address) ip)
    (reset! (SonyCamInfo :api-services) services)
    ))


(defn- search-result-handler
  "This function returns SimpleSsdpClient object."
  []
  (proxy [com.example.sony.cameraremote.SimpleSsdpClient$SearchResultHandler] []
    (onDeviceFound   [^com.example.sony.cameraremote.ServerDevice device]
      (reset! SonyDevice device)
      (reset! RemoteApi (proxy [com.example.sony.cameraremote.SimpleRemoteApi] [device]))
      (updateSonyCamInfo! device))
    (onFinished      []
      (println "onFinished!"))
    (onErrorFinished []
      (println "onErrorFinished!"))))


(defn init
  "Set up camera device. This Device Discovery sequence reffers CameraRemoteSampleApp.java"
  []
  (let [ssdp (new SimpleSsdpClient)]
    (if (not (.isSearching ssdp)) (.search ssdp (search-result-handler)))))


;; #Public functions of SimpleRemoteApi.java
(defn getAvailableApiList
  "Calls getAvailableApiList API to the target server. Request JSON data is
   such like as below.
   
   <pre>
   {
     \"method\": \"getAvailableApiList\",
     \"params\": [\"\"],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.getAvailableApiList @RemoteApi))


(defn getApplicationInfo
  "Calls getApplicationInfo API to the target server. Request JSON data is
   such like as below.
   
   <pre>
   {
     \"method\": \"getApplicationInfo\",
     \"params\": [\"\"],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.getApplicationInfo @RemoteApi))


(defn getShootMode
  "/**
   Calls getShootMode API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"getShootMode\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.getShootMode @RemoteApi))


(defn setShootMode
  "Calls setShootMode API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"setShootMode\",
     \"params\": [\"still\"],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @param shootMode shoot mode (ex. \"still\")
   @return JSON data of response"
  [shootMode]
  (-> @RemoteApi (.setShootMode shootMode)))


(defn getAvailableShootMode
  "Calls getAvailableShootMode API to the target server. Request JSON data
   is such like as below.
   
   <pre>
   {
     \"method\": \"getAvailableShootMode\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.getAvailableShootMode @RemoteApi))


(defn getSupportedShootMode
  "Calls getSupportedShootMode API to the target server. Request JSON data
   is such like as below.
   
   <pre>
   {
     \"method\": \"getSupportedShootMode\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.getSupportedShootMode @RemoteApi))


(defn startLiveview
  "Calls startLiveview API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"startLiveview\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.startLiveview @RemoteApi))


(defn stopLiveview
  "Calls stopLiveview API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"stopLiveview\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.stopLiveview @RemoteApi))


(defn startRecMode
  "Calls startRecMode API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"startRecMode\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.startRecMode @RemoteApi))


(defn stopRecMode
  "Calls stopRecMode API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"stopRecMode\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.stopRecMode @RemoteApi))


(defn- actTakePicture
  "Calls actTakePicture API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"actTakePicture\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.actTakePicture @RemoteApi))


(defn startMovieRec
  "Calls startMovieRec API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"startMovieRec\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.startMovieRec @RemoteApi))


(defn stopMovieRec
  "Calls stopMovieRec API to the target server. Request JSON data is such
   like as below.
   
   <pre>
   {
     \"method\": \"stopMovieRec\",
     \"params\": [],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  []
  (.stopMovieRec @RemoteApi))


(defn actZoom
  "Calls actZoom API to the target server. Request JSON data is such like as
   below.
   
   <pre>
   {
     \"method\": \"actZoom\",
     \"params\": [\"in\",\"stop\"],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @return JSON data of response"
  [^String direction ^String movement]
  (-> @RemoteApi (.actZoom direction movement)))


(defn getEvent
  "Calls getEvent API to the target server. Request JSON data is such like
   as below.
   
   <pre>
   {
     \"method\": \"getEvent\",
     \"params\": [true],
     \"id\": 2,
     \"version\": \"1.0\"
   }
   </pre>
   
   @param longPollingFlag true means long polling request.
   @return JSON data of response"
  [longPollingFlag]
  (-> @RemoteApi (.getEvent longPollingFlag)))


#_(
;; #Usage

;; Namespace setting and load files.
(use 'facet.core)

;; Start device discovery.
(init)

;; Then, your camera information is updated.
SonyCamInfo

;; Take a picture.
(takeAndFetchPicture)
;; This returns URL like below.
;; "http://10.0.0.1:60152/pict130106_1704390000.JPG?%211234%21http%2dget%3a%2a%3aimage%2fjpeg%3a%2a%21%21%21%21%21"
;; Now you can grab your photo by accessing the URL.
)

(ns status-im.ui.components.invite.events
  (:require [re-frame.core :as re-frame]
            [oops.core :refer [oget]]
            [status-im.utils.fx :as fx]
            [status-im.ui.components.react :as react]
            [status-im.acquisition.core :as acquisition]))

(def get-link "get.status.im")

(re-frame/reg-fx
 ::share
 (fn [content]
   (.share ^js react/sharing (clj->js content))))

(fx/defn share-link
  {:events [::share-link]}
  [_ response]
  (let [invite-id (oget response "invite-id")
        message   (str "Hey join me on Status:" get-link invite-id)]
    {::share {:message message}}))

(fx/defn generate-invite
  {:events [::generate-invite]}
  [cofx {:keys [address]}]
  (acquisition/handle-acquisition cofx
                                  {:message    {:address address}
                                   :on-success [::share-link]}))

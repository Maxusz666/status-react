(ns status-im.ui.components.invite.events
  (:require [re-frame.core :as re-frame]
            [oops.core :refer [oget]]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
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

(defn- get-reward [contract address on-success]
  (json-rpc/eth-call
   {:contract   contract
    :method     "getReferralReward(address)"
    :params     [address]
    ;; [amount maxThreshold attribCount]
    :outputs    ["uint256" "uint256" "uint256"]
    :on-success on-success}))

(re-frame/reg-fx
 ::get-rewards
 (fn [accounts]
   (doseq [{:keys [contract address on-success]} accounts]
    (get-reward contract address on-success))))

(fx/defn default-reward-success
  {:events [::default-reward-success]}
  [{:keys [db]} [amount max-threshold attrib-count]]
  {:db (assoc-in db [:acquisition :referral] {:amount        amount
                                              :max-threshold max-threshold
                                              :attrib-count  attrib-count})})

(fx/defn get-reward-success
  {:events [::get-reward-success]}
  [{:keys [db]} account [amount max-threshold attrib-count]]
  {:db (assoc-in db [:acquisition :accounts account]
                 {:amount        amount
                  :max-threshold max-threshold
                  :attrib-count  attrib-count})})

(fx/defn get-default-reward
  {:events [::get-default-reward]}
  [{:keys [db] :as cofx}]
  {::get-rewards [{:contract   (contracts/get-address db :status/acquisition)
                   :address    (ethereum/default-address db)
                   :on-success #(re-frame/dispatch [::default-reward-success %])}]})

(fx/defn get-accounts-reward
  {:events [::get-accounts-reward]}
  [{:keys [db]}]
  (let [contract   (contracts/get-address db :status/acquisition)
        on-success #(re-frame/dispatch [::get-reward-success %])
        accounts   (filter #(not= (:type %) :watch) (get db :multiaccount/accounts))]
    {::get-rewards (mapv (fn [{:keys [address]}]
                           {:address    address
                            :contract   contract
                            :on-success on-success}) accounts)}))

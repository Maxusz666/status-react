(ns status-im.ui.components.invite.views
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.bottom-sheet.view :as bottom-sheet]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.button :as button]
            [status-im.i18n :as i18n]
            [quo.design-system.spacing :as spacing]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.invite.events :as events]
            [quo.react-native :as rn]))

;; Select account sheet
(defn render-account [current-account change-account]
  (fn [account]
    [list-item/list-item
     {:theme     :selectable
      :selected? (= (:address current-account) (:address account))
      :icon      [chat-icon/custom-icon-view-list (:name account) (:color account)]
      :title     (:name account)
      :subtitle  (utils/get-shortened-checksum-address (:address account))
      :on-press  #(change-account account)}]))

(defn accounts-list [accounts current-account change-account]
  (fn []
    [rn/view {:flex 1}
     [quo/text {:align :center
                :style {:padding-horizontal 16}}
      "Select an account to receive your referral bonus"]
     [rn/flat-list {:data      accounts
                    :key-fn    :address
                    :render-fn (render-account current-account change-account)}]]))

;; Invite sheet

(defn- step [{:keys [number description]}]
  [rn/view {:style (merge
                    (:small spacing/padding-vertical)
                    {:flex-direction :row
                     :flex           1
                     :align-items    :center})}
   [rn/view {:style {:width           40
                     :height          40
                     :border-radius   20
                     :border-width    1
                     :justify-content :center
                     :align-items     :center
                     :border-color    (:ui-01 @colors/theme)}}
    [quo/text {:weight :bold
               :size   :large}
     number]]
   [rn/view {:padding-left (:base spacing/spacing)
             :flex         1}
    [quo/text {}
     description]]])

(def steps-values [{:number      1
                    :description "You send a unique invite link to your friend to download and join Status"}
                   {:number      2
                    :description "Your friend downloads Status and creates an account"}
                   {:number      3
                    :description "Your friend buys a Starter Pack on any Android device. Sorry, no iOS!"}
                   {:number      4
                    :description "You receive your referral bonus"}])

(defn referral-steps []
  [rn/view {:style (merge
                    (:tiny spacing/padding-vertical)
                    (:base spacing/padding-horizontal)
                    {:border-bottom-width 1
                     :border-bottom-color (:ui-01 @colors/theme)})}
   [rn/view {:style {:padding-top    (:small spacing/spacing)
                     :padding-bottom (:x-tiny spacing/spacing)}}
    [quo/text {:color :secondary}
     "How it works"]]
   [rn/view {:flex 1}
    (for [s steps-values]
      [step s])]])

(defn referral-account []
  (let [visible (reagent/atom false)]
    (fn [{:keys [account accounts change-account]}]
      [rn/view {:style (:tiny spacing/padding-vertical)}
       [rn/view {:style (merge (:base spacing/padding-horizontal)
                               (:x-tiny spacing/padding-vertical))}
        [quo/text {:color :secondary}
         "Account to receive your referral bonus"]]
       [rn/modal {:visible     @visible
                  :transparent true}
        [bottom-sheet/bottom-sheet {:show?     true
                                    :on-cancel #(reset! visible false)
                                    :content   (accounts-list accounts account
                                                              change-account)}]]
       [list-item/list-item
        {:icon     [chat-icon/custom-icon-view-list (:name account) (:color account)]
         :title    (:name account)
         :subtitle (utils/get-shortened-checksum-address (:address account))
         :on-press #(reset! visible true)}]])))

(defn- referral-sheet []
  (let [account* (reagent/atom nil)]
    (fn []
      (let [accounts        @(re-frame/subscribe [:accounts-without-watch-only])
            default-account @(re-frame/subscribe [:multiaccount/default-account])
            account         (or @account* default-account)]
        [rn/view {:flex 1}
         [referral-steps]
         [referral-account {:account        account
                            :change-account #(reset! account* %)
                            :accounts       accounts}]
         [toolbar/toolbar {:show-border? true
                           :center       {:label    "Invite"
                                          :type     :secondary
                                          :on-press #(re-frame/dispatch [::events/generate-invite
                                                                         {:address (get account :address)}])}}]]))))

(defn invite-button []
  (let [visible (reagent/atom false)]
    (fn []
      [:<>
       [rn/modal {:visible     @visible
                  :transparent true}
        [bottom-sheet/bottom-sheet {:show?     true
                                    :on-cancel #(reset! visible false)
                                    :content   referral-sheet}]]
       [rn/view {:style (merge (:tiny spacing/padding-vertical)
                               {:align-items :center})}
        [button/button {:label               :t/invite-friends
                        :on-press            #(reset! visible true)
                        :accessibility-label :invite-friends-button}]]])))

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
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [quo.design-system.spacing :as spacing]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.invite.events :as events]
            [quo.react-native :as rn]))

;; Select account sheet
(defn- render-account [current-account change-account]
  (fn [account]
    (let [{:keys [max-threshold attrib-count]}
          @(re-frame/subscribe [:invite/account-reward account])]
     [list-item/list-item
      {:theme     :selectable
       :selected? (= (:address current-account) (:address account))
       :disabled? (and max-threshold attrib-count
                       (< max-threshold (inc attrib-count)))
       :icon      [chat-icon/custom-icon-view-list (:name account) (:color account)]
       :title     (:name account)
       :subtitle  (utils/get-shortened-checksum-address (:address account))
       :on-press  #(change-account account)}])))

(defn- accounts-list [accounts current-account change-account]
  (fn []
    [rn/view {:flex 1}
     [rn/view {:style (merge (:base spacing/padding-horizontal)
                             (:tiny spacing/padding-vertical))}
      [quo/text {:align :center}
       (i18n/label :t/invite-select-account)]]
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
    [quo/text (i18n/label description)]]])

(def steps-values [{:number      1
                    :description :t/invite-instruction-first}
                   {:number      2
                    :description :t/invite-instruction-second}
                   {:number      3
                    :description :t/invite-instruction-third}
                   {:number      4
                    :description :t/invite-instruction-fourth}])

(defn- referral-steps []
  [rn/view {:style (merge
                    (:tiny spacing/padding-vertical)
                    (:base spacing/padding-horizontal)
                    {:border-bottom-width 1
                     :border-bottom-color (:ui-01 @colors/theme)})}
   [rn/view {:style {:padding-top    (:small spacing/spacing)
                     :padding-bottom (:x-tiny spacing/spacing)}}
    [quo/text {:color :secondary}
     (i18n/label :t/invite-instruction)]]
   [rn/view {:flex 1}
    (for [s steps-values]
      ^{:key (str (:number s))}
      [step s])]])

(defn- referral-account []
  (let [visible (reagent/atom false)]
    (fn [{:keys [account accounts change-account]}]
      [rn/view {:style (:tiny spacing/padding-vertical)}
       [rn/view {:style (merge (:base spacing/padding-horizontal)
                               (:x-tiny spacing/padding-vertical))}
        [quo/text {:color :secondary}
         (i18n/label :t/invite-receive-account)]]
       [rn/modal {:visible     @visible
                  :transparent true}
        [bottom-sheet/bottom-sheet {:show?     true
                                    :on-cancel #(reset! visible false)
                                    :content   (accounts-list accounts account
                                                              (fn [a]
                                                                (change-account a)
                                                                (reset! visible false)))}]]
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
                           :center       {:label    (i18n/label :t/invite-button)
                                          :type     :secondary
                                          :on-press #(re-frame/dispatch [::events/generate-invite
                                                                         {:address (get account :address)}])}}]]))))

(defn- invite []
  (let [visible  (reagent/atom false)
        on-press (fn []
                   (reset! visible true)
                   (re-frame/dispatch [::events/get-accounts-reward]))]
    (fn [{:keys [component props]}]
      [:<>
       [rn/modal {:visible     @visible
                  :transparent true}
        [bottom-sheet/bottom-sheet {:show?     true
                                    :on-cancel #(reset! visible false)
                                    :content   referral-sheet}]]
       [component {:on-press on-press
                   :props    props}]])))

(defn- button-component [{:keys [on-press]}]
  (let [amount @(re-frame/subscribe [::events/default-reward])]
    [rn/view {:style {:align-items :center}}
     [rn/view {:style (:tiny spacing/padding-vertical)}
      [button/button {:label               :t/invite-friends
                      :on-press            on-press
                      :accessibility-label :invite-friends-button}]]
     [rn/view {:style (merge (:tiny spacing/padding-vertical)
                             (:base spacing/padding-horizontal))}
      (when amount
        [rn/view {:style {:flex-direction  :row
                          :align-items     :center
                          :justify-content :center}}
         [rn/view {:style (:tiny spacing/padding-horizontal)}
          (when-let [{:keys [source]} (tokens/symbol->icon :SNT)]
            [rn/image {:style  {:width  20
                                :height 20}
                       :source (source)}])]
         [quo/text {:align :center}
          (i18n/label :t/invite-reward {:value (money/wei->str :eth (* 1000000000000000 amount) "SNT")})]])]]))

(defn- list-item-component [{:keys [on-press props]}]
  (let [amount @(re-frame/subscribe [::events/default-reward])]
    [list-item/list-item
     {:theme               :action
      :title               (i18n/label :t/invite-friends)
      :subtitle            (i18n/label :t/invite-reward {:value (money/wei->str :eth amount "SNT")})
      :icon                :main-icons/share
      :accessibility-label (:accessibility-label props)
      :on-press            on-press}]))

(defn button []
  [invite {:component button-component}])

(defn list-item [props]
  [invite {:component list-item-component
           :props     props}])

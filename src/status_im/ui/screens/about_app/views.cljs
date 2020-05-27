(ns status-im.ui.screens.about-app.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [quo.core :as quo]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.about-app.styles :as styles])
  (:require-macros [status-im.utils.views :as views]))

(defn- data [app-version node-version]
  [{:size                :small
    :title               (i18n/label :t/privacy-policy)
    :accessibility-label :privacy-policy
    :on-press
    #(re-frame/dispatch
      [:privacy-policy/privacy-policy-button-pressed])
    :chevron             true}
   [copyable-text/copyable-text-view
    {:copied-text app-version}
    [quo/list-item
     {:size                :small
      :accessibility-label :app-version
      :title               (i18n/label :t/version)
      :accessory           :text
      :accessory-text      app-version}]]
   [copyable-text/copyable-text-view
    {:copied-text node-version}
    [quo/list-item
     {:size                :small
      :accessibility-label :node-version
      :title               (i18n/label :t/node-version)
      :acccessory          :text
      :accessory-text      node-version}]]])

(views/defview about-app []
  (views/letsubs [app-version  [:get-app-short-version]
                  node-version [:get-app-node-version]]
    [react/view {:flex 1 :background-color colors/white}
     [topbar/topbar {:title :t/about-app}]
     [list/flat-list
      {:data      (data app-version node-version)
       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]]))

(views/defview learn-more-sheet []
  (views/letsubs [{:keys [title content]} [:bottom-sheet/options]]
    [react/view {:style {:padding-left 16 :padding-top 16
                         :padding-right 34 :padding-bottom 0}}
     [react/view {:style {:align-items :center :flex-direction :row :margin-bottom 16}}
      [vector-icons/icon :main-icons/info {:color colors/blue
                                           :container-style {:margin-right 13}}]
      [react/text {:style styles/learn-more-title} title]]
     [react/text {:style styles/learn-more-text} content]]))

(def learn-more
  {:content learn-more-sheet})

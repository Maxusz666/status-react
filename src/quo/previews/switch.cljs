(ns quo.previews.switch
  (:require [reagent.core :as reagent]
            [quo.core :as quo]
            [quo.react-native :as rn]
            [quo.design-system.colors :as colors]))

(defn preview []
  (let [state (reagent/atom true)]
    (fn []
      [rn/view {:background-color (:ui-background @colors/theme)
                :flex             1}
       [rn/view {:padding         24
                 :flex            1
                 :justify-content :center
                 :align-items     :center}
        [rn/touchable-opacity {:style {:margin 10
                                       :padding 10}
                               :on-press #(swap! state not)}
         [quo/text (str "Switch state:" @state)]]
        [quo/switch {:value     @state
                     :on-change #(reset! state %)}]]])))

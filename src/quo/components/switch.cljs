(ns quo.components.switch
  (:require [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo.react :as react]
            [quo.animated :as animated]
            [quo.gesture-handler :as gh]
            [quo.design-system.colors :as colors]))

(def spring-config {:damping                   50
                    :mass                      0.3
                    :stiffness                 120
                    :overshootClamping         true
                    :bouncyFactor              1})

(defn switch-style [state]
  {:width            52
   :height           28
   :border-radius    14
   :padding          4
   :background-color (animated/mix-color state
                                         (:ui-01 @colors/theme)
                                         (:interactive-01 @colors/theme))})

(defn bullet-style [state hold]
  {:width            20
   :height           20
   :border-radius    10
   :opacity          (animated/mix hold 1 0.6)
   :transform        [{:translateX (animated/mix state 0 24)}]
   :background-color colors/white
   :elevation        4
   :shadow-opacity   1
   :shadow-radius    16
   :shadow-color     (:shadow-01 @colors/theme)
   :shadow-offset    {:width 0 :height 4}})

(defn switch-hooks [props]
  (let [{:keys [value onChange disabled]}
        (bean/bean props)
        state       (animated/use-value 0)
        tap-state   (animated/use-value (:undetermined gh/states))
        tap-handler (animated/on-gesture {:state tap-state})
        hold        (react/use-memo
                     (fn []
                       (animated/with-timing-transition
                         (animated/eq tap-state (:began gh/states))
                         {}))
                     [])
        transition  (react/use-memo
                     (fn []
                       (animated/with-spring-transition state spring-config))
                     [])
        press-end   (fn []
                      (when (and (not disabled) onChange)
                        (onChange (not value))))]
    (animated/code!
     (fn []
       (animated/cond* (animated/eq tap-state (:end gh/states))
                       [(animated/set state (animated/not* state))
                        (animated/set tap-state (:undetermined gh/states))
                        (animated/call* [] press-end)]))
     [press-end])
    (animated/code!
     (fn []
       (animated/set state (if (true? value) 1 0)))
     [value])
    (reagent/as-element
     [gh/tap-gesture-handler (merge tap-handler
                                    {:shouldCancelWhenOutside true
                                     :enabled                 (not disabled)})
      [animated/view {:style (switch-style transition)}
       [animated/view {:style (bullet-style transition hold)}]]])))

(def switch (reagent/adapt-react-class switch-hooks))

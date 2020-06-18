(ns quo.components.animated.pressable
  (:require [quo.animated :as animated]
            [quo.react :as react]
            [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo.gesture-handler :as gesture-handler]))

(def long-press-duration 500)
(def scale-down-small 0.95)
(def scale-down-large 0.9)
(def opactiy 0.75)
(def time-in 100)
(def time-out 200)

(defmulti type->animation :type)

(defmethod type->animation :primary
  [{:keys [animation]}]
  {:background {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(defmethod type->animation :secondary
  [{:keys [animation]}]
  {:background {:transform [{:scale (animated/mix animation scale-down-small 1)}]
                :opacity   (animated/mix animation 0 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(defmethod type->animation :icon
  [{:keys [animation]}]
  {:background {:transform [{:scale (animated/mix animation scale-down-large 1)}]
                :opacity   (animated/mix animation 0 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-large)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(defmethod type->animation :list-item
  [{:keys [animation]}]
  {:background {:opacity (animated/mix animation 0 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(defmethod type->animation :scale
  [{:keys [animation]}]
  {:background {:opacity 0}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(def absolute-fill
  {:top      0
   :bottom   0
   :left     0
   :right    0
   :position :absolute})

(defn pressable-hooks [props]
  (let [{background-color    :backgroundColor
         border-radius       :borderRadius
         type                :type
         disabled            :disabled
         on-press            :onPress
         on-long-press       :onLongPress
         on-press-start      :onPressStart
         accessibility-label :accessibilityLabel
         children            :children
         :or                 {border-radius 0
                              type          "primary"}}
        (bean/bean props)
        long-press-ref       (react/create-ref)
        state                (animated/use-value (:undetermined gesture-handler/states))
        long-state           (animated/use-value (:undetermined gesture-handler/states))
        active               (animated/eq state (:began gesture-handler/states))
        gesture-handler      (animated/on-gesture {:state state})
        long-gesture-handler (animated/on-gesture {:state long-state})
        duration             (animated/cond* active time-in time-out)
        animation            (animated/with-timing-transition active
                               {:duration duration
                                :easing   (:ease-in animated/easings)})
        {:keys [background foreground]}
        (type->animation {:type      (keyword type)
                          :animation animation})
        handle-press         (fn [] (when on-press (on-press)))
        handle-press-start   (fn [] (when on-press-start (on-press-start)))
        handle-long-press    (fn [] (when on-long-press (on-long-press)))]
    (animated/code!
     (fn []
       (when on-long-press
         (animated/cond* (animated/eq long-state (:active gesture-handler/states))
                         [(animated/call* [] handle-long-press)
                          (animated/set state (:undetermined gesture-handler/states))])))
     [on-long-press])
    (animated/code!
     (fn []
       (animated/block
        [(animated/cond* (animated/eq state (:began gesture-handler/states))
                         (animated/call* [] handle-press-start))
         (animated/cond* (animated/eq state (:end gesture-handler/states))
                         [(animated/set state (:undetermined gesture-handler/states))
                          (animated/call* [] handle-press)])]))
     [on-press on-long-press on-press-start])
    (reagent/as-element
     [gesture-handler/long-press-gesture-handler
      (merge long-gesture-handler
             {:enabled         (and on-long-press (not disabled))
              :min-duration-ms long-press-duration
              :ref             long-press-ref})
      [animated/view {:accessible          true
                      :accessibility-label accessibility-label}
       [gesture-handler/tap-gesture-handler
        (merge gesture-handler
               {:shouldCancelWhenOutside true
                ;; :wait-for                long-press-ref
                :enabled                 (and (or on-press on-long-press on-press-start)
                                              (not disabled))})
        [animated/view
         [animated/view {:style (merge absolute-fill
                                       background
                                       {:background-color background-color
                                        :border-radius    border-radius})}]
         (into [animated/view {:style foreground}]
               (react/get-children children))]]]])))

(def pressable (reagent/adapt-react-class pressable-hooks))

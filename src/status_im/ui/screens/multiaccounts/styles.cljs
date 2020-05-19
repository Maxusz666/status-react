(ns status-im.ui.screens.multiaccounts.styles
  (:require [status-im.ui.components.colors :as colors]))

(def multiaccounts-view
  {:flex 1})

(def multiaccounts-container
  {:flex               1
   :margin-top         24
   :justify-content    :space-between})

(def multiaccount-image-size 40)

(def multiaccounts-list-container
  {:flex             1
   :padding-bottom 8})

(def multiaccount-view
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :height             64})

(def multiaccount-badge-text-view
  {:margin-left  16
   :margin-right 31
   :flex-shrink  1})

(def multiaccount-badge-text
  {:font-weight "500"})

(def multiaccount-badge-pub-key-text
  {:font-family "monospace"
   :color       colors/gray})

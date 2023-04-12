//
//  UISegmentedControl.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 06.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//
import SwiftUI

extension UISegmentedControl {
  override open func didMoveToSuperview() {
     super.didMoveToSuperview()
     self.setContentHuggingPriority(.defaultLow, for: .vertical)
   }
}

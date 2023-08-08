//
//  AppVersion.swift
//  More
//
//  Created by Jan Cortiel on 08.08.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct AppVersion: View {
    var body: some View {
        Text("\("App Version".localize(withComment: "App Version")): \(Bundle.main.appBuild)")
            .font(.system(size: 10, weight: .medium))
            .foregroundColor(.more.primary)
            .padding(.vertical, 10)
    }
}

struct AppVersion_Previews: PreviewProvider {
    static var previews: some View {
        AppVersion()
    }
}

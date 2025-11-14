//
//  AppVersion.swift
//  More
//
//  Created by Jan Cortiel on 08.08.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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

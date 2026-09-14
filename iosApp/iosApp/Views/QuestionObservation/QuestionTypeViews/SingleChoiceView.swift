//
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct SingleChoiceView: View {
    @ObservedObject var viewModel: QuestionViewModel
    @Binding var selected: String?

    var body: some View {
        VStack(alignment: .leading) {
            ForEach(viewModel.answers, id: \.self) { answerOption in
                RadioButtonField(
                    id: answerOption,
                    label: answerOption,
                    isMarked: selected == answerOption,
                    callback: { selectedId in
                        if selected == selectedId {
                            selected = nil
                        } else {
                            selected = selectedId
                        }
                    }
                )
            }
        }
    }
}

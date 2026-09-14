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

struct MultiChoiceView: View {
    @ObservedObject var viewModel: QuestionViewModel
    @Binding var selected: Set<String>

    var body: some View {
        VStack(alignment: .leading) {
            ForEach(viewModel.answers, id: \.self) { answerOption in
                CheckboxField(
                    id: answerOption,
                    label: answerOption,
                    isSelected: selected.contains(answerOption),
                    callback: { toggledId in
                        if selected.contains(toggledId) {
                            selected.remove(toggledId)
                        } else {
                            selected.insert(toggledId)
                        }
                    }
                )
            }
        }
    }
}

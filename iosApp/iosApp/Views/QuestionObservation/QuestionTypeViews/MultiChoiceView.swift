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

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

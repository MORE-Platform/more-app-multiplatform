//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    let recorder = AppDelegate.recorder
    let scheduleListType: ScheduleListType
    private let coreModel: CoreScheduleViewModel

    let filterViewModel: DashboardFilterViewModel = DashboardFilterViewModel()
    @Published var filterText: String = String.localizedString(forKey: "no_filter_activated", inTable: "DashboardFilter", withComment: "String for no filter set")
    @Published var schedules: [Int64: [ScheduleModel]] = [:] {
        didSet {
            scheduleDates = Array(schedules.keys.sorted())
        }
    }

    @Published var scheduleDates: [Int64] = []

    init(observationFactory: IOSObservationFactory, scheduleListType: ScheduleListType) {
        self.scheduleListType = scheduleListType
        self.coreModel = CoreScheduleViewModel(dataRecorder: recorder, scheduleListType: scheduleListType, coreFilterModel: filterViewModel.coreModel)
        self.loadSchedules()
        self.filterViewModel.delegate = self
    }

    func start(scheduleId: String) {
        coreModel.start(scheduleId: scheduleId)
    }

    func pause(scheduleId: String) {
        coreModel.pause(scheduleId: scheduleId)
    }

    func stop(scheduleId: String) {
        coreModel.stop(scheduleId: scheduleId)
    }

    func loadSchedules() {
        coreModel.onScheduleModelListChange { [weak self] scheduleMap in
            if let self {
                self.schedules = scheduleMap.reduce([:]) { partialResult, pair -> [Int64: [ScheduleModel]] in
                    var result = partialResult
                    result[Int64(truncating: pair.key)] = pair.value
                    return result
                }
            }
        }
    }
    
    func viewDidAppear() {
        coreModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }
}

extension Dictionary<KotlinLong, [ScheduleModel]> {
    func convertToInt64() -> [Int64: [ScheduleModel]] {
        reduce([:]) { partialResult, pair -> [Int64: [ScheduleModel]] in
            var result = partialResult
            result[Int64(truncating: pair.key)] = pair.value
            return result
        }
    }
}

extension Dictionary<Int64, [ScheduleModel]> {
    func convertToKotlinLong() -> [KotlinLong: [ScheduleModel]] {
        reduce([:]) { partialResult, pair -> [KotlinLong: [ScheduleModel]] in
            var result = partialResult
            result[KotlinLong(value: pair.key)] = pair.value
            return result
        }
    }
}

extension ScheduleViewModel: DashboardFilterObserver {
    
    func onFilterChanged(multiSelect: Bool, filter: String, list: [String], stringTable: String) -> [String] {
        var selectedValueList = list
        if multiSelect {
            if filter == String.localizedString(forKey: "All Items", inTable: stringTable, withComment: "String for All Items") {
                filterViewModel.observationTypeFilter.removeAll()
            } else {
                if filterViewModel.observationTypeFilter.contains(filter) {
                    filterViewModel.observationTypeFilter.remove(at: filterViewModel.observationTypeFilter.firstIndex(of: filter)!)
                } else {
                    filterViewModel.observationTypeFilter.append(filter)
                }
            }
            selectedValueList = filterViewModel.observationTypeFilter
            filterViewModel.setObservationTypeFilters()
        } else {
            if !selectedValueList.isEmpty {
                selectedValueList.removeAll()
            }
            selectedValueList.append(filter)
            filterViewModel.dateFilterString = filter
            filterViewModel.setDateFilterValue()
        }
        return selectedValueList
    }
    
    func updateFilterText() -> String {
        var stringTable = "DashboardFilter"
        var typeFilterText = ""
        var dateFilterText = ""
        if noFilterSet() {
            return String.localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "No filter set")
        } else {
            if typeFilterSet() {
                if filterViewModel.observationTypeFilter.count == 1 {
                    typeFilterText = "\(filterViewModel.observationTypeFilter.count) \(String.localizedString(forKey: "type", inTable: stringTable, withComment: "Observation type"))"
                } else {
                    typeFilterText = "\(filterViewModel.observationTypeFilter.count) \(String.localizedString(forKey: "type_plural", inTable: stringTable, withComment: "Observation types"))"
                }
            }
            if dateFilterSet() {
                dateFilterText = String.localizedString(forKey: filterViewModel.dateFilterString, inTable: stringTable, withComment: "Time filter")
            }
            if dateFilterSet() && typeFilterSet() {
                return "\(typeFilterText), \(dateFilterText)"
            } else if dateFilterSet(){
                return dateFilterText
            } else {
                return typeFilterText
            }
        }
    }
    
    func dateFilterSet() -> Bool {
        return filterViewModel.dateFilterString != "ENTIRE_TIME"
    }
    
    func typeFilterSet() -> Bool {
        return !filterViewModel.observationTypeFilter.isEmpty
    }
    
    func noFilterSet() -> Bool {
        return !dateFilterSet() && !typeFilterSet()
    }
}

//
//  Polar360Queue.swift
//  iosApp
//
//  Created by Masek Gergely on 30.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

class Polar360Queue<T>{
    private let maxSize: Int
    private var deque: [T] = []
    
    init(maxSize: Int) {
        precondition(maxSize > 0, "maxSize must be greater than 0")
        self.maxSize = maxSize
    }
    func add(_ item: T) {
            if deque.count == maxSize {
                deque.removeFirst()
            }
            deque.append(item)
        }
        
        /// Get and remove the most recent item
        func pollLast() -> T? {
            return deque.popLast()
        }
        
        /// Get and remove the most recent [count] items
        func pollLast(_ count: Int) -> [T] {
            var result: [T] = []
            let n = min(count, deque.count)
            for _ in 0..<n {
                if let last = deque.popLast() {
                    result.append(last)
                }
            }
            return result
        }
        
        /// Peek at the most recent item without removing
        func peekLast() -> T? {
            return deque.last
        }
        
        /// Current snapshot
        func toList() -> [T] {
            return deque
        }
        
        func size() -> Int {
            return deque.count
        }
        
        func clear() {
            deque.removeAll()
        }
}

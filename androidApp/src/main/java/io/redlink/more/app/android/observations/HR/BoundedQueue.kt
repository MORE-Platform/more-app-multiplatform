package io.redlink.more.app.android.observations.HR

class BoundedQueue<T>(private val maxSize: Int) {
    private val deque: ArrayDeque<T> = ArrayDeque()

    /** Add new item, dropping oldest if full */
    fun add(item: T) {
        if (deque.size == maxSize) {
            deque.removeFirst()
        }
        deque.addLast(item)
    }

    /** Get and remove the most recent item */
    fun pollLast(): T? = if (deque.isNotEmpty()) deque.removeLast() else null

    /** Get and remove the most recent [count] items */
    fun pollLast(count: Int): List<T> {
        val result = mutableListOf<T>()
        repeat(count.coerceAtMost(deque.size)) {
            result.add(deque.removeLast())
        }
        return result
    }

    /** Peek at the most recent item without removing */
    fun peekLast(): T? = deque.lastOrNull()

    /** Current snapshot */
    fun toList(): List<T> = deque.toList()

    fun size(): Int = deque.size
    fun clear() = deque.clear()

}
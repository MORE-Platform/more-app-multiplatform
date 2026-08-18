package io.redlink.more.events

expect class DayMonitor(
    onEvent: (AppEvent) -> Unit
) {
    fun start()
    fun stop()
    fun refresh()
}

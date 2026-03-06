package io.redlink.more.scopes

interface StudyMoreScope : MoreScope {
    override fun cancel(uuid: String)
    override fun cancel(uuids: Collection<String>)
    override fun cancel()
}
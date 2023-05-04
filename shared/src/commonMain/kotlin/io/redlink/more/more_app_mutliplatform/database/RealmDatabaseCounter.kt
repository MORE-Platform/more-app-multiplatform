package io.redlink.more.more_app_mutliplatform.database

import io.github.aakira.napier.Napier

data class RealmDatabaseCounter(var counter: Int = 0){
    fun increment() {
        counter++
        Napier.d { "RealmRepositoryCounter: $counter" }
    }

    fun decrement() {
        counter--
        Napier.d { "RealmRepositoryCounter: $counter" }
    }
}
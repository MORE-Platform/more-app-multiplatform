package io.redlink.more.more_app_mutliplatform.extensions

infix fun (() -> Unit).then(after: () -> Unit): () -> Unit = { this(); after() }
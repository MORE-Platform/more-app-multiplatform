package io.redlink.more.extensions

infix fun (() -> Unit).then(after: () -> Unit): () -> Unit = { this(); after() }
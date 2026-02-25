package io.redlink.umm.participant.extensions

infix fun (() -> Unit).then(after: () -> Unit): () -> Unit = { this(); after() }
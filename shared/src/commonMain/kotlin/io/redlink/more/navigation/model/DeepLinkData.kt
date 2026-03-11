package io.redlink.more.navigation.model

data class DeepLinkData(
    val route: String,
    val params: Map<String, String?> = emptyMap()
)

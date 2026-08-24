package io.redlink.more.extensions

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

fun StringResource.desc(): StringDesc = StringDesc.Resource(this)

fun StringResource.formatted(list: List<Any>): StringDesc = StringDesc.ResourceFormatted(this, list)

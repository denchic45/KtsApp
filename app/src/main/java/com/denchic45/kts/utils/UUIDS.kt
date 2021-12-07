package com.denchic45.kts.utils

import java.util.*

object UUIDS {
    fun createShort(): String = UUID.randomUUID().toString().substring(0, 13)
}
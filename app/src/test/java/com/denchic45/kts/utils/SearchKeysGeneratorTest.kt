package com.denchic45.kts.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SearchKeysGeneratorTest {

    @Test
    fun testTwoWords() {
        SearchKeysGenerator().generateKeys("Вася Пупкин Петрович").forEach {
            println(it)
        }
    }
}
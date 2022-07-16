package com.denchic45.kts.util

import org.junit.jupiter.api.Test

internal class SearchKeysGeneratorTest {

    @Test
    fun testTwoWords() {
        SearchKeysGenerator().generateKeys("Вася Пупкин Петрович").forEach {
            println(it)
        }
    }
}
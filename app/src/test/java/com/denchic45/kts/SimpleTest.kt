package com.denchic45.kts

import com.google.firebase.FirebaseException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test


class SimpleTest {

    @Test
    fun test(): Unit = runBlocking {
        emptyFlow<String>()
            .onStart { println("Start Begin!") }
            .collect { println(it) } // prints Begin, a, b, c
    }

    @Test
    fun testPlusMap() {
        val map:Map<String,Boolean> = emptyMap()

        val map1 = map + Pair("lol", false)

        val map2 = map1 + Pair("lol", true)

        println("Map")
        println(map2)
    }


}
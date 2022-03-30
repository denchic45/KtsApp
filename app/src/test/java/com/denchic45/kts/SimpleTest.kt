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


}
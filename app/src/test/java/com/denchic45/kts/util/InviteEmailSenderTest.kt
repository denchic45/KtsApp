package com.denchic45.kts.util

import org.junit.jupiter.api.Test

internal class InviteEmailSenderTest {

    @Test
    fun sendTest() {
        InviteEmailSender().send("denchic860@gmail.com")
    }
}
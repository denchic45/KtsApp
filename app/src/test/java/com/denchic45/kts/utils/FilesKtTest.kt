package com.denchic45.kts.utils

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.io.File

internal class FilesKtTest {

    @Test
    fun getMimeType() {
        assertEquals("image/png", File("sample.png").getMimeType())
        assertEquals("audio/mpeg", File("sample.mp3").getMimeType())
        assertEquals("audio/ogg", File("sample.ogg").getMimeType())
        assertEquals("application/msword", File("sample.docx").getMimeType())
    }
}
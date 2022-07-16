package com.denchic45.kts.util

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.io.File

internal class FilesKtTest {

    @Test
    fun getMimeType() {
        assertEquals("image", File("sample.png").getMimeType())
        assertEquals("audio", File("sample.mp3").getMimeType())
        assertEquals("audio", File("sample.ogg").getMimeType())
        val mimeType = File("sample.docx").getMimeType()
        assertEquals("application", mimeType)
    }
}
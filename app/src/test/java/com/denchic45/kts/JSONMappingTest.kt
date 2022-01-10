package com.denchic45.kts

import com.denchic45.kts.data.model.domain.ContentDetails
import com.denchic45.kts.data.model.domain.MarkType
import com.denchic45.kts.data.model.mapper.CourseContentMapper
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test

class JSONMappingTest {

    val gson = GsonBuilder().registerTypeAdapter(MarkType::class.java, CourseContentMapper.MarkTypeDeserializer())
        .create()

    @Test
    fun jsonTest() {
        val json = "{\n" +
                "  \"disabledSendAfterDate\":false,\"attachments\":[\"/storage/emulated/0/Download/file.gif\"],\"sendDate\":{\"nanoseconds\":0,\"seconds\":1641772740},\"markType\":{\"position\":0,\"maxScore\":10},\"answerType\":{\"attachmentsSizeLimit\":200,\"attachmentsLimit\":16,\"charsLimit\":500,\"attachmentsAvailable\":false,\"textAvailable\":true},\"completed\":true}"
        val obj = gson.fromJson(json, ContentDetails.Task::class.java)
        println("chars lim: ${(obj.markType as MarkType.Score).maxScore}")

    }


}


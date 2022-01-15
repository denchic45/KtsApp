package com.denchic45.kts

import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.utils.MembersComparator
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.full.memberProperties

class ReflectionTest {

    val user1 = User(
        firstName = "Ivan",
        surname = "Ivanov",
        patronymic = "Ivanich",
        timestamp = Date(),
        photoUrl = "",
        gender = 1,
        role = "",
        admin = false,
        email = "",
        generatedAvatar = true,
        groupId = "",
        phoneNum = ""
    )

    val user2 = user1.copy(
        firstName = "Anton",
        gender = 2,
        timestamp = Date(),
        admin = true,
    )

    val task1 = Task(
        id = "uuid",
        courseId = "",
        timestamp = Date(),
        name = "",
        order = 0,
        answerType = AnswerType(
            textAvailable = true,
            charsLimit = 1,
            attachmentsAvailable = true,
            attachmentsLimit = 1,
            attachmentsSizeLimit = 1
        ),
        attachments = emptyList(),
        commentsEnabled = true,
        completionDate = LocalDateTime.now(),
        createdDate = Date(),
        description = "",
        disabledSendAfterDate = false,
        markType = MarkType.Binary,
        sectionId = ""
    )

    val task2 = task1.copy(
        answerType = AnswerType(
            textAvailable = true,
            charsLimit = 2,
            attachmentsAvailable = false,
            attachmentsLimit = 2,
            attachmentsSizeLimit = 1
        ),
        markType = MarkType.Binary,
        attachments = listOf(
            Attachment(
                file = File("sample.png")
            )
        ),
        timestamp = Date()
    )


    @Test
    fun test() {
        User::class.memberProperties.forEach {
            println("equals: ${it.name} ${it.get(user1) == it.get(user2)}")
        }
    }

    @Test
    fun test2() {
        val hashmap = MembersComparator.mapOfDifference(task1, task2)
        println(hashmap)
        println(hashmap["timestamp"]!!.javaClass)
    }
}
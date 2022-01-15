package com.denchic45.kts

import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.utils.CourseContents
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class SortContentsTest {

    private val courseId = "courseId"

    @Test
    fun test() {

        val sectionId1 = "vc94vcf"
        val sectionId2 = "c34fhm9"
        val sectionId3 = "h43grt95f"

        val sections = listOf(
            Section(sectionId3, courseId, "Some Section", 3),
            Section(sectionId1, courseId, "Divider", 1),
            Section(sectionId2, courseId, "Other", 2)
        )

        val contents = listOf(
            createContent("Part 1", sectionId2, 1),
            createContent("Part 0", sectionId2, 0),
            createContent("Material 0", sectionId3, 0),
            createContent("Task 1", sectionId1, 1),
            createContent("Task 2", sectionId1, 2),
            createContent("Task 0", sectionId1, 0),
            createContent("Task 0", "", 0),
        )

        val sortedList = CourseContents.sort(contents, sections)

        sortedList.forEach {
            when (it) {
                is CourseContent -> {
                    println("   Content: ${it.name} section: ${it.sectionId} order: ${it.order}")
                }
                is Section -> {
                    println("Section: ${it.name} section: ${it.id} order: ${it.order}")
                }
            }
        }
    }


    private fun createContent(name: String, sectionId: String, order: Int) =
        Task(
            "",
            courseId,
            sectionId,
            name,
            "",
            order,
            LocalDateTime.now(),
            false,
            emptyList(),
            AnswerType(false, 0, false, 0, 0),
            MarkType.Binary,
            false,
            Date(),
            Date()
        )
}
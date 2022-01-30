package com.denchic45.kts.utils

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.CourseContent
import com.denchic45.kts.data.model.domain.Section

object CourseContents {

    fun sort(
        contents: List<CourseContent>,
        sections: List<Section>
    ): List<DomainModel> {
        val sectionsById = sections.associateBy { it.id }

        return (sections + contents).groupBy {
            when (it) {
                is Section -> it.id
                is CourseContent -> it.sectionId
                else -> throw IllegalStateException()
            }
        }
            .toSortedMap(compareBy { (sectionsById[it] ?: Section.createEmpty()).order })
            .flatMap {
                        it.value.sortedWith { o1, o2 ->
                            when {
                                o1 is Section && o2 is CourseContent -> -1
                                o1 is CourseContent && o2 is Section -> 1
                                o1 is CourseContent && o2 is CourseContent -> o1.order.compareTo(o2.order)
                                else -> throw IllegalStateException()
                            }
                        }
            }
    }
}
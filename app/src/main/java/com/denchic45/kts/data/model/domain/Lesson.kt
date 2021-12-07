package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.room.EventEntity.TYPE

data class Lesson(
    val subject: Subject,
    val teachers: List<User>,
    val task: Task? = null,
) : EventDetails() {

    val isEmpty: Boolean
        get() = subject.isEmpty

    private constructor() : this(Subject.createEmpty(), emptyList(), Task.createEmpty())

    override fun copy(): Lesson {
        return Lesson(subject, teachers, task)
    }

    override val type: TYPE
        get() = TYPE.LESSON

    companion object {
        fun createEmpty(): Lesson {
            return Lesson()
        }
    }
}
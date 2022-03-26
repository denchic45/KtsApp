package com.denchic45.kts

import java.text.SimpleDateFormat
import java.util.*

class LessonTimeCalculator {
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("HH:mm")
    private var orderLesson = 0
    private var lessonTime = 0
    fun getCalculatedTime(orderLesson: Int, lessonTime: Int): String {
        this.orderLesson = orderLesson
        this.lessonTime = lessonTime
        calendar[Calendar.HOUR_OF_DAY] = 7
        calendar[Calendar.MINUTE] = 40
        return "${getStartTime()}-${getEndTime()}"
    }

    private fun getStartTime(): String {
        calendar.add(Calendar.MINUTE, lessonTime * orderLesson + orderLesson * BREAK_TIME)
        if (orderLesson == 1) {
            calendar[Calendar.HOUR] = 8
            calendar[Calendar.MINUTE] = 30
        } else if (orderLesson > 1) {
            calendar.add(Calendar.MINUTE, BREAK_TIME * 2)
        }
        dateFormat.format(calendar.time)
        return dateFormat.format(calendar.time)
    }

    private fun getEndTime(): String {
        if (orderLesson == 0) {
            calendar.add(Calendar.MINUTE, +40)
        } else {
            calendar.add(Calendar.MINUTE, +lessonTime)
        }
        if (orderLesson == 0) {
            calendar.add(Calendar.MINUTE, BREAK_TIME)
        }
        return dateFormat.format(calendar.time)
    }

    companion object {
        private const val BREAK_TIME = 5
    }
}
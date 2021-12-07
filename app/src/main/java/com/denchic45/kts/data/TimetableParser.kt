package com.denchic45.kts.data

import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.domain.Event.Companion.empty
import com.denchic45.kts.data.model.domain.SimpleEventDetails.Companion.dinner
import com.denchic45.kts.data.model.domain.SimpleEventDetails.Companion.practice
import com.denchic45.kts.utils.DateFormatUtil
import com.denchic45.kts.utils.Events
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

class TimetableParser(getSpecialSubjects: Single<List<Subject>>) {
    private val days = listOf(
        "ПОНЕДЕЛЬНИК ",
        "ВТОРНИК",
        "СРЕДА",
        "ЧЕТВЕРГ",
        "ПЯТНИЦА",
        "СУББОТА"
    )
    private val specialSubjects: MutableList<Subject> = ArrayList()
    private var cellOfGroupPos = 0
    private lateinit var table: XWPFTable
    private var currentRow = 0
    private var currentDayOfWeek = 0
    private var currentGroup: GroupWithCourses? = null
    private lateinit var emitter: SingleEmitter<List<GroupWeekLessons>>
    fun parseDoc(
        docFile: File,
        callbackGroupInfo: Function<Int, Single<List<GroupWithCourses>>>
    ): Single<List<GroupWeekLessons>> {
        return Single.create { emitter: SingleEmitter<List<GroupWeekLessons>> ->
            this@TimetableParser.emitter = emitter
            val wordDoc: XWPFDocument = try {
                XWPFDocument(FileInputStream(docFile))
            } catch (e: IOException) {
                e.printStackTrace()
                emitter.onError(Exception("Произошла проблема при извлечении документа"))
                return@create
            }
            table = wordDoc.tables[0]
            val cellsInGroups: MutableList<XWPFTableCell> = ArrayList()
            cellsInGroups.addAll(table.getRow(1).tableCells)
            cellsInGroups.addAll(table.getRow(2).tableCells)
            val groupWeekLessonsList: MutableList<GroupWeekLessons> = ArrayList()
            callbackGroupInfo.apply(findCourseNumber())
                .subscribe { groupWithCourses: List<GroupWithCourses> ->
                    val cellsCount = table.getRow(1).tableCells.size
                    for (group in groupWithCourses) {
                        if (emitter.isDisposed) return@subscribe
                        currentGroup = group
                        cellOfGroupPos = cellsInGroups.indexOf(
                            getCellByGroupName(
                                cellsInGroups,
                                group.group.name
                            )
                        )
                        if (cellOfGroupPos == -1) continue
                        cellOfGroupPos =
                            if (cellOfGroupPos > cellsCount) cellOfGroupPos - cellsCount else cellOfGroupPos
                        groupWeekLessonsList.add(GroupWeekLessons(group.group, lessonsOfGroup))
                    }
                    if (emitter.isDisposed) return@subscribe
                    emitter.onSuccess(groupWeekLessonsList)
                }
        }
    }

    private fun findCourseNumber(): Int {
        val text = table.getRow(0).getCell(0).text
        return text.replace(Regex("[^0-9]+"), "").toInt()
    }

    private fun getCellByGroupName(
        tableCells: List<XWPFTableCell>,
        groupName: String
    ): XWPFTableCell? {
        return tableCells.stream()
            .filter { cell: XWPFTableCell ->
                val text = cell.text
                resetFormatting(text).contains(resetFormatting(groupName))
            }
            .findAny()
            .orElse(null)
    }

    private val lessonsOfGroup: List<EventsOfTheDay>
        get() {
            val weekLessons: MutableList<EventsOfTheDay> = ArrayList()
            currentRow = 3
            currentDayOfWeek = 0
            while (!table.getRow(currentRow).getCell(0).text.contains("ПОНЕДЕЛЬНИК")) currentRow++
            for (i in 0 until STUDY_DAY_COUNT) {
                if (emitter.isDisposed) break
                val dateInCell = table.getRow(currentRow).getCell(0).text
                currentRow++
                val date = dateInCell.substring(dateInCell.lastIndexOf(" ") + 1)
                if (DateFormatUtil.validateDateOfString(date, DateFormatUtil.DD_MM_yy)) {
                    weekLessons.add(getEventsOfTheDay(date))
                    currentDayOfWeek++
                } else {
                    emitter.onError(TimetableInvalidDateException("Некоректная дата: $dateInCell"))
                }
            }
            return weekLessons
        }

    private fun getEventsOfTheDay(dateText: String): EventsOfTheDay {
        val date = DateFormatUtil.convertStringToDateUTC(dateText, DateFormatUtil.DD_MM_yy)
        val eventsOfTheDay = EventsOfTheDay(date)
        if (currentRow == table.rows.size) return eventsOfTheDay
        var cells = table.getRow(currentRow).tableCells
        while (hasRowsOfCurrentDate()) {
            val orderText = cells[0].text
            val subjectAndRoomText = cells[cellOfGroupPos].text
                .replace("\\(.*\\)".toRegex(), "")
                .trim { it <= ' ' }
            currentRow++
            if (currentRow < table.rows.size) {
                cells = table.getRow(currentRow).tableCells
            }
            if (orderText.isNotEmpty()) {
                eventsOfTheDay.add(createEvent(orderText.toInt(), subjectAndRoomText, date))
            } else if (subjectAndRoomText.isNotEmpty()) {
                emitter.onError(
                    TimetableOrderLessonException(
                        """
    У урока нет порядкового номера! 
    Дата: $dateText
    Поле урока: $subjectAndRoomText
    Группа: ${currentGroup!!.group.name}
    """.trimIndent()
                    )
                )
            }
        }
        if (eventsOfTheDay.events.isNotEmpty()) Events.removeRedundantEmptyEvents(eventsOfTheDay.events)
        return eventsOfTheDay
    }

    private fun hasRowsOfCurrentDate(): Boolean {
        if (currentRow == table.rows.size - 1) return false
        return if (currentDayOfWeek == 5) true
        else !table.getRow(currentRow)
            .getCell(0).text.contains(
                days[currentDayOfWeek + 1]
            )
    }

    private fun createEvent(order: Int, subjectAndRoom: String, date: Date): Event {
        var subjectAndRoom = subjectAndRoom
        if (subjectAndRoom.isEmpty()) {
            return empty(group = currentGroup!!.group, order = order, date = date)
        }
        val separatorSubjectRoomPos = subjectAndRoom.indexOf('\\')
        val eventName: String
        val room: String
        subjectAndRoom = subjectAndRoom.replace("\\(.*\\)".toRegex(), "")
        if (separatorSubjectRoomPos != -1) {
            eventName = subjectAndRoom.substring(0, separatorSubjectRoomPos)
            room = subjectAndRoom.substring(separatorSubjectRoomPos + 1)
        } else {
            eventName = subjectAndRoom
            room = ""
        }
        val subjectByName = findSubjectByName(eventName)
        return if (subjectByName.isPresent) {
            val subject = subjectByName.get()
            Event(
                group = currentGroup!!.group,
                order = order,
                date = date,
                room = room,
                details = Lesson(subject, teachers = findTeachersBySubject(subject))
            )
        } else {
            Event(
                group = currentGroup!!.group,
                order = order,
                date = date,
                room = room,
                details = createEventDetails(eventName)
            )
        }
    }

    private fun findSubjectByName(subjectName: String): Optional<Subject> {
        return currentGroup!!.courses
            .stream()
            .filter { course: Course ->
                resetFormatting(course.info.subject.name).contains(
                    resetFormatting(subjectName)
                )
            }
            .findFirst()
            .map { it.info.subject }
    }

    private fun createEventDetails(eventName: String): EventDetails {
        return when (eventName.lowercase(Locale.getDefault())) {
            "обед" -> dinner()
            "практика" -> practice()
            else -> SimpleEventDetails(eventName)
        }
    }

    private fun findTeachersBySubject(subject: Subject): List<User> {
        return currentGroup!!.courses.stream()
            .filter { it.info.subject == subject }
            .map { it.info.teacher }
            .collect(Collectors.toList())
    }

    private fun resetFormatting(s: String): String {
        return s.replace(" ", "")
            .replace("-", "")
            .replace("–", "")
            .lowercase(Locale.getDefault())
    }

    open class TimetableParserException(message: String?) : Exception(message)
    class TimetableInvalidDateException(message: String?) : TimetableParserException(message)
    class TimetableOrderLessonException(message: String?) : TimetableParserException(message)

    companion object {
        const val STUDY_DAY_COUNT = 6
    }

    init {
        getSpecialSubjects.subscribe { collection: List<Subject> ->
            specialSubjects.addAll(collection)
        }
    }
}
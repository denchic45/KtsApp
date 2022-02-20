package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.firestore.CourseContentDoc
import com.denchic45.kts.data.model.room.CourseContentEntity
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import java.io.File
import java.util.stream.Collectors

@Mapper
abstract class CourseContentMapper {
    @Mapping(source = "courseEntity.id", target = "id")
    abstract fun courseContentEntityWithDetailsToTask(
        courseEntity: CourseContentEntity,
        taskDetails: ContentDetails.Task
    ): Task

    @Mapping(source = "courseEntity.id", target = "id")
    @Mapping(source = "contentDetails", target = "contentDetails")
    abstract fun courseContentEntityWithDetailsToTaskDoc(
        courseEntity: CourseContentEntity,
        contentDetails: ContentDetails.Task
    ): CourseContentDoc


    abstract fun taskToTaskDetails(task: Task): ContentDetails.Task

    @Mapping(source = "task.id", target = "id")
    @Mapping(target = "submissions", ignore = true)
    abstract fun taskWithDetailsToTaskDoc(
        task: Task,
        contentDetails: ContentDetails.Task,
        contentType: ContentType
    ): CourseContentDoc

    fun docToEntity(snapshots: QuerySnapshot): Pair<List<CourseContentEntity>, List<CourseContentDoc>> {
        val courseContentEntities: MutableList<CourseContentEntity> = ArrayList()
        val courseContentDocs: MutableList<CourseContentDoc> = ArrayList()
        for (snapshot in snapshots) {
            val courseContentDoc = snapshot.toObject(
                CourseContentDoc::class.java
            )
            courseContentEntities.add(
                courseContentDocToEntity(
                    courseContentDoc,
                    Gson().toJson(snapshot["contentDetails"])
                )
            )
            courseContentDocs.add(courseContentDoc)
        }
        return Pair<List<CourseContentEntity>, List<CourseContentDoc>>(
            courseContentEntities,
            courseContentDocs
        )
    }

    @Mapping(source = "contentDetails", target = "contentDetails")
    abstract fun courseContentDocToEntity(
        courseContentDoc: CourseContentDoc,
        contentDetails: String
    ): CourseContentEntity

    fun domainToDoc(courseContent: CourseContent): CourseContentDoc {
        return if (courseContent is Task) {
            domainToTaskDoc(courseContent)
        } else throw IllegalStateException()
    }

    fun domainToTaskDoc(task: Task): CourseContentDoc {
        return taskWithDetailsToTaskDoc(task,
            taskToTaskDetails(task),
            ContentType.TASK)
    }


    fun entityToTaskDomain(entity: CourseContentEntity): Task {
        return courseContentEntityWithDetailsToTask(
            entity,
            GsonBuilder().create().fromJson(entity.contentDetails, ContentDetails.Task::class.java)
        )
    }

    fun entityToTaskDoc(entity: CourseContentEntity): CourseContentDoc {
        return courseContentEntityWithDetailsToTaskDoc(
            entity,
            GsonBuilder().create().fromJson(entity.contentDetails, ContentDetails.Task::class.java)
        )
    }

    fun mapAttachmentsToFilePaths(attachments: List<Attachment>): List<String> {
        return attachments.stream()
            .map { (file) -> file.path }
            .collect(Collectors.toList())
    }

    fun mapFilePathsToAttachments(filePaths: List<String>): List<Attachment> {
        return if (filePaths == null) emptyList() else filePaths.stream()
            .map { path: String -> Attachment(File(path)) }
            .collect(Collectors.toList())
    }

    @Named("entityToDomain")
    fun entityToDomain(entity: CourseContentEntity): CourseContent {
        return when (entity.contentType) {
            ContentType.TASK -> entityToTaskDomain(entity)
            else -> throw IllegalStateException()
        }
    }

    @Named("entityToDomainAssignment")
    fun entityToDomainAssignment(entity: CourseContentEntity): Task {
        return when (entity.contentType) {
            ContentType.TASK -> entityToTaskDomain(entity)
            else -> throw IllegalStateException()
        }
    }

    abstract fun entityToDomain(courseContentEntities: List<CourseContentEntity>): List<Task>

    abstract fun entityToDomainAssignment(courseContentEntities: List<CourseContentEntity>): List<Task>
}
package com.denchic45.kts.data.model.mapper;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.Attachment;
import com.denchic45.kts.data.model.domain.ContentDetails;
import com.denchic45.kts.data.model.domain.ContentType;
import com.denchic45.kts.data.model.domain.CourseContent;
import com.denchic45.kts.data.model.domain.Task;
import com.denchic45.kts.data.model.firestore.CourseContentDoc;
import com.denchic45.kts.data.model.room.CourseContentEntity;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Pair;

@Mapper
public interface CourseContentMapper {

    @Mapping(source = "courseEntity.id", target = "id")
    Task courseContentEntityWithDetailsToTask(CourseContentEntity courseEntity, ContentDetails.Task taskDetails);

    @Mapping(source = "task.id", target = "id")
    @Mapping(target = "submissions", ignore = true)
    CourseContentDoc taskWithDetailsToTaskDoc(Task task, ContentDetails.Task contentDetails, ContentType contentType);

    default Pair<List<CourseContentEntity>, List<CourseContentDoc>> docToEntity(@NonNull QuerySnapshot snapshots) {
        List<CourseContentEntity> courseContentEntities = new ArrayList<>();
        List<CourseContentDoc> courseContentDocs = new ArrayList<>();
        for (QueryDocumentSnapshot snapshot : snapshots) {
            CourseContentDoc courseContentDoc = snapshot.toObject(CourseContentDoc.class);
            courseContentEntities.add(
                    courseContentDocToEntity(
                            courseContentDoc,
                            new Gson().toJson(snapshot.get("details"))
                    )
            );
            courseContentDocs.add(courseContentDoc);
        }
        return new Pair<>(courseContentEntities, courseContentDocs);
    }

    @Mapping(source = "details", target = "details")
    CourseContentEntity courseContentDocToEntity(CourseContentDoc courseContentDoc, String details);

    default CourseContentDoc domainToDoc(CourseContent courseContent) {
        if (courseContent instanceof Task) {
            return domainToTaskDoc((Task) courseContent);
        } else
            throw new IllegalStateException();
    }

    default CourseContentDoc domainToTaskDoc(Task task) {
        return taskWithDetailsToTaskDoc(task, taskToTaskDetails(task), ContentType.TASK);
    }

    ContentDetails.Task taskToTaskDetails(Task task);

    default Task entityToTaskDomain(CourseContentEntity entity) {
        return courseContentEntityWithDetailsToTask(entity,
                new GsonBuilder().create().fromJson(entity.getDetails(), ContentDetails.Task.class));
    }

    default List<String> mapAttachmentsToFilePaths(@NonNull List<Attachment> attachments) {
        return attachments.stream()
                .map(attachment -> attachment.getFile().getPath())
                .collect(Collectors.toList());
    }

    default List<Attachment> mapFilePathsToAttachments(List<String> filePaths) {
        if (filePaths == null)
            return Collections.emptyList();
        return filePaths.stream()
                .map(path -> new Attachment(new File(path)))
                .collect(Collectors.toList());
    }

    @Named("entityToDomain")
    default CourseContent entityToDomain(@NonNull CourseContentEntity entity) {
        switch (entity.getContentType()) {
            case TASK:
                return entityToTaskDomain(entity);
            default:
                throw new IllegalStateException();
        }
    }

    List<CourseContent> entityToDomain(List<CourseContentEntity> courseContentEntities);
}
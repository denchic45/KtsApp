package com.denchic45.kts.data.model.mapper;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.Attachment;
import com.denchic45.kts.data.model.domain.ContentDetails;
import com.denchic45.kts.data.model.domain.ContentType;
import com.denchic45.kts.data.model.domain.CourseContent;
import com.denchic45.kts.data.model.domain.MarkType;
import com.denchic45.kts.data.model.domain.Task;
import com.denchic45.kts.data.model.firestore.CourseContentDoc;
import com.denchic45.kts.data.model.room.CourseContentEntity;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface CourseContentMapper {

    @Mapping(source = "courseEntity.id", target = "id")
    Task courseContentEntityWithDetailsToTask(CourseContentEntity courseEntity, ContentDetails.Task taskDetails);

    @Mapping(source = "task.id", target = "id")
    @Mapping(target = "completions", ignore = true)
    CourseContentDoc taskWithDetailsToTaskDoc(Task task, ContentDetails.Task contentDetails, ContentType contentType);

    default List<CourseContentEntity> docToEntity(@NonNull QuerySnapshot snapshots) {
        List<CourseContentEntity> courseContents = new ArrayList<>();
        for (QueryDocumentSnapshot snapshot : snapshots) {
            courseContents.add(
                    courseContentDocToEntity(
                            snapshot.toObject(CourseContentDoc.class),
                            new Gson().toJson(snapshot.get("details"))
                    )
            );
        }
        return courseContents;
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
                new GsonBuilder().registerTypeAdapter(MarkType.class,
                        new CourseContentMapper.MarkTypeDeserializer())
                        .create().fromJson(entity.getDetails(), ContentDetails.Task.class));
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

    class MarkTypeDeserializer implements JsonDeserializer<MarkType> {

        @Override
        public MarkType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            switch (json.getAsJsonObject().get("position").getAsInt()) {
                case 0:
                    return context.deserialize(json, MarkType.Score.class);
                case 1:
                    return context.deserialize(json, MarkType.Binary.class);
                default:
                    throw new IllegalStateException();
            }
        }
    }
}






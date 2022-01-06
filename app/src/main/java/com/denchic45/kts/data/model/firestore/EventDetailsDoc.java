package com.denchic45.kts.data.model.firestore;

import com.denchic45.kts.data.model.DocModel;
import com.denchic45.kts.data.model.room.TaskEntity;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsDoc implements DocModel {

    private String subjectId;
    private TaskEntity taskEntity;
    private List<String> teacherUuidList;
    private String name;
    private String iconUrl;
    private String color;
    private String type;

    public EventDetailsDoc(String subjectId, TaskEntity taskEntity, List<String> teacherUuidList, String name, String iconUrl, String color, String type) {
        this.subjectId = subjectId;
        this.taskEntity = taskEntity;
        this.teacherUuidList = teacherUuidList;
        this.name = name;
        this.iconUrl = iconUrl;
        this.color = color;
        this.type = type;
    }

    public EventDetailsDoc() {
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public TaskEntity getTaskEntity() {
        return taskEntity;
    }

    public void setTaskEntity(TaskEntity taskEntity) {
        this.taskEntity = taskEntity;
    }

    public List<String> getTeacherUuidList() {
        return teacherUuidList;
    }

    public void setTeacherUuidList(List<String> teacherUuidList) {
        if (teacherUuidList == null)
            teacherUuidList = new ArrayList<>();
        this.teacherUuidList = teacherUuidList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.denchic45.kts.data.model.firestore;

import com.denchic45.kts.data.model.DocModel;
import com.denchic45.kts.data.model.domain.User;
import com.google.firebase.firestore.ServerTimestamp;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class EventDoc implements DocModel {

    private String uuid;
//    @ServerTimestamp
//    private Date timestamp;
    private int order;
    private Date date;
    private String room;
    //    private String subjectUuid;
//    private List<User> teachers;
    private String groupUuid;
    private EventDetailsDoc eventDetailsDoc;



    public EventDoc(String uuid, Date timestamp, int order, Date date, String room, String groupUuid, EventDetailsDoc eventDetailsDoc) {
        this.uuid = uuid;
//        this.timestamp = timestamp;
        this.order = order;
        this.date = date;
        this.room = room;
//        this.subjectUuid = subjectUuid;
//        this.teachers = teachers;
        this.groupUuid = groupUuid;
        this.eventDetailsDoc = eventDetailsDoc;
//        this.type = type;
    }

    public EventDoc() {
    }
//    private String type;

    public EventDetailsDoc getEventDetailsDoc() {
        return eventDetailsDoc;
    }

    public void setEventDetailsDoc(EventDetailsDoc eventDetailsDoc) {
        this.eventDetailsDoc = eventDetailsDoc;
    }

//    public Date getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(Date timestamp) {
//        this.timestamp = timestamp;
//    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

//    public String getSubjectUuid() {
//        return subjectUuid;
//    }

//    public void setSubjectUuid(String subjectUuid) {
//        this.subjectUuid = subjectUuid;
//    }

//    public List<User> getTeachers() {
//        if (teachers == null)
//            teachers = new ArrayList<>();
//        return teachers;
//    }

//    public void setTeachers(List<User> teachers) {
//        this.teachers = teachers;
//    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
}

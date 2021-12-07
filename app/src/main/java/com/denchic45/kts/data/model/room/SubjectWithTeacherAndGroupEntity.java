package com.denchic45.kts.data.model.room;

import androidx.room.Embedded;

import com.denchic45.kts.data.model.EntityModel;

public class SubjectWithTeacherAndGroupEntity implements EntityModel {

    @Embedded
    private SubjectEntity subjectEntity;
    @Embedded
    private UserEntity teacherEntity;
    @Embedded
    private GroupWithCuratorAndSpecialtyEntity groupWithCuratorAndSpecialtyEntity;

    public SubjectEntity getSubjectEntity() {
        return subjectEntity;
    }

    public void setSubjectEntity(SubjectEntity subjectEntity) {
        this.subjectEntity = subjectEntity;
    }

    public UserEntity getTeacherEntity() {
        return teacherEntity;
    }

    public void setTeacherEntity(UserEntity teacherEntity) {
        this.teacherEntity = teacherEntity;
    }

    public GroupWithCuratorAndSpecialtyEntity getGroupWithCuratorAndSpecialtyEntity() {
        return groupWithCuratorAndSpecialtyEntity;
    }

    public void setGroupWithCuratorAndSpecialtyEntity(GroupWithCuratorAndSpecialtyEntity groupWithCuratorAndSpecialtyEntity) {
        this.groupWithCuratorAndSpecialtyEntity = groupWithCuratorAndSpecialtyEntity;
    }
}

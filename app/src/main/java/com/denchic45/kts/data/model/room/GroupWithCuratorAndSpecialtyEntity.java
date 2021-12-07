package com.denchic45.kts.data.model.room;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.denchic45.kts.data.model.EntityModel;

public class GroupWithCuratorAndSpecialtyEntity implements EntityModel {

    @Embedded
    private GroupEntity groupEntity;

    @Relation(
            parentColumn = "uuid_curator",
            entityColumn = "uuid_user"
    )
    private UserEntity curatorEntity;

    @Relation(
            parentColumn = "specialtyUuid",
            entityColumn = "specialtyUuid"
    )
    private SpecialtyEntity specialtyEntity;

    public GroupWithCuratorAndSpecialtyEntity() {

    }

    @Ignore
    public GroupWithCuratorAndSpecialtyEntity(GroupEntity groupEntity, UserEntity curatorEntity, SpecialtyEntity specialtyEntity) {
        this.groupEntity = groupEntity;
        this.curatorEntity = curatorEntity;
        this.specialtyEntity = specialtyEntity;
    }

    public SpecialtyEntity getSpecialtyEntity() {
        return specialtyEntity;
    }

    public void setSpecialtyEntity(SpecialtyEntity specialtyEntity) {
        this.specialtyEntity = specialtyEntity;
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public UserEntity getCuratorEntity() {
        return curatorEntity;
    }

    public void setCuratorEntity(UserEntity curatorEntity) {
        this.curatorEntity = curatorEntity;
    }
}

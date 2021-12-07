package com.denchic45.kts.data.model.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.denchic45.kts.data.model.EntityModel;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Entity(tableName = "specialty")
public class SpecialtyEntity implements EntityModel {

    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "specialtyUuid")
    protected String uuid;
    private String name;


    public SpecialtyEntity() {
    }

    @Ignore
    public SpecialtyEntity(@NotNull String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @NotNull String getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecialtyEntity)) return false;
        SpecialtyEntity specialtyEntity = (SpecialtyEntity) o;
        return getUuid().equals(specialtyEntity.getUuid()) &&
                Objects.equals(getName(), specialtyEntity.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getName());
    }
}

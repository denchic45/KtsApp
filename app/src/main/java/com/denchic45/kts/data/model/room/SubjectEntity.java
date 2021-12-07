package com.denchic45.kts.data.model.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.denchic45.kts.data.model.EntityModel;

@Entity(tableName = "subject")
public class SubjectEntity implements EntityModel {

    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "uuid_subject")
    protected String uuid;
    private String name;
    private String iconUrl;
    private String colorName;

    @Ignore
    public SubjectEntity(@NonNull String uuid, String name, String iconUrl, String colorName) {
        this.uuid = uuid;
        this.name = name;
        this.iconUrl = iconUrl;
        this.colorName = colorName;
    }

    public SubjectEntity() {
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

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}

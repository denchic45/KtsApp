package com.denchic45.kts.data.model.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.denchic45.kts.data.model.EntityModel;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "user")
public class UserEntity implements EntityModel {

    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "uuid_user")
    protected String uuid;
    private String firstName;
    private String surname;
    @ColumnInfo(name = "uuid_user_group")
    private String groupUuid;
    private String role;
    private String phoneNum;
    private String email;
    private String photoUrl;
    private int gender;
    private boolean admin;
    private String patronymic;
    private boolean generatedAvatar;
    @ColumnInfo(name = "timestamp_user")
    @TypeConverters({TimestampConverter.class})
    private Date timestamp;

    @Ignore
    public UserEntity(@NotNull String uuid, String firstName, String surname, String groupUuid, String role, String phoneNum, String photoUrl, int gender, Date timestamp, boolean admin, boolean generatedAvatar) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.surname = surname;
        this.groupUuid = groupUuid;
        this.role = role;
        this.photoUrl = photoUrl;
        this.phoneNum = phoneNum;
        this.gender = gender;
        this.timestamp = timestamp;
        this.admin = admin;
        this.generatedAvatar = generatedAvatar;
    }

    @Ignore
    public String getFullName() {
        return firstName + surname;
    }

    @Ignore
    public UserEntity(String firstName, String surname, String groupUuid, String role, String phoneNum, String photoUrl, int gender, Date timestamp, boolean generatedAvatar) {
        uuid = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.surname = surname;
        this.groupUuid = groupUuid;
        this.role = role;
        this.photoUrl = photoUrl;
        this.phoneNum = phoneNum;
        this.gender = gender;
        this.timestamp = timestamp;
        this.generatedAvatar = generatedAvatar;
    }

    @Ignore
    public UserEntity(String firstName) {
        this.firstName = firstName;
        uuid = "";
        timestamp = new Date(0);
    }

    @Ignore
    public UserEntity(String firstName, String surname, String groupUuid, String role, String phoneNum, String photoUrl, int gender, boolean admin) {
        this.firstName = firstName;
        this.surname = surname;
        this.groupUuid = groupUuid;
        this.role = role;
        this.phoneNum = phoneNum;
        this.photoUrl = photoUrl;
        this.gender = gender;
        this.admin = admin;
        uuid = UUID.randomUUID().toString();
    }


    public UserEntity() {
        uuid = UUID.randomUUID().toString();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String StringUuid) {
        this.groupUuid = StringUuid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isGeneratedAvatar() {
        return generatedAvatar;
    }

    public void setGeneratedAvatar(boolean generatedAvatar) {
        this.generatedAvatar = generatedAvatar;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull String uuid) {
        this.uuid = uuid;
    }
}

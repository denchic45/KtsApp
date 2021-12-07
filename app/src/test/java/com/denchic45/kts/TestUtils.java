package com.denchic45.kts;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.Subject;
import com.denchic45.kts.data.model.domain.User;
import com.denchic45.kts.data.model.room.GroupEntity;
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity;
import com.denchic45.kts.data.model.room.SpecialtyEntity;
import com.denchic45.kts.data.model.room.UserEntity;

import org.jetbrains.annotations.Contract;

import java.util.Date;

public class TestUtils {

    @NonNull
    @Contract(" -> new")
    public static GroupEntity createGroupEntity() {
        return new GroupEntity("uuidOfGroup", "PKS", 2, "uuidOfSpecialty", new Date());
    }

    public static UserEntity createCuratorEntity() {
        return new UserEntity("", "Мария", "Ивановна", "", User.TEACHER, "+79510832144", "", 0, new Date(), false, true);
    }

    public static User createCurator() {
        return new User("", "Мария", "Ивановна", "", "",User.TEACHER, "+79510832144", "", "", new Date(), 0 , true, false);
    }

    public static User createStudent1() {
        return new User("", "Иван", "Иванов", "", "",User.STUDENT, "+79510832144", "", "", new Date(), 0 , true, false);
    }

    public static User createStudent2() {
        return new User("", "Петр", "Петрович", "", "",User.STUDENT, "+79510832144", "", "", new Date(), 0 , true, false);
    }

    public static  Subject createSubject() {
        return new Subject("","Math", "ic_math","red");
    }

    @NonNull
    @Contract(" -> new")
    public static GroupWithCuratorAndSpecialtyEntity createGroupWithCuratorAndSpecialtyEntity() {
        return new GroupWithCuratorAndSpecialtyEntity(createGroupEntity(), createCuratorEntity(), createSpecialityEntity());
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    public static SpecialtyEntity createSpecialityEntity() {
        return new SpecialtyEntity("", "specialty");
    }
}

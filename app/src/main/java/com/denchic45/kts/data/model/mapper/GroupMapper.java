package com.denchic45.kts.data.model.mapper;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.Group;
import com.denchic45.kts.data.model.firestore.GroupDoc;
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity;
import com.denchic45.kts.data.model.room.GroupEntity;
import com.denchic45.kts.utils.SearchKeysGenerator;
import com.google.firebase.firestore.FieldValue;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(uses = {UserMapper.class, SpecialtyMapper.class})
public interface GroupMapper extends

        DomainDocMapper<Group, GroupDoc>,
        DocEntityMapper<GroupDoc, GroupEntity> {

    @Mapping(target = "searchKeys", ignore = true)
    @Mapping(target = "allUsers", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Override
    GroupDoc domainToDoc(Group domain);

    @Mapping(source = "specialty", target = "specialtyEntity")
    @Mapping(source = ".", target = "groupEntity")
    @Mapping(source = "curator", target = "curatorEntity")
    GroupWithCuratorAndSpecialtyEntity domainToEntity(Group domain);

    @DoIgnore
    @InheritInverseConfiguration(name = "domainToEntity")
    Group groupWithCuratorAndSpecialtyEntityToGroup(GroupWithCuratorAndSpecialtyEntity entity);

    default Group entityToDomain(GroupWithCuratorAndSpecialtyEntity entity) {
        if (entity == null)
            return Group.deleted();
        else
            return groupWithCuratorAndSpecialtyEntityToGroup(entity);
    }

//    @Mapping(target = "specialtyUuid", source = "specialty.uuid")
//    @Mapping(target = "curatorUuid", source = "curator.uuid")
//    @Override
//    GroupEntity domainToEntity(Group group);
//
//    @Mapping(target = "specialty", ignore = true)
//    @Mapping(target = "curator", ignore = true)
//    @Override
//    Group entityToDomain(GroupEntity entity);

    default Map<String, Object> domainToMap(@NotNull Group group) {
        Map<String, Object> map = new HashMap<>();
        map.put("uuid", group.getUuid());
        map.put("name", group.getName());
        map.put("course", group.getCourse());
        map.put("specialtyUuid", group.getSpecialty());
        map.put("timestamp", FieldValue.serverTimestamp());
        map.put("specialty", group.getSpecialty());
        map.put("curator", group.getCurator());
        return map;
    }

//    @AfterMapping
//    default void setEmptiesValueInDoc(@MappingTarget @NotNull GroupDoc groupDoc) {
//        groupDoc.setStudents(Collections.emptyMap());
//        groupDoc.setTeachers(Collections.emptyMap());
//        groupDoc.setSubjects(Collections.emptyMap());
//    }

    @Override
    Group docToDomain(GroupDoc doc);

    @Mapping(source = "curator.uuid", target = "curatorUuid")
    @Mapping(source = "specialty.uuid", target = "specialtyUuid")
    @Override
    GroupEntity docToEntity(GroupDoc doc);

    @AfterMapping
    default void addSearchKeys(@MappingTarget @NotNull GroupDoc groupDoc) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        groupDoc.setSearchKeys(generator.generateKeys(groupDoc.getName(), predicate -> predicate.length() > 1));
    }


    @Mapping(target = "searchKeys", ignore = true)
//    @Mapping(target = "courses", ignore = true)
    @Mapping(target = "allUsers", ignore = true)
    @Override
    GroupDoc entityToDoc(GroupEntity entity);
}

package com.denchic45.kts.data.model.mapper;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.Course;
import com.denchic45.kts.data.model.domain.CourseInfo;
import com.denchic45.kts.data.model.domain.Group;
import com.denchic45.kts.data.model.firestore.CourseDoc;
import com.denchic45.kts.data.model.room.CourseEntity;
import com.denchic45.kts.data.model.room.CourseWithSubjectAndTeacher;
import com.denchic45.kts.data.model.room.CourseWithSubjectWithTeacherAndGroups;
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity;
import com.denchic45.kts.utils.SearchKeysGenerator;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = {GroupMapper.class, UserMapper.class, SubjectMapper.class})
public interface CourseMapper {

    @Mapping(source = "info.teacher", target = "teacher")
    @Mapping(source = "info.subject", target = "subject")
    @Mapping(source = "info.name", target = "name")
    @Mapping(qualifiedByName = "addGroupUuid", source = "groups", target = "groupUuids")
    CourseDoc domainToDoc(Course course);

    CourseInfo docToDomain(CourseDoc doc);

    List<CourseInfo> docToDomain(List<CourseDoc> docs);

//    @Mapping(source = "group", target = "groupWithCuratorAndSpecialtyEntity")

    //    @Mapping(source = "info.subject", target = "subjectEntity")
//    @Mapping(source = "info.teacher", target = "teacherEntity")
//    @Mapping(source = "info", target = "courseEntity")

//    @InheritInverseConfiguration(name = "entityToDomain")
//    CourseWithSubjectWithTeacherAndGroups domainToEntity(Course domain);

    @Mapping(source = "groupEntities", target = "groups")

//    @Mapping(source = "courseEntity.name", target = "info.name")
//    @Mapping(source = "courseEntity.uuid", target = "info.uuid")
//    @Mapping(source = "subjectEntity", target = "info.subject")
//    @Mapping(source = "teacherEntity", target = "info.teacher")
    @Mapping(source = ".", target = "info", qualifiedByName = "entityToDomainInfo")
    Course entityToDomain(CourseWithSubjectWithTeacherAndGroups entity);

    @Mapping(source = "subject.uuid", target = "subjectId")
    @Mapping(source = "teacher.uuid", target = "teacherUuid")
    @Mapping(source = "uuid", target = "id")
    
    CourseEntity docToEntity(CourseDoc doc);

    List<CourseEntity> docToEntity(List<CourseDoc> doc);

    @InheritInverseConfiguration(name = "docToEntity")
    
    CourseDoc entityToDoc(CourseEntity entity);



    List<Course> entityToDomain(List<CourseWithSubjectWithTeacherAndGroups> entities);

    @Mapping(source = "courseEntity", target = ".")
    @Named("entityToDomainInfo")
            @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "teacherEntity", target = "teacher")
    @Mapping(source = "courseEntity.id", target = "uuid")
    CourseInfo entityToDomainInfo(CourseWithSubjectWithTeacherAndGroups entity);

    @Mapping(source = "courseEntity", target = ".")
    @Named("entityToDomainInfo")
    @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "teacherEntity", target = "teacher")
    CourseInfo entityToDomainInfo(CourseWithSubjectAndTeacher entity);

    @InheritInverseConfiguration(name = "entityToDomainInfo")
//    @Named("domainInfoToEntity")
    CourseEntity domainInfoToEntity(CourseInfo domain);

   default List<CourseInfo> entityToDomainInfo(@NonNull List<CourseWithSubjectWithTeacherAndGroups> entities) {
        return entities.stream().map(this::entityToDomainInfo).collect(Collectors.toList());
    }

    default List<CourseInfo> entityToDomainInfo2(@NonNull List<CourseWithSubjectAndTeacher> entities) {
        return entities.stream().map(this::entityToDomainInfo).collect(Collectors.toList());
    }

    default List<String> mapCoursesGroup(@NonNull List<GroupWithCuratorAndSpecialtyEntity> entities) {
        return entities.stream()
                .map(groupWithCuratorAndSpecialtyEntity -> groupWithCuratorAndSpecialtyEntity.getGroupEntity().getId())
                .collect(Collectors.toList());
    }

    @Named("addGroupUuid")
    default List<String> addGroupUuid(@NonNull List<Group> groups) {
        return groups.stream().map(Group::getUuid).collect(Collectors.toList());
    }

    @AfterMapping
    default void addSearchKeys(@MappingTarget @NotNull CourseDoc courseDoc) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        courseDoc.setSearchKeys(generator.generateKeys(courseDoc.getName(), predicate -> predicate.length() > 1));
    }
}
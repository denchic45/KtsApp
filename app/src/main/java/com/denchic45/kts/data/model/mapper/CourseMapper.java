package com.denchic45.kts.data.model.mapper;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.Course;
import com.denchic45.kts.data.model.domain.CourseGroup;
import com.denchic45.kts.data.model.domain.CourseHeader;
import com.denchic45.kts.data.model.firestore.CourseDoc;
import com.denchic45.kts.data.model.room.CourseEntity;
import com.denchic45.kts.data.model.room.CourseWithSubjectAndTeacherEntities;
import com.denchic45.kts.data.model.room.CourseWithSubjectWithTeacherAndGroupsEntities;
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity;
import com.denchic45.kts.utils.SearchKeysGenerator;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = {GroupMapper.class, UserMapper.class, SubjectMapper.class, SpecialtyMapper.class})
public interface CourseMapper {

//    @Mapping(source = "info.teacher", target = "teacher")
//    @Mapping(source = "info.subject", target = "subject")
//    @Mapping(source = "info.name", target = "name")
    @Mapping(qualifiedByName = "addGroupId", source = "groups", target = "groupIds")
    CourseDoc domainToDoc(Course course);

    CourseHeader docToDomain(CourseDoc doc);

    List<CourseHeader> docToDomain(List<CourseDoc> docs);

    @Mapping(source = "groupEntities", target = "groups")

    @Mapping(source = "courseEntity.name", target = "name")
    @Mapping(source = "courseEntity.id", target = "id")
    @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "teacherEntity", target = "teacher")
    @Named("entityToDomain2")
    Course entityToDomain2(CourseWithSubjectWithTeacherAndGroupsEntities entity);

    @IterableMapping(qualifiedByName = "entityToDomain2")
    List<Course> entityToDomain2(List<CourseWithSubjectWithTeacherAndGroupsEntities> entity);

    @Mapping(source = "courseEntity.name", target = "name")
    @Mapping(source = "courseEntity.id", target = "id")
    @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "teacherEntity", target = "teacher")
    @Named("entityToDomainHeaders")
    CourseHeader entityToDomainHeaders(CourseWithSubjectAndTeacherEntities entity);

    @IterableMapping(qualifiedByName = "entityToDomainHeaders")
    List<CourseHeader> entityToDomainHeaders(List<CourseWithSubjectAndTeacherEntities> entity);

//    @Mapping(source = "courseEntity.name", target = "name")
//    @Mapping(source = "courseEntity.id", target = "id")
//    @Mapping(source = "subjectEntity", target = "subject")
//    @Mapping(source = "teacherEntity", target = "teacher")
//    Course entityToDomain(CourseWithSubjectAndTeacherEntities entity);

    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "teacher.id", target = "teacherId")
    CourseEntity docToEntity(CourseDoc doc);

    List<CourseEntity> docToEntity(List<CourseDoc> doc);

    @InheritInverseConfiguration(name = "docToEntity")
    CourseDoc entityToDoc(CourseEntity entity);

//    List<Course> entityToDomain(List<CourseWithSubjectWithTeacherAndGroupsEntities> entities);

    List<Course> entityToDomain(List<CourseWithSubjectAndTeacherEntities> entities);

//    @InheritConfiguration(name = "entityToDomain")
//    Course entityToDomainInfo(CourseWithSubjectWithTeacherAndGroupsEntities entity);

//    @Mapping(source = "courseEntity", target = ".")
//
//    @Mapping(source = "subjectEntity", target = "subject")
//    @Mapping(source = "teacherEntity", target = "teacher")
//    Course entityToDomainInfo(CourseWithSubjectAndTeacherEntities entity);

//    default List<Course> entityToDomainInfo(@NonNull List<CourseWithSubjectWithTeacherAndGroups> entities) {
//        return entities.stream().map(this::entityToDomainInfo).collect(Collectors.toList());
//    }
//
//    default List<Course> entityToDomainInfo2(@NonNull List<CourseWithSubjectAndTeacherEntities> entities) {
//        return entities.stream().map(this::entityToDomain).collect(Collectors.toList());
//    }

    @Mapping(source = "teacherEntity", target = "teacher")
    @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "courseEntity", target = ".")
    CourseDoc entityToDoc(CourseWithSubjectAndTeacherEntities entities);

    @Named("addGroupId")
    default List<String> addGroupId(@NonNull List<CourseGroup> groups) {
        return groups.stream().map(CourseGroup::getId).collect(Collectors.toList());
    }

    @AfterMapping
    default void addSearchKeys(@MappingTarget @NotNull CourseDoc courseDoc) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        courseDoc.setSearchKeys(generator.generateKeys(courseDoc.getName(), predicate -> predicate.length() > 1));
    }
}
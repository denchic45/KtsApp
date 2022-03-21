package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.CourseHeader
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.room.CourseEntity
import com.denchic45.kts.data.model.room.CourseWithSubjectAndTeacherEntities
import com.denchic45.kts.data.model.room.CourseWithSubjectWithTeacherAndGroupsEntities
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import org.mapstruct.*

@Mapper(uses = [GroupMapper::class, UserMapper::class, SubjectMapper::class, SpecialtyMapper::class])
abstract class CourseMapper {

    @Mapping(qualifiedByName = ["courseGroupToGroupId"], source = "groups", target = "groupIds")
    abstract fun domainToDoc(course: Course): CourseDoc

    abstract fun docToDomain(docs: List<CourseDoc>): List<CourseHeader>

    abstract fun docToDomain2(docs: List<CourseDoc>): List<CourseHeader>

    @Named("groupWithCuratorAndSpecialtyEntityToCourseGroup")
    @Mapping(source = "entities.groupEntity", target = ".")
    abstract fun groupWithCuratorAndSpecialtyEntityToCourseGroup(entities: GroupWithCuratorAndSpecialtyEntity): CourseGroup

    @IterableMapping(qualifiedByName = ["groupWithCuratorAndSpecialtyEntityToCourseGroup"])
    abstract fun groupWithCuratorAndSpecialtyEntityToCourseGroup(entities: List<GroupWithCuratorAndSpecialtyEntity>): List<CourseGroup>

    @Mapping(source = "groupEntities", target = "groups")
    @Mapping(source = "courseEntity.name", target = "name")
    @Mapping(source = "courseEntity.id", target = "id")
    @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "teacherEntity", target = "teacher")
    @Named("entityToDomain2")
    abstract fun entityToDomain2(entity: CourseWithSubjectWithTeacherAndGroupsEntities): Course

    @IterableMapping(qualifiedByName = ["entityToDomain2"])
    abstract fun entityToDomain2(entity: List<CourseWithSubjectWithTeacherAndGroupsEntities>): List<Course>

    @Mapping(source = "courseEntity.name", target = "name")
    @Mapping(source = "courseEntity.id", target = "id")
    @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "teacherEntity", target = "teacher")
    @Named("entityToDomainHeaders")
    abstract fun entityToDomainHeaders(entity: CourseWithSubjectAndTeacherEntities): CourseHeader

    @IterableMapping(qualifiedByName = ["entityToDomainHeaders"])
    abstract fun entityToDomainHeaders(entity: List<CourseWithSubjectAndTeacherEntities>): List<CourseHeader>

    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "teacher.id", target = "teacherId")
    abstract fun docToEntity(doc: CourseDoc): CourseEntity

    abstract fun docToEntity(doc: List<CourseDoc>): List<CourseEntity>

    abstract fun entityToDomain(entities: List<CourseWithSubjectAndTeacherEntities>): List<Course>

    @Mapping(source = "teacherEntity", target = "teacher")
    @Mapping(source = "subjectEntity", target = "subject")
    @Mapping(source = "courseEntity", target = ".")
    abstract fun entityToDoc(entities: CourseWithSubjectAndTeacherEntities): CourseDoc

    @Named("courseGroupToGroupId")
    fun courseGroupToGroupId(group: CourseGroup): String {
        return group.id
    }


}
package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.domain.GroupWithCourses;
import com.denchic45.kts.data.model.firestore.GroupDoc;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {SpecialtyMapper.class,GroupMapper.class, CourseMapper.class})
public interface GroupInfoMapper extends DomainDocMapper<GroupWithCourses, GroupDoc> {
//
//    @Mapping(target = "searchKeys", ignore = true)
//    @Mapping(target = "allUsers", ignore = true)
//    @Mapping(target = "students", ignore = true)
////    @Mapping(target = "timestamp", source = "group.timestamp")
//    @Mapping(target = "course", source = "group.course")
//    @Mapping(target = "name", source = "group.name")
//    @Mapping(target = "specialty", source = "group.specialty")
//    @Mapping(target = "curator", source = "group.curator")
//    @Override
//    GroupDoc domainToDoc(GroupWithCourses domain);
//
//    @Mapping(target = "searchKeys", ignore = true)
//    @Override
//    List<GroupDoc> domainToDoc(List<GroupWithCourses> domain);
//
//    @Mapping(source = "name", target = "group.name")
//    @Mapping(source = "course", target = "group.course")
//    @Mapping(source = "uuid", target = "group.id")
//    @Mapping(source = "timestamp", target = "group.timestamp")
//    @Mapping(source = "curator", target = "group.curator")
//    @Mapping(source = "specialty", target = "group.specialty")
////    @Mapping(target = "subject", ignore = true)
////    @Mapping(target = "group", ignore = true)
////    @Mapping(target = "teacher", ignore = true)
//    @Override
//    GroupWithCourses docToDomain(GroupDoc doc);


    //    default Map<String, Object> domainToMap(@NotNull GroupInfo groupInfo) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("name", groupInfo.getName());
//        map.put("specialtyUuid", groupInfo.getSpecialtyEntity().getUuid());
//        map.put("course", groupInfo.getCourse());
//        List<SubjectEntity> subjectEntities = new ArrayList<>();
//        List<User> teachers = new ArrayList<>();
//        groupInfo.getSubjectTeacherList().forEach(subjectTeacher -> {
//            subjectEntities.add(subjectTeacher.getSubjectEntity());
//            teachers.add(subjectTeacher.getTeacher());
//        });
//        map.put("subjects", subjectEntities);
//        map.put("teachers", teachers);
//        map.put("specialty", groupInfo.getSpecialtyEntity());
//        map.put("timestamp", new Date());
//        return map;
//    }
}

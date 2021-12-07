package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.domain.Subject;
import com.denchic45.kts.data.model.firestore.SubjectDoc;
import com.denchic45.kts.data.model.room.SubjectEntity;
import com.denchic45.kts.utils.SearchKeysGenerator;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface SubjectMapper extends DomainEntityMapper<Subject, SubjectEntity>,
        DomainDocMapper<Subject, SubjectDoc>,
        DocEntityMapper<SubjectDoc, SubjectEntity>{

    @Mapping(target = "searchKeys", ignore = true)
    @Override
    SubjectDoc domainToDoc(Subject domain);

    @Mapping(target = "searchKeys", ignore = true)
    @Override
    List<SubjectDoc> domainToDoc(List<Subject> domain);

    @AfterMapping
    default void addSearchKeys(@MappingTarget @NotNull SubjectDoc subjectDoc) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        subjectDoc.setSearchKeys(generator.generateKeys(subjectDoc.getName(), predicate -> !predicate.isEmpty()));
    }
}

package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.domain.Specialty;
import com.denchic45.kts.data.model.firestore.SpecialtyDoc;
import com.denchic45.kts.data.model.room.SpecialtyEntity;
import com.denchic45.kts.utils.SearchKeysGenerator;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface SpecialtyMapper extends DomainDocMapper<Specialty, SpecialtyDoc>,
        DomainEntityMapper<Specialty, SpecialtyEntity>,
        DocEntityMapper<SpecialtyDoc, SpecialtyEntity> {
    @AfterMapping
    default void addSearchKeys(@MappingTarget @NotNull SpecialtyDoc specialtyDoc) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        specialtyDoc.setSearchKeys(generator.generateKeys(specialtyDoc.getName(), predicate -> predicate.length() > 2));
    }

    @Mapping(target = "searchKeys", ignore = true)
    @Override
    SpecialtyDoc entityToDoc(SpecialtyEntity entity);

    @Mapping(target = "searchKeys", ignore = true)
    @Override
    List<SpecialtyDoc> entityToDoc(List<SpecialtyEntity> entity);
}

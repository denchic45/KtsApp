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
public interface SpecialtyMapper {
    @AfterMapping
    default void addSearchKeys(@MappingTarget @NotNull SpecialtyDoc specialtyDoc) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        specialtyDoc.setSearchKeys(generator.generateKeys(specialtyDoc.getName(), predicate -> predicate.length() > 2));
    }

    @Mapping(target = "searchKeys", ignore = true)
    SpecialtyDoc entityToDoc(SpecialtyEntity entity);

    @Mapping(target = "searchKeys", ignore = true)
    List<SpecialtyDoc> entityToDoc(List<SpecialtyEntity> entity);
    
    SpecialtyEntity docToEntity(SpecialtyDoc doc);

    List<SpecialtyEntity> docToEntity(List<SpecialtyDoc> doc);

    SpecialtyDoc domainToDoc(Specialty domain);

    Specialty docToDomain(SpecialtyDoc doc);

    List<Specialty> docToDomain(List<SpecialtyDoc> doc);

    List<SpecialtyDoc> domainToDoc(List<Specialty> domain);

    SpecialtyEntity domainToEntity(Specialty domain);

    Specialty entityToDomain(SpecialtyEntity entity);

    List<SpecialtyEntity> domainToEntity(List<Specialty> domain);
}

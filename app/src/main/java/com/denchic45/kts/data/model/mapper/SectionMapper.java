package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.domain.Section;
import com.denchic45.kts.data.model.room.SectionEntity;

import org.mapstruct.Mapper;

@Mapper
public interface SectionMapper extends DomainEntityMapper<Section, SectionEntity> {
}

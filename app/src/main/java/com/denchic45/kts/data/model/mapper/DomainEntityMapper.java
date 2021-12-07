package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.DomainModel;
import com.denchic45.kts.data.model.EntityModel;

import java.util.List;

public interface DomainEntityMapper<DomainT extends DomainModel, EntityT extends EntityModel> {

    EntityT domainToEntity(DomainT domain);

    DomainT entityToDomain(EntityT entity);

    List<EntityT> domainToEntity(List<DomainT> domain);

    List<DomainT> entityToDomain(List<EntityT> entity);
}

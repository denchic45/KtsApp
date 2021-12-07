package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.DocModel;
import com.denchic45.kts.data.model.EntityModel;

import java.util.List;

public interface DocEntityMapper<DocT extends DocModel, EntityT extends EntityModel> {

    EntityT docToEntity(DocT doc);

    DocT entityToDoc(EntityT entity);

    List<EntityT> docToEntity(List<DocT> doc);

    List<DocT> entityToDoc(List<EntityT> entity);
}

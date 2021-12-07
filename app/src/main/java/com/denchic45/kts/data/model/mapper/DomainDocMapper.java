package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.DocModel;
import com.denchic45.kts.data.model.DomainModel;

import java.util.List;

public interface DomainDocMapper<DomainT extends DomainModel, DocT extends DocModel>{
    DocT domainToDoc(DomainT domain);

    DomainT docToDomain(DocT doc);

    List<DocT> domainToDoc(List<DomainT> domain);

    List<DomainT> docToDomain(List<DocT> doc);
}

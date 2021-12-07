package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.domain.User;
import com.denchic45.kts.data.model.firestore.UserDoc;
import com.denchic45.kts.data.model.room.UserEntity;
import com.denchic45.kts.utils.SearchKeysGenerator;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends DocEntityMapper<UserDoc, UserEntity>, DomainEntityMapper<User, UserEntity>, DomainDocMapper<User, UserDoc> {

    default Map<String, Object> entityToMap(List<UserEntity> entities) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("students", entities);
        return map;
    }

    @Mapping(target = "searchKeys", expression = "java(addSearchKeys(domain.getFullName()))")
    @Override
    UserDoc domainToDoc(User domain);

    @Mapping(target = "searchKeys", expression = "java(addSearchKeys(entity.getFullName()))")
    @Override
    UserDoc entityToDoc(UserEntity entity);

    default List<String> addSearchKeys(@NotNull String fullName) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        return generator.generateKeys(fullName, predicate -> predicate.length() > 2);
    }
}

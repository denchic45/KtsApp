package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.domain.User;
import com.denchic45.kts.data.model.firestore.UserDoc;
import com.denchic45.kts.data.model.room.UserEntity;
import com.denchic45.kts.utils.SearchKeysGenerator;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
@Mapper
public interface UserMapper {

    default Map<String, Object> entityToMap(List<UserEntity> entities) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("students", entities);
        return map;
    }

    List<UserDoc> entityToDoc(List<UserEntity> entity);


    List<UserDoc> domainToDoc(List<User> domain);

    User entityToDomain(UserEntity entity);

    List<User> entityToDomain(List<UserEntity> entity);

    @Mapping(target = "searchKeys", expression = "java(addSearchKeys(domain.getFullName()))")
    UserDoc domainToDoc(User domain);

    @Mapping(target = "searchKeys", expression = "java(addSearchKeys(entity.getFullName()))")
    UserDoc entityToDoc(UserEntity entity);

    default List<String> addSearchKeys(@NotNull String fullName) {
        SearchKeysGenerator generator = new SearchKeysGenerator();
        return generator.generateKeys(fullName, predicate -> predicate.length() > 2);
    }

    UserEntity docToEntity(@NotNull UserDoc user);

    List<UserEntity> docToEntity(@NotNull List<UserDoc> users);

    @NotNull User docToDomain(@NotNull UserDoc users);

    @NotNull List<User> docToDomain(@NotNull List<UserDoc> users);
}

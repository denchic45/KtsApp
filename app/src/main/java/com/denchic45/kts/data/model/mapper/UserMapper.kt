package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.domain.model.User
import com.denchic45.kts.data.remotedb.model.UserDoc
import com.denchic45.kts.data.model.room.UserEntity
import org.mapstruct.Mapper

@Mapper
abstract class UserMapper {

    abstract fun entityToDoc(entity: UserEntity): UserDoc
    abstract fun entityToDoc(entity: List<UserEntity>): List<UserDoc>

    abstract fun entityToDomain(entity: UserEntity): User
    abstract fun entityToDomain(entity: List<UserEntity>): List<User>

    abstract fun domainToDoc(domain: User): UserDoc

    abstract fun domainToDoc(domain: List<User>): List<UserDoc>

    abstract fun docToEntity(user: UserDoc): UserEntity
    abstract fun docToEntity(users: List<UserDoc>): List<UserEntity>

    abstract fun docToDomain(users: UserDoc): User
    abstract fun docToDomain(users: List<UserDoc>): List<User>
}
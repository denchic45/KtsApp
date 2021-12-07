package com.denchic45.kts.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.denchic45.kts.data.model.room.SpecialtyEntity;

@Dao
public abstract class SpecialtyDao extends BaseDao<SpecialtyEntity> {

    @Query("DELETE FROM specialty WHERE specialtyUuid NOT IN (:availableSpecialties)")
    public abstract void deleteMissing(String availableSpecialties);

    @Query("SELECT * FROM specialty WHERE specialtyUuid =:uuid")
    public abstract SpecialtyEntity getByUuidSync(String uuid);

    @Query("SELECT * FROM specialty WHERE specialtyUuid =:uuid")
    public abstract LiveData<SpecialtyEntity> getByUuid(String uuid);
}

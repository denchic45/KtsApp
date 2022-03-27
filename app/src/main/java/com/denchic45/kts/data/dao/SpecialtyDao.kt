package com.denchic45.kts.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.denchic45.kts.data.model.room.SpecialtyEntity;

@Dao
public abstract class SpecialtyDao extends BaseDao<SpecialtyEntity> {

    @Query("DELETE FROM specialty WHERE specialty_id NOT IN (:availableSpecialties)")
    public abstract void deleteMissing(String availableSpecialties);

    @Query("SELECT * FROM specialty WHERE specialty_id =:id")
    public abstract SpecialtyEntity getSync(String id);

    @Query("SELECT * FROM specialty WHERE specialty_id =:id")
    public abstract LiveData<SpecialtyEntity> get(String id);
}

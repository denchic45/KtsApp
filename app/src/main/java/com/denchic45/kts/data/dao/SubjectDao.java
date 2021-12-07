package com.denchic45.kts.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.denchic45.kts.data.model.room.SubjectEntity;

import java.util.List;

@Dao
public abstract class SubjectDao extends BaseDao<SubjectEntity> {

    @Query("SELECT * FROM subject WHERE uuid_subject =:subjectUuid")
    public abstract SubjectEntity getByUuidSync(String subjectUuid);

    @Query("SELECT * FROM subject WHERE uuid_subject =:subjectUuid")
    public abstract LiveData<SubjectEntity> getByUuid(String subjectUuid);

    @Query("SELECT s.* FROM subject s JOIN course c ON s.uuid_subject == c.uuid_subject JOIN group_course gc ON gc.uuid_course == c.uuid_course WHERE gc.uuid_group =:groupUuid")
    public abstract LiveData<List<SubjectEntity>> getByGroupUuid(String groupUuid);

//    @Query("DELETE FROM subject WHERE uuid_subject IN(SELECT s.uuid_subject FROM subject s JOIN course c JOIN group_course gc ON c.uuid_subject == s.uuid_subject AND c.uuid_course == gc.uuid_course WHERE gc.uuid_group =:groupUuid AND s.uuid_subject NOT IN (:availableSubjectUuids) )")
//    public abstract void deleteMissingByGroup(List<String> availableSubjectUuids, String groupUuid);

    @Query("DELETE FROM subject WHERE uuid_subject NOT IN(SELECT s.uuid_subject FROM subject s INNER JOIN course c ON c.uuid_subject == s.uuid_subject)")
    public abstract void deleteUnrelatedByCourse();
}
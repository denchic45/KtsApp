package com.denchic45.kts.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.denchic45.kts.data.model.room.SubjectEntity;

import java.util.List;

@Dao
public abstract class SubjectDao extends BaseDao<SubjectEntity> {

    @Query("SELECT * FROM subject WHERE subject_id =:subjectUuid")
    public abstract SubjectEntity getByUuidSync(String subjectUuid);

    @Query("SELECT * FROM subject WHERE subject_id =:subjectUuid")
    public abstract LiveData<SubjectEntity> getByUuid(String subjectUuid);

    @Query("SELECT s.* FROM subject s JOIN course c ON s.subject_id == c.subject_id JOIN group_course gc ON gc.uuid_course == c.uuid_course WHERE gc.group_id =:groupId")
    public abstract LiveData<List<SubjectEntity>> getByGroupUuid(String groupId);

//    @Query("DELETE FROM subject WHERE subject_id IN(SELECT s.subject_id FROM subject s JOIN course c JOIN group_course gc ON c.subject_id == s.subject_id AND c.uuid_course == gc.uuid_course WHERE gc.group_id =:groupUuid AND s.subject_id NOT IN (:availableSubjectUuids) )")
//    public abstract void deleteMissingByGroup(List<String> availableSubjectUuids, String groupUuid);

    @Query("DELETE FROM subject WHERE subject_id NOT IN(SELECT s.subject_id FROM subject s INNER JOIN course c ON c.subject_id == s.subject_id)")
    public abstract void deleteUnrelatedByCourse();
}
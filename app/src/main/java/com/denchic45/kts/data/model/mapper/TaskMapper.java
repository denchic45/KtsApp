package com.denchic45.kts.data.model.mapper;

import com.denchic45.kts.data.model.domain.Task;
import com.denchic45.kts.data.model.firestore.TaskDoc;
import com.denchic45.kts.data.model.room.TaskEntity;

import org.mapstruct.Mapper;

@Mapper
public interface TaskMapper extends DomainEntityMapper<Task, TaskEntity>,
        DocEntityMapper<TaskDoc,TaskEntity>,
        DomainDocMapper<Task,TaskDoc>{
    @Override
    TaskEntity domainToEntity(Task domain);

    @Override
    Task entityToDomain(TaskEntity entity);
}

package com.lnet.tms.mapper;

import com.lnet.model.tms.exception.exceptionEntity.OperationExceptionLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExceptionLogMapper {
    int insert(OperationExceptionLog operationExceptionLog);

    int batchInsert(List<OperationExceptionLog> logs);
}

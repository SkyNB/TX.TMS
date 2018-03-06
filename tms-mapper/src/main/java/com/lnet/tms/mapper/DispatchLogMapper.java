package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchLogMapper {

    int insert(DispatchLog record);

    List<DispatchLog> findByDispatchId(String dispatchId);

    int batchInsert(List<DispatchLog> records);
}
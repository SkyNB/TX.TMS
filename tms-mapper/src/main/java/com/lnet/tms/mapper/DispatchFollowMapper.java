package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFollow;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DispatchFollowMapper {

    List<DispatchFollow> findByDispatchId(String dispatchId);

    int batchInsert(List<DispatchFollow> records);

    int deleteByDispatchId(String dispatchId);

    List<DispatchFollow> findByDispatchIds(List<String> strings);
}
package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeDetail;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchFeeDetailMapper {

    List<DispatchFeeDetail> findByDispatchId(String dispatchId);

    int batchInsert(List<DispatchFeeDetail> records);

    int batchUpdate(List<DispatchFeeDetail> records);
}
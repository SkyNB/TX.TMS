package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeApplyItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchFeeApplyItemMapper {

    List<DispatchFeeApplyItem> findByApplyId(String feeApplyId);

    int batchUpdate(List<DispatchFeeApplyItem> records);

    int batchInsert(List<DispatchFeeApplyItem> items);
}
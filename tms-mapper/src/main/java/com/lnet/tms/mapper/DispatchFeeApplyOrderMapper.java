package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeApplyOrder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchFeeApplyOrderMapper {

    int batchInsert(List<DispatchFeeApplyOrder> dispatchFeeApplyOrders);

    List<DispatchFeeApplyOrder> findByFeeApplyId(String feeApplyId);
}

package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeApplyPic;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchFeeApplyPicMapper {

    int batchInsert(List<DispatchFeeApplyPic> dispatchFeeApplyPicList);

    List<DispatchFeeApplyPic> findByFeeApplyId(String feeApplyId);
}
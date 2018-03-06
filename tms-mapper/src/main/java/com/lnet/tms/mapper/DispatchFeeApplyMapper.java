package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeApply;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DispatchFeeApplyMapper {

    int insert(DispatchFeeApply record);

    DispatchFeeApply get(String feeApplyId);

    int update(DispatchFeeApply record);

    List<DispatchFeeApply> pageList(Map<String, Object> params);

    List<DispatchFeeApply> searchFeeApplies(
            @Param("siteCode") String siteCode, @Param("applyUserId") String applyUserId, @Param("applyDate") String applyDate);
}
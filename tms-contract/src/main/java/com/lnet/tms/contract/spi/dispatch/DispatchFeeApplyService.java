package com.lnet.tms.contract.spi.dispatch;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeApply;

import java.util.Map;

public interface DispatchFeeApplyService {

    PageResponse<DispatchFeeApply> pageList(Integer page, Integer pageSize, Map<String, Object> params);

    Response<DispatchFeeApply> get(String feeApplyId);

    Response<DispatchFeeApply> create(DispatchFeeApply dispatchFeeApply);

    Response<DispatchFeeApply> approve(DispatchFeeApply dispatchFeeApply);

    Response<DispatchFeeApply> update(DispatchFeeApply dispatchFeeApply);

    ListResponse<DispatchFeeApply> searchFeeApplies(String siteCode, String applyUserId, String applyDate);

    Response<Map<String,Object>> getMap(String feeApplyId);
}

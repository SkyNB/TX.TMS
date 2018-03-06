package com.lnet.tms.contract.spi.dispatch;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.oms.order.orderDto.DispatchQueryDto;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchDto;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchMonthDto;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchPayableDto;
import com.lnet.model.tms.dispatch.dispatchEntity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DispatchService {

    /**
     * 创建派车单
     *
     * @param dispatch
     * @return
     */
    Response<Dispatch> create(Dispatch dispatch);

    /**
     * 根据ID获取派车单
     *
     * @param dispatchId
     * @return
     */
    Response<Dispatch> get(String dispatchId);

    /**
     * 加单
     *
     * @param model
     * @return
     */
    Response<Dispatch> addOrders(DispatchOperateModel model);

    /**
     * 减单
     *
     * @param dispatchId
     * @param orderNos
     * @param operatorId
     * @return
     */
    Response<Dispatch> removeOrders(String dispatchId, List<String> orderNos, String operatorId);

    /**
     * 派车单指派司机
     *
     * @param dispatch
     * @return
     */
    Response<Dispatch> assign(Dispatch dispatch);

    /**
     * APP司机登录后，根据不同操作查询派车单列表
     *
     * @param vehicleId
     * @param status
     * @return
     */
    Response<List<Dispatch>> findForDriver(String vehicleId, Dispatch.statusEnum status);

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param params
     * @return
     */
    PageResponse<DispatchDto> pageList(Integer page, Integer pageSize, Map<String, Object> params);

    /**
     * 司机接受已指派的派车单
     *
     * @param dispatchId
     * @return
     */
    Response<Dispatch> accept(String dispatchId, String operatorId);

    /**
     * 司机拒绝已指派的派车单
     *
     * @param dispatchId
     * @return
     */
    Response<Dispatch> reject(String dispatchId, String operatorId, String notes);

    /**
     * 派车单按单装车
     *
     * @param dispatchId
     * @param orderNos
     * @param operatorId
     * @return
     */
    Response<Dispatch> loadOrders(String dispatchId, List<String> orderNos, String operatorId);

    /**
     * 派车单按箱装车
     *
     * @param dispatchId
     * @param packageNos
     * @param operatorId
     * @return
     */
    Response<Dispatch> loadPackages(String dispatchId, List<String> packageNos, String operatorId);

    /**
     * 派车单完成装车
     *
     * @param dispatchId
     * @param operatorId
     * @return
     */
    Response<Dispatch> finishLoading(String dispatchId, String operatorId);

    /**
     * 派车单在途
     *
     * @param dispatchId
     * @param operatorId
     * @return
     */
    Response<Dispatch> start(String dispatchId, LocalDateTime startTime, String operatorId);

    /**
     * 派车单完成
     *
     * @param dispatchId
     * @param finishOrderNos
     * @param notFinishOrderNos
     * @param operatorId
     * @return
     */
    Response<Dispatch> finish(String dispatchId, List<String> finishOrderNos, List<String> notFinishOrderNos, String operatorId);

    /**
     * 派车单取消
     *
     * @param dispatchId
     * @param operatorId
     * @return
     */
    Response<Dispatch> cancel(String dispatchId, String notes, String operatorId);

    /**
     * @param branchCode
     * @param siteCode
     * @param condition
     * @param driverId
     * @param assignDate
     * @return
     */
    ListResponse<Dispatch> searchDispatch(String branchCode, String siteCode, String condition, String driverId, String assignDate);

    /**
     * 查询派车单明细
     *
     * @param dispatchId
     * @return
     */
    ListResponse<DispatchItem> findItems(String dispatchId);

    /**
     * 查询派车单明细
     *
     * @param dispatchIds
     * @return
     */
    ListResponse<DispatchItem> findItems(List<String> dispatchIds);

    /**
     * 指定派车单跟车人
     *
     * @param dispatchId
     * @param follows
     * @param operatorId
     * @return
     */
    Response flowers(String dispatchId, List<DispatchFollow> follows, String operatorId);

    /**
     * 查询订单派车箱数
     *
     * @param siteCode
     * @param orderNos
     * @return
     */
    ListResponse<OrderDispatchPackageQty> findOrderDispatchPackageQty(String siteCode, List<String> orderNos);

    /**
     * @param createdBy
     * @return
     */
    ListResponse<DispatchMonthDto> searchGroupByMonth(String createdBy);

    /**
     * 更新派车单费用
     *
     * @param dispatchId
     * @param feeDetails
     * @param operatorId
     * @return
     */
    Response updateFee(String dispatchId, List<DispatchFeeDetail> feeDetails, String operatorId);

    /**
     * 根据派车单号查询
     *
     * @param dispatchNo
     * @return
     */
    Response<Dispatch> getByNo(String dispatchNo);

    /**
     * 根据订单号查询派车单明细集合
     *
     * @param siteCode
     * @param orderNos
     * @return
     */
    /**
     * 根据订单号查询派车单明细集合
     *
     * @param siteCode
     * @param orderNos
     * @return
     */
    ListResponse<DispatchItem> findItemsByOrderNos(String siteCode, List<String> orderNos);

    /***
     *
     * @param siteCode
     * @param orderNo
     * @return
     */
    ListResponse<Dispatch> findDispatchByOrderNo(String siteCode, String orderNo);

    /**
     * 查询当天派车量
     *
     * @param branchCode
     * @param siteCode
     * @return
     */
    Response<Integer> getTodayDispatch(String branchCode, String siteCode);

    Response<Map<String,Object>> getMap(String dispatchId);

    Response getOrderCount(String currentBranchCode, String currentSiteCode);

    ListResponse<Dispatch> findByDrivers(List<String> vehicleIds, Dispatch.statusEnum status);

    PageResponse<DispatchPayableDto> searchDispatchPay(Integer page, Integer pageSize, Map<String, Object> filterMap);

    PageResponse<DispatchQueryDto> dispatchQueryPageList(Integer page, Integer pageSize, Map<String, Object> params);

    PageResponse searchDispatchItemPay(Integer page, Integer pageSize, Map<String, Object> filterMap);
}

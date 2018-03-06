package com.lnet.tms.contract.spi.consgin;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.lnet.model.oms.order.orderDto.ConsignQueryDto;
import com.lnet.model.tms.consign.consignDto.*;
import com.lnet.model.tms.consign.consignEntity.*;

public interface ConsignOrderService {

    /**
     * 创建托运单
     *
     * @param consignOrder
     * @return
     */
    Response<ConsignOrder> create(ConsignOrder consignOrder);

    /**
     * 更新托运单
     *
     * @param consignOrder
     * @param operatorId
     * @return
     */
    public Response<ConsignOrder> update(ConsignOrder consignOrder, String operatorId);

    /**
     * 托运单发运
     *
     * @param consignOrder
     * @param operatorId
     * @return
     */
    Response<ConsignOrder> consign(ConsignOrder consignOrder, String operatorId);

    /**
     * 批量发运
     *
     * @param model
     * @param isMerge
     * @return
     */
    Response batchConsign(BatchConsignModel model, boolean isMerge);

    /**
     * 托运单取消
     *
     * @param consignOrderId
     * @param operatorId
     * @param notes
     * @param siteCode
     * @return
     */
    public Response cancel(String consignOrderId, String operatorId, String notes, String siteCode);

    /**
     * 根据ID查询托运单
     *
     * @param consignOrderId
     * @return
     */
    public Response<Map<String, Object>> get(String consignOrderId);

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param params
     * @return
     */
    PageResponse<ConsignOrderPageDto> pageList(Integer page, Integer pageSize, Map<String, Object> params);

    /**
     * 更新托运单临时单号
     *
     * @param consignOrderId
     * @param consignOrderNo
     * @param operatorId
     * @return
     */
    Response<ConsignOrder> updateOrderNo(String consignOrderId, String consignOrderNo, String operatorId);

    /**
     * 根据ID批量查询托运单
     *
     * @param consignOrderIds
     * @return
     */
    ListResponse<ConsignOrder> findByIds(List<String> consignOrderIds);

    /**
     * 根据ID批量查询托运单明细
     *
     * @param consignOrderIds
     * @return
     */
    ListResponse<ConsignOrderItem> findItemByIds(List<String> consignOrderIds);

    /**
     * 查询订单发运箱数信息
     *
     * @param siteCode
     * @param orderNos
     * @return
     */
    ListResponse<OrderConsignPackageQty> findOrderConsignPackageQty(String siteCode, List<String> orderNos);

    /**
     * 批量查询托运单，同一单号会存在不同承运商的托运单中
     *
     * @param consignOrderNos
     * @return
     */
    ListResponse<ConsignOrder> findByNos(List<String> consignOrderNos);

    /**
     * 根据承运商编码及托运单号查询托运单
     *
     * @param carrierCode
     * @param consignOrderNo
     * @return
     */
    Response<ConsignOrder> getByNo(String carrierCode, String consignOrderNo);

    /**
     * 根据新易泰单号查询托运单信息
     *
     * @param orderNo
     * @return
     */
    ListResponse<ConsignOrder> findByOrderNo(String orderNo);

    /**
     *
     *
     * @param createdBy
     * @return
     */
    ListResponse<OrderMonthDto> findOrderByMonth(String createdBy);

    /**
     *
     *
     * @param branchCode
     * @param siteCode
     * @return
     */
    Response getTodayConsignCount(String branchCode, String siteCode);

    /**
     *
     *
     * @param carrierCode
     * @return
     */
    ListResponse<ConsignOrder> payableModifyQuery(String carrierCode);

    /**
     * 查询托运单明细
     * @param orderNos
     * @return
     */
    ListResponse<ConsignDetailDto> findDetailsByOrderNos(List<String> orderNos);

    /**
     * 根据订单号查询托运单日志信息
     *
     * @param orderNo
     * @return
     */
    ListResponse<ConsignOrderLog> findConsignOrderLog(String orderNo);

    /**
     *
     *
     * @param vehicleId
     * @param currentBranchCode
     * @param currentSiteCode
     * @return
     */
    List<ConsignOrderItemDto> createByDriverQuery(String vehicleId, String currentBranchCode, String currentSiteCode);

    /**
     *
     *
     * @param consignOrderIds
     * @param arriveTime
     * @param userId
     * @param currentSiteCode
     * @return
     */
    Response arrive(List<String> consignOrderIds, LocalDateTime arriveTime, String userId, String currentSiteCode);

    /**
     *
     *
     * @param consignOrderIds
     * @param startUpTime
     * @param userId
     * @param currentSiteCode
     * @param name
     * @return
     */
    Response startUp(List<String> consignOrderIds, LocalDateTime startUpTime, String userId, String currentSiteCode, String name);

    /**
     *
     *
     * @param orderNos
     * @param siteCode
     * @return
     */
    public ListResponse<ConsignOrderItemDto> findItemDtoListByOrderNos(List<String> orderNos, String siteCode);

    /**
     *
     *
     * @param consignOrderIds
     * @param finishTime
     * @param operatorId
     * @return
     */
    public Response finish(List<String> consignOrderIds, LocalDateTime finishTime, String operatorId);

    /**
     *
     *
     * @param consignOrderId
     * @return
     */
    public Response<ConsignOrder> getById(String consignOrderId);

    //public Response getByNo(String carrieCode, String consignOrderNo);
    public Response getDetailByNo(String carrieCode, String consignOrderNo);

    /**
     *
     *
     * @param params
     * @return
     */
    public ListResponse<ConsignQueryDto> queryConsign(Map<String, Object> params);

    /**
     *
     *
     * @param page
     * @param pageSize
     * @param params
     * @return
     */
    public PageResponse<ConsignPayableDto> searchConsignPayable(Integer page, Integer pageSize, Map<String, Object> params);

    /**
     *
     *
     * @param page
     * @param pageSize
     * @param params
     * @return
     */
    public PageResponse<ConsignQueryDto> selectOrder(Integer page, Integer pageSize, Map<String, Object> params);
}

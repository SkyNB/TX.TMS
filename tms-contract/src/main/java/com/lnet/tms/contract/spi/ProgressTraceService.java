package com.lnet.tms.contract.spi;

import com.lnet.framework.core.Response;
import com.lnet.model.tms.PackageTraceModel;
import com.lnet.model.tms.delivery.DeliveryOrderTraceCreateModel;
import com.lnet.model.tms.delivery.DeliveryOrderTraceItem;
import com.lnet.model.tms.order.orderEntity.OrderTrace;
import com.lnet.model.tms.order.orderEntity.OrderTraceModel;

import java.util.List;

public interface ProgressTraceService {
    /**
     * 记录客户订单进度信息
     *
     * @return
     */
    Response addOrderTrace(OrderTraceModel orderTrace);

    /**
     * 批量记录客户订单进度信息
     *
     * @param orderTraceModels
     * @return
     */
    Response batchAddOrderTrace(List<OrderTraceModel> orderTraceModels);

    /**
     * 记录运输订单进度信息
     *
     * @return
     */
    Response addDeliverOrderTrace(DeliveryOrderTraceCreateModel deliveryOrderTraceModel);

    /**
     * 批量记录运输订单进度信息
     *
     * @param deliveryOrderTraceCreateModels
     * @return
     */
    Response batchAddDeliveryOrderTrace(List<DeliveryOrderTraceCreateModel> deliveryOrderTraceCreateModels);

    /**
     * 记录包/箱的进度
     *
     * @return
     */
    Response addPackageTrace(PackageTraceModel packageTraceModel);

    // 整车跟踪

    /**
     * 查询订单跟踪信息
     *
     * @return
     */
    Response<OrderTrace> OrderProgressQuery(String orderNo);

    /**
     * 查询运输订单跟踪信息
     *
     * @param deliveryNo 运输单号
     * @param shipper 承运商编码
     * @return
     */
    Response<List<DeliveryOrderTraceItem>> deliveryOrderProgressQuery(String deliveryNo, String shipper);

    /**
     * 查询订单的跟踪信息（订单进度信息和运输订单进度信息综合起来）
     *
     * @param orderNo
     * @return
     */
    Response<OrderTrace> progressQuery(String orderNo);

    Response<OrderTrace> orderTraceQuery(String orderNo);
}

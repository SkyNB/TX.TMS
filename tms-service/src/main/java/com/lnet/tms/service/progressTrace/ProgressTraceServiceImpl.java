package com.lnet.tms.service.progressTrace;

import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.oms.order.orderEntity.OrderLog;
import com.lnet.model.tms.PackageTraceModel;
import com.lnet.model.tms.delivery.DeliveryOrderTrace;
import com.lnet.model.tms.delivery.DeliveryOrderTraceCreateModel;
import com.lnet.model.tms.delivery.DeliveryOrderTraceItem;
import com.lnet.model.tms.order.orderEntity.OrderTrace;
import com.lnet.model.tms.order.orderEntity.OrderTraceItem;
import com.lnet.model.tms.order.orderEntity.OrderTraceModel;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import com.lnet.model.tms.consign.consignEntity.ConsignOrderLog;
import com.lnet.tms.contract.spi.ProgressTraceService;
import com.lnet.tms.contract.spi.consgin.ConsignOrderService;
import com.lnet.tms.mapper.DeliveryOrderTraceItemMapper;
import com.lnet.tms.mapper.DeliveryOrderTraceMapper;
import com.lnet.tms.mapper.OrderTraceItemMapper;
import com.lnet.tms.mapper.OrderTraceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ProgressTraceServiceImpl implements ProgressTraceService {
    private final String className = this.getClass().getSimpleName() + ".";
    @Resource
    private OrderTraceMapper orderTraceMapper;
    @Resource
    private OrderTraceItemMapper orderTraceItemMapper;
    @Resource
    private DeliveryOrderTraceMapper deliveryOrderTraceMapper;
    @Resource
    private DeliveryOrderTraceItemMapper deliveryOrderTraceItemMapper;

    @Resource
    LogisticsOrderService logisticsOrderService;
    @Resource
    ConsignOrderService consignOrderService;

    @Override
    public Response addOrderTrace(OrderTraceModel orderTraceModel) {
        try {
            Assert.notNull(orderTraceModel);
            Assert.hasText(orderTraceModel.getOrderNo());

            boolean isSuccess;
            Optional<OrderTrace> optionalOrderTrace = Optional.ofNullable(orderTraceMapper.getByOrderNo(orderTraceModel.getOrderNo()));
            if (optionalOrderTrace.isPresent()) {
                //add OrderTraceItems
                isSuccess = addOrderTraceItems(orderTraceModel, optionalOrderTrace.get().getTracingId());
            } else {
                //add OrderTrace
                OrderTrace orderTrace = OrderTrace.builder()
                        .tracingId(Snowflake.getInstance().next())
                        .orderNo(orderTraceModel.getOrderNo())
                        .customerOrderNo(orderTraceModel.getCustomerOrderNo())
                        .customerCode(orderTraceModel.getCustomerCode())
                        .build();
                isSuccess = orderTraceMapper.insert(orderTrace) > 0;
                if (isSuccess) {
                    //add OrderTraceItems
                    isSuccess = addOrderTraceItems(orderTraceModel, orderTrace.getTracingId());
                }
            }

            return isSuccess ? ResponseBuilder.success("记录成功!") : ResponseBuilder.fail("记录失败!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error(className + "addOrderTrace", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response addDeliverOrderTrace(DeliveryOrderTraceCreateModel deliveryOrderTraceCreateModel) {
        try {
            Assert.notNull(deliveryOrderTraceCreateModel);
            Assert.hasText(deliveryOrderTraceCreateModel.getDeliveryNo());

            Boolean isSuccess = false;
            Optional<DeliveryOrderTrace> optional = Optional.ofNullable(deliveryOrderTraceMapper.getByDeliveryNoAndShipper(deliveryOrderTraceCreateModel.getDeliveryNo(), deliveryOrderTraceCreateModel.getShipper()));

            if (optional.isPresent()) {
                //存在 添加明细
                isSuccess = addDeliveryOrderItem(deliveryOrderTraceCreateModel, optional.get().getTracingId());
            } else {
                //不存在 新建记录
                DeliveryOrderTrace deliveryOrderTrace = DeliveryOrderTrace.builder()
                        .tracingId(Snowflake.getInstance().next())
                        .deliveryNo(deliveryOrderTraceCreateModel.getDeliveryNo())
                        .shipper(deliveryOrderTraceCreateModel.getShipper())
                        .build();

                isSuccess = deliveryOrderTraceMapper.insert(deliveryOrderTrace) > 0;
                if (isSuccess)
                    addDeliveryOrderItem(deliveryOrderTraceCreateModel, deliveryOrderTrace.getTracingId());
            }
            return isSuccess ? ResponseBuilder.success("添加成功!") : ResponseBuilder.fail("添加失败!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error(className + "addPackageTrace", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response batchAddOrderTrace(List<OrderTraceModel> orderTraceModels) {
        return null;
    }

    @Override
    public Response batchAddDeliveryOrderTrace(List<DeliveryOrderTraceCreateModel> deliveryOrderTraceCreateModels) {
        // TODO: 2016/10/18 此方法的效率还可以进一步优化（如果导入的不同的托运单信息越多，访问的数据库次数就越多，消耗资源越多）
        try {
            List<String> deliveryNos = deliveryOrderTraceCreateModels.stream().map(m -> m.getDeliveryNo()).distinct().collect(Collectors.toList());//批量数据中的运输单号
            List<DeliveryOrderTrace> deliveryOrderTraces = deliveryOrderTraceMapper.findByDeliveryNos(deliveryNos);//查找存在的信息记录

            //按运输单号分组
            Map<String, List<DeliveryOrderTraceCreateModel>> groupByDeliveryNo = deliveryOrderTraceCreateModels.stream().collect(Collectors.groupingBy(DeliveryOrderTraceCreateModel::getDeliveryNo));

            //遍历groupByDeliveryNo
            groupByDeliveryNo.entrySet().forEach(m -> {

                List<DeliveryOrderTraceCreateModel> tem = m.getValue();
                //在已经按运输单号分组的情况下再按承运商编码分组
                Map<String, List<DeliveryOrderTraceCreateModel>> groupByShip = tem.stream().collect(Collectors.groupingBy(DeliveryOrderTraceCreateModel::getShipper));

                groupByShip.entrySet().forEach(m2 -> {
                    Optional<DeliveryOrderTrace> optional = deliveryOrderTraces.stream().filter(f -> f.getDeliveryNo().equals(m.getKey()) && f.getShipper().equals(m2.getKey())).findFirst();

                    if (optional.isPresent()) {
                        DeliveryOrderTrace deliveryOrderTrace = optional.get();
                        deliveryOrderTraces.remove(deliveryOrderTrace);

                        //存在 添加明细
                        List<DeliveryOrderTraceItem> items = m2.getValue().stream().map(e -> {
                            return DeliveryOrderTraceItem.builder()
                                    .itemId(Snowflake.getInstance().next())
                                    .tracingId(deliveryOrderTrace.getTracingId())
                                    .operator(e.getOperator())
                                    .operateTime(e.getOperateTime())
                                    .operateAddress(e.getOperateAddress())
                                    .description(e.getDescription())
                                    .tracingTime(LocalDateTime.now())
                                    .build();
                        }).collect(Collectors.toList());

                        deliveryOrderTraceItemMapper.batchInsert(items);
                    } else {
                        //不存在 新建记录

                        List<DeliveryOrderTraceCreateModel> createModels = m2.getValue();

                        //新建运输订单跟踪记录
                        DeliveryOrderTrace deliveryOrderTrace = DeliveryOrderTrace.builder()
                                .tracingId(Snowflake.getInstance().next())
                                .shipper(createModels.get(0).getShipper())
                                .deliveryNo(createModels.get(0).getDeliveryNo())
                                .build();
                        boolean isSuccess = deliveryOrderTraceMapper.insert(deliveryOrderTrace) > 0;

                        //新增运输订单跟踪记录明细
                        if (isSuccess) {
                            List<DeliveryOrderTraceItem> items = createModels.stream().map(e -> {
                                return DeliveryOrderTraceItem.builder()
                                        .itemId(Snowflake.getInstance().next())
                                        .tracingId(deliveryOrderTrace.getTracingId())
                                        .operator(e.getOperator())
                                        .operateTime(e.getOperateTime())
                                        .operateAddress(e.getOperateAddress())
                                        .description(e.getDescription())
                                        .tracingTime(LocalDateTime.now())
                                        .build();
                            }).collect(Collectors.toList());

                            deliveryOrderTraceItemMapper.batchInsert(items);
                        }
                    }
                });
            });

            return ResponseBuilder.success("", "批量创建运输订单跟踪信息成功！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response addPackageTrace(PackageTraceModel packageTraceModel) {
        return ResponseBuilder.success();
    }

    @Override
    public Response<OrderTrace> OrderProgressQuery(String orderNo) {
        // TODO: 2016/6/23 对接OMS查询跟踪信息
        try {
            Assert.hasText(orderNo);

            OrderTrace orderTrace = orderTraceMapper.getByOrderNo(orderNo);

            if (orderTrace != null)
                orderTrace.setItems(orderTraceItemMapper.getByTracingId(orderTrace.getTracingId()));

            return ResponseBuilder.success(orderTrace);
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<List<DeliveryOrderTraceItem>> deliveryOrderProgressQuery(String deliveryNo, String shipper) {
        try {
            Assert.hasText(deliveryNo);

            List<DeliveryOrderTraceItem> listModels = deliveryOrderTraceItemMapper.getByDeliveryNoAndShipper(deliveryNo, shipper);
            return ResponseBuilder.success(listModels);
        } catch (Exception e) {
            log.error(className + "deliveryOrderProgressQuery", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<OrderTrace> progressQuery(String orderNo) {
        return null;
    }

    @Override
    public Response<OrderTrace> orderTraceQuery(String orderNo) {
        List<ConsignOrder> consignOrders = consignOrderService.findByOrderNo(orderNo).getBody();
        List<DeliveryOrderTraceItem> deliveryOrderTraceItems = new ArrayList<>();

        //订单所在的托运单跟踪信息
        consignOrders.forEach(f -> {
            deliveryOrderTraceItems.addAll(deliveryOrderProgressQuery(f.getConsignOrderNo(), f.getCarrierCode()).getBody());
        });

        //订单的物流信息
        Response<OrderTrace> response = OrderProgressQuery(orderNo);
        OrderTrace orderTrace;
        if (response.getBody() != null)
            orderTrace = response.getBody();
        else
            orderTrace = new OrderTrace();

        List<OrderTraceItem> orderTraceItems = new ArrayList<>();
        if (orderTrace.getItems() != null)
            orderTraceItems.addAll(orderTrace.getItems());

        //托运单日志
        List<ConsignOrderLog> coLogs = consignOrderService.findConsignOrderLog(orderNo).getBody();

        //订单日志
        List<OrderLog> orLogs = logisticsOrderService.findOrderLogs(orderNo).getBody();

        //联合托运单跟踪信息
        deliveryOrderTraceItems.forEach(f -> {
            OrderTraceItem item = OrderTraceItem.builder()
                    .operator(f.getOperator())
                    .operateTime(f.getOperateTime())
                    .operateAddress(f.getOperateAddress())
                    .description(f.getDescription())
                    .tracingTime(f.getTracingTime())
                    .build();

            orderTraceItems.add(item);
        });

        //联合托运单日志
        coLogs.forEach(e -> {
            OrderTraceItem item = OrderTraceItem.builder()
                    .operateTime(e.getOperationTime())
                    .description(e.getOperationContent())
                    .build();
            orderTraceItems.add(item);
        });

        //联合订单日志
        orLogs.forEach(e -> {
            OrderTraceItem item = OrderTraceItem.builder()
                    .operateTime(e.getOperationTime())
                    .description(e.getOperationContent())
                    .build();
            orderTraceItems.add(item);
        });

        //将整体的信息按操作时间排序
        List<OrderTraceItem> result = orderTraceItems.stream().sorted(new Comparator<OrderTraceItem>() {
            @Override
            public int compare(OrderTraceItem o1, OrderTraceItem o2) {
                return -1 * (o1.getOperateTime().compareTo(o2.getOperateTime()));
            }
        }).collect(Collectors.toList());
        orderTrace.setItems(result);
        response.setBody(orderTrace);

        return response;
    }

    private boolean addOrderTraceItems(OrderTraceModel orderTraceModel, String tracingId) {
        OrderTraceItem item = OrderTraceItem.builder()
                .itemId(Snowflake.getInstance().next())
                .tracingId(tracingId)
                .operator(orderTraceModel.getOperator())
                .operateTime(orderTraceModel.getOperateTime())
                .operateAddress(orderTraceModel.getOperateAddress())
                .description(orderTraceModel.getDescription())
                .tracingTime(LocalDateTime.now())
                .build();
        return orderTraceItemMapper.insert(item) > 0;
    }

    private boolean addDeliveryOrderItem(DeliveryOrderTraceCreateModel model, String tracingId) {
        DeliveryOrderTraceItem item = DeliveryOrderTraceItem.builder()
                .itemId(Snowflake.getInstance().next())
                .tracingId(tracingId)
                .operator(model.getOperator())
                .operateTime(model.getOperateTime())
                .operateAddress(model.getOperateAddress())
                .description(model.getDescription())
                .tracingTime(LocalDateTime.now())
                .build();
        return deliveryOrderTraceItemMapper.insert(item) > 0;
    }
}

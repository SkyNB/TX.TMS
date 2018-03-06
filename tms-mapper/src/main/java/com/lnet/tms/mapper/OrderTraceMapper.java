package com.lnet.tms.mapper;

import com.lnet.model.tms.order.orderEntity.OrderTrace;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderTraceMapper {

    int insert(OrderTrace orderTrace);

    OrderTrace getById(String tracingId);

    OrderTrace getByOrderNo(String orderNo);

}

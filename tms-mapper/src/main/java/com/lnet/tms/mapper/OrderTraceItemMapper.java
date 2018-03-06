package com.lnet.tms.mapper;

import com.lnet.model.tms.order.orderEntity.OrderTraceItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTraceItemMapper {
    int deleteById(String itemId);

    int insert(OrderTraceItem item);

    List<OrderTraceItem> getByTracingId(String tracingId);

    int update(OrderTraceItem item);

}

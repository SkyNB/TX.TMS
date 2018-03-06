package com.lnet.tms.mapper;

import com.lnet.model.tms.delivery.DeliveryOrderTraceItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryOrderTraceItemMapper {
    Integer insert(DeliveryOrderTraceItem item);

    int batchInsert(List<DeliveryOrderTraceItem> items);

    List<DeliveryOrderTraceItem> getByDeliveryNoAndShipper(@Param("deliveryNo") String deliveryNo, @Param("shipper") String shipper);
}

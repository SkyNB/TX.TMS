package com.lnet.tms.mapper;

import com.lnet.model.tms.delivery.DeliveryOrderTrace;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryOrderTraceMapper {

    Integer insert(DeliveryOrderTrace deliveryOrderTrace);

    DeliveryOrderTrace getByDeliveryNoAndShipper(@Param("deliveryNo") String deliveryNo, @Param("shipper") String shipper);

    List<DeliveryOrderTrace> findByDeliveryNos(List<String> deliveryNos);
}

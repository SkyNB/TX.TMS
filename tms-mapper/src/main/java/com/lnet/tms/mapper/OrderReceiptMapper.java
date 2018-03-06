package com.lnet.tms.mapper;


import com.lnet.model.tms.order.orderEntity.OrderReceipt;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReceiptMapper {
    int insert(OrderReceipt orderReceipt);

    int batchInsert(List<OrderReceipt> orderReceipts);

    List<OrderReceipt> getByCustomerCodeAndOrderNo(@Param("customerCode") String customerCode, @Param("orderNo") String orderNo);

    List<OrderReceipt> findByCustomerOrderNos(List<String> customerOrderNos);
}

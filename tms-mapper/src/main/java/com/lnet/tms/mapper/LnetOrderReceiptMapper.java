package com.lnet.tms.mapper;


import com.lnet.model.tms.order.orderEntity.LnetOrderReceipt;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LnetOrderReceiptMapper {
    int insert(LnetOrderReceipt lnetOrderReceipt);

    int batchInsert(List<LnetOrderReceipt> lnetOrderReceipts);

    LnetOrderReceipt getByLnetOrderNo(String lnetOrderNo);
}

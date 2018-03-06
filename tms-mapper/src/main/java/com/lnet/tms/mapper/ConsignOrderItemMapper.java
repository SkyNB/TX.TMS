package com.lnet.tms.mapper;

import com.lnet.model.tms.consign.consignEntity.ConsignOrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsignOrderItemMapper {
    int delete(String itemId);

    int deleteByConsignOrderId(String consignOrderId);

    int insert(ConsignOrderItem record);

    int batchInsert(List<ConsignOrderItem> items);

    List<ConsignOrderItem> findByConsignOrderId(String consignOrderId);

    int update(ConsignOrderItem record);

    int batchUpdate(List<ConsignOrderItem> items);

    int batchDelete(List<ConsignOrderItem> items);

    List<ConsignOrderItem> findByConsignOrderIds(List<String> consignOrderIds);
}
package com.lnet.tms.mapper;

import com.lnet.model.tms.consign.consignEntity.ConsignOrderLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsignOrderLogMapper {

    int insert(ConsignOrderLog record);

    int batchInsert(List<ConsignOrderLog> logs);

    List<ConsignOrderLog> findByConsignOrderId(String consignOrderId);
}
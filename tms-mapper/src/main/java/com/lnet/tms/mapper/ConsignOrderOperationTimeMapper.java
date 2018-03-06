package com.lnet.tms.mapper;

import com.lnet.model.tms.consign.consignEntity.ConsignOrderOperationTime;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsignOrderOperationTimeMapper {
    int delete(String consignOrderId);

    int insert(ConsignOrderOperationTime record);

    ConsignOrderOperationTime get(String consignOrderId);

    int update(ConsignOrderOperationTime record);

    int batchInsert(List<ConsignOrderOperationTime> records);

    int batchUpdate(List<ConsignOrderOperationTime> records);

    int batchUpdateConsignTime(List<ConsignOrderOperationTime> records);

    int batchUpdateStartUpTime(@Param("consignOrderIds") List<String> consignOrderIds, @Param("startUpTime") LocalDateTime startUpTime);

    int batchUpdateArriveTime(@Param("consignOrderIds") List<String> consignOrderIds, @Param("arriveTime") LocalDateTime arriveTime);

    int batchUpdateFinishTime(@Param("consignOrderIds") List<String> consignOrderIds, @Param("finishTime") LocalDateTime finishTime);

    List<ConsignOrderOperationTime> findByConsignOrderIds(List<String> consignOrderIds);
}
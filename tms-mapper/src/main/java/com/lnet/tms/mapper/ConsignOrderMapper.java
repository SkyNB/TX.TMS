package com.lnet.tms.mapper;

import com.lnet.model.tms.consign.consignDto.ConsignDetailDto;
import com.lnet.model.tms.consign.consignDto.ConsignOrderPageDto;
import com.lnet.model.tms.consign.consignDto.OrderMonthDto;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ConsignOrderMapper {

    int delete(String consignOrderId);

    int insert(ConsignOrder record);

    int batchInsert(List<ConsignOrder> consignOrders);

    ConsignOrder get(String consignOrderId);

    int update(ConsignOrder record);

    List<ConsignOrderPageDto> pageList(Map<String, Object> params);

    boolean orderNoIsExists(@Param("carrierCode") String carrierCode,
                            @Param("consignOrderNo") String consignOrderNo,
                            @Param("consignOrderId") String consignOrderId);

    int updateStatus(@Param("consignOrderId") String consignOrderId,
                     @Param("status") ConsignOrder.consignStatus status);

    int batchUpdateStatus(@Param("consignOrderIds") List<String> consignOrderIds,
                          @Param("status") ConsignOrder.consignStatus status);


    List<ConsignOrder> findByOrderNos(List<String> consignOrderNos);

    List<ConsignOrder> findByIds(List<String> consignOrderIds);

    int updateOrderNo(@Param("consignOrderId") String consignOrderId, @Param("consignOrderNo") String consignOrderNo);

    ConsignOrder getByNo(@Param("carrierCode") String carrierCode, @Param("consignOrderNo") String consignOrderNo);

    List<ConsignOrder> findByOrderNo(String orderNo);

    List<OrderMonthDto> findOrderByMonth(String createdBy);

    Integer getTodayConsignCount(@Param("branchCode") String branchCode, @Param("siteCode") String siteCode);

    List<ConsignOrder> findByCarrierCode(@Param("carrierCode") String carrierCode, @Param("statusList") List<String> status);

    List<ConsignDetailDto> findItemsByOrderNos(List<String> orderNos);
}
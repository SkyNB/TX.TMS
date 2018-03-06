package com.lnet.tms.mapper;


import com.lnet.model.tms.order.orderDto.OrderTransferListDto;
import com.lnet.model.tms.order.orderEntity.OrderTransfer;
import com.lnet.model.tms.transfer.TransferReportListDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderTransferMapper {

    int batchInsert(List<OrderTransfer> orderTransferList);

    int batchUpdate(List<OrderTransfer> orderTransferList);

    int batchUpdateStatus(@Param("status") OrderTransfer.statusEnum status,
                          @Param("siteCode") String siteCode, @Param("orderNos") List<String> orderNos);

    List<OrderTransfer> findBySiteCodeAndOrderNos(@Param("siteCode") String siteCode,
                                                  @Param("orderNos") List<String> orderNos);

    List<OrderTransferListDto> pageList(Map<String, Object> params);

    Long getTransferSequenceNo();

    int batchUpdateArriveInfo(List<OrderTransfer> orderTransferList);

    OrderTransfer getByOrderNoAndTransferSite(@Param("orderNo") String orderNo, @Param("siteCode") String siteCode);

    List<TransferReportListDto> reportPageList(Map<String, Object> params);
}
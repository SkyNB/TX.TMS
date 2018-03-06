package com.lnet.tms.contract.spi;


import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.order.orderDto.OrderTransferListDto;
import com.lnet.model.tms.order.orderEntity.OrderTransfer;
import com.lnet.model.tms.transfer.TransferReceiptDto;
import com.lnet.model.tms.transfer.TransferReportListDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TransferService {

    ListResponse<OrderTransfer> batchCreate(List<OrderTransfer> orderTransferList);

    ListResponse<OrderTransfer> batchArrive(String siteCode, List<String> orderNos, List<String> arriveRemarkList, LocalDateTime arriveTime);

    ListResponse<OrderTransfer> toDispatched(String siteCode, List<String> orderNos);

    ListResponse<OrderTransfer> toConsigned(String siteCode, List<String> orderNos);

    ListResponse<OrderTransfer> saveReceiptInfo(TransferReceiptDto transferReceiptDto);

    PageResponse<OrderTransferListDto> pageList(Integer page, Integer pageSize, Map<String, Object> params);

    Response<OrderTransfer> getByOrderNoAndTransferSite(String orderNo, String siteCode);

    ListResponse<OrderTransfer> findByOrderNosAndSiteCode(List<String> orderNos, String siteCode);

    Response removeToArrived(List<String> orderNos, String siteCode);

    PageResponse<TransferReportListDto> reportPageList(Integer page, Integer pageSize, Map<String, Object> params);

    Response<OrderTransferListDto> findTransferByOrderNo(String orderNo, String currentSiteCode);
}

package com.lnet.tms.contract.spi;

import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.oms.order.orderDto.LogisticsOrderReceiptDto;
import com.lnet.model.tms.order.orderDto.LnetOrderReceiptDto;
import com.lnet.model.tms.order.orderDto.OrderReceiptDto;

import java.util.List;

public interface ReceiptService {

    /**
     * 按客户单号单个上传回单
     *
     * @param orderReceiptDto
     * @return
     */
    Response<String> uploadOrderReceipt(OrderReceiptDto orderReceiptDto);

    /**
     * 按客户单号批量上传回单
     *
     * @param orderReceiptDtos
     * @return
     */
    Response batchUploadOrderReceipt(List<OrderReceiptDto> orderReceiptDtos);

    /**
     * 按新易泰单号单个上传回单
     *
     * @param lnetOrderReceiptDto
     * @return
     */
    Response<String> uploadLnetOrderReceipt(LnetOrderReceiptDto lnetOrderReceiptDto);

    /**
     * 按新易泰单号批量上传回单
     *
     * @param lnetOrderReceiptDtos
     * @return
     */
    Response batchUploadLnetOrderReceipt(List<LnetOrderReceiptDto> lnetOrderReceiptDtos);

    /**
     * 根据新易泰单号查询回单信息
     *
     * @param lnetOrderNo
     * @return
     */
    Response<LnetOrderReceiptDto> getLnetOrderReceipt(String lnetOrderNo);

    /**
     * 根据客户编码和客户单号查询回单信息
     *
     * @param customerCode
     * @param orderNo
     * @return
     */
    Response<List<OrderReceiptDto>> getOrderReceipt(String customerCode, String orderNo);

    /**
     * 按照客户单号查询回单的图片信息
     *
     * @param customerOrderNos
     * @return
     */
    ListResponse<OrderReceiptDto> getOrderReceipt(List<String> customerOrderNos);

    Response receiptScan(List<String> orderNos, String customerCode);

    PageResponse<LogisticsOrderReceiptDto> searchForReceipt(KendoGridRequest request);
}

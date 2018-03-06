package com.lnet.tms.service.transfer;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.BeanHelper;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.ums.transprotation.transprotationDto.LogisticsOrderAllDto;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.tms.order.orderEntity.OrderTransfer;
import com.lnet.model.tms.order.orderDto.OrderTransferListDto;
import com.lnet.model.tms.transfer.TransferReceiptDto;
import com.lnet.model.tms.transfer.TransferReportListDto;
import com.lnet.tms.contract.spi.TransferService;
import com.lnet.tms.mapper.OrderTransferMapper;
import com.lnet.ums.contract.api.CustomerService;
import com.lnet.model.ums.customer.customerEntity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Transactional
@Service
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final String className = this.getClass().getSimpleName() + ".";

    @Autowired
    OrderTransferMapper transferMapper;

    @Autowired
    LogisticsOrderService logisticsOrderService;

    @Resource
    CustomerService customerService;

    @Override
    public ListResponse<OrderTransfer> batchCreate(List<OrderTransfer> orderTransferList) {
        try {
            Assert.notEmpty(orderTransferList);
            orderTransferList.stream().forEach(orderTransfer -> {
                        orderTransfer.setTransferId(Snowflake.getInstance().next());
                        orderTransfer.setCreateDate(LocalDateTime.now());
                        orderTransfer.setTransferNumber("DP" + new DecimalFormat("00000000").format(transferMapper.getTransferSequenceNo()));
                        orderTransfer.setStatus(OrderTransfer.statusEnum.NOT_ARRIVED);
                    }
            );
            Assert.isTrue(transferMapper.batchInsert(orderTransferList) == orderTransferList.size());
            return ResponseBuilder.list(orderTransferList);
        } catch (Exception e) {
            log.error(className + "batchCreate", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<OrderTransfer> batchArrive(String siteCode, List<String> orderNos, List<String> arriveRemarkList, LocalDateTime arriveTime) {
        try {
            Assert.notEmpty(orderNos);
            Assert.notEmpty(arriveRemarkList);
            Assert.notNull(siteCode);
            Assert.isTrue(orderNos.size() == arriveRemarkList.size());
            List<OrderTransfer> orderTransfers = transferMapper.findBySiteCodeAndOrderNos(siteCode, orderNos);
            List<String> exceptionOrderNos = new ArrayList<>();
            List<OrderTransfer> updateOrderTransfers = new ArrayList<>();
            for (int i = 0; i < orderNos.size(); i++) {
                String orderNo = orderNos.get(i);
                Optional<OrderTransfer> optional = orderTransfers.stream().filter(orderTransfer -> orderTransfer.getOrderNo().equals(orderNo)).findFirst();
                Assert.isTrue(optional.isPresent());
                OrderTransfer orderTransfer = optional.get();
                if (!orderTransfer.getStatus().equals(OrderTransfer.statusEnum.NOT_ARRIVED)) {
                    exceptionOrderNos.add(orderTransfer.getOrderNo());
                }
                orderTransfer.setStatus(OrderTransfer.statusEnum.ARRIVED);
                orderTransfer.setArriveTime(arriveTime);
                orderTransfer.setArriveRemark(arriveRemarkList.get(i));
                updateOrderTransfers.add(orderTransfer);
            }
            if (exceptionOrderNos.size() > 0) {
                return ResponseBuilder.listFail("订单" + String.join(",", exceptionOrderNos) + "已确认到货！");
            }
            transferMapper.batchUpdate(updateOrderTransfers);
            return ResponseBuilder.list(updateOrderTransfers);
        } catch (Exception e) {
            log.error(className + "batchArrive", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public Response removeToArrived(List<String> orderNos, String siteCode) {
        try {
            Assert.notEmpty(orderNos);
            Assert.notNull(siteCode);
            transferMapper.batchUpdateStatus(OrderTransfer.statusEnum.ARRIVED, siteCode, orderNos);
            return ResponseBuilder.success();
        } catch (Exception e) {
            log.error(className + "removeToArrived", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<OrderTransfer> toDispatched(String siteCode, List<String> orderNos) {
        try {
            Assert.notNull(siteCode);
            Assert.notEmpty(orderNos);
            List<OrderTransfer> orderTransfers = transferMapper.findBySiteCodeAndOrderNos(siteCode, orderNos);
            /*if (orderTransfers != null && orderTransfers.size() > 0) {
                List<String> exceptionOrderNos = orderTransfers.stream().filter(orderTransfer ->
                        !OrderTransfer.statusEnum.ARRIVED.equals(orderTransfer.getStatus())
                ).map(OrderTransfer::getOrderNo).collect(Collectors.toList());
                if (exceptionOrderNos != null && exceptionOrderNos.size() > 0) {
                    return ResponseBuilder.listFail("订单" + String.join(",", exceptionOrderNos) + "状态异常，请选择已到达且未派车未发运的订单！");
                }
            }*/
            transferMapper.batchUpdateStatus(OrderTransfer.statusEnum.DISPATCHED, siteCode, orderNos);
            return ResponseBuilder.list(orderTransfers);
        } catch (Exception e) {
            log.error(className + "toDispatched", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<OrderTransfer> toConsigned(String siteCode, List<String> orderNos) {
        try {
            Assert.notNull(siteCode);
            Assert.notEmpty(orderNos);
            List<OrderTransfer> orderTransfers = transferMapper.findBySiteCodeAndOrderNos(siteCode, orderNos);
            /*if (orderTransfers != null && orderTransfers.size() > 0) {
                List<String> exceptionOrderNos = orderTransfers.stream().filter(orderTransfer ->
                        !OrderTransfer.statusEnum.ARRIVED.equals(orderTransfer.getStatus())
                                && !OrderTransfer.statusEnum.DISPATCHED.equals(orderTransfer.getStatus())
                ).map(OrderTransfer::getOrderNo).collect(Collectors.toList());
                if (exceptionOrderNos != null && exceptionOrderNos.size() > 0) {
                    return ResponseBuilder.listFail("订单" + String.join(",", exceptionOrderNos) + "状态异常，请选择已到达且未发运的订单！");
                }
            }*/
            transferMapper.batchUpdateStatus(OrderTransfer.statusEnum.CONSIGNED, siteCode, orderNos);
            return ResponseBuilder.list(orderTransfers);
        } catch (Exception e) {
            log.error(className + "toConsigned", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<OrderTransfer> saveReceiptInfo(TransferReceiptDto transferReceiptDto) {
        try {
            Assert.notNull(transferReceiptDto);
            Assert.notEmpty(transferReceiptDto.getOrderNos());
            Assert.notNull(transferReceiptDto.getSiteCode());
            List<OrderTransfer> orderTransferList =
                    transferMapper.findBySiteCodeAndOrderNos(transferReceiptDto.getSiteCode(), transferReceiptDto.getOrderNos());
            if (orderTransferList != null && orderTransferList.size() > 0) {
                orderTransferList.stream().forEach(
                        orderTransfer -> {
                            orderTransfer.setReceiptPostDate(transferReceiptDto.getReceiptPostTime());
                            orderTransfer.setReceiptInfo(transferReceiptDto.getReceiptInfo());
                        }
                );
                transferMapper.batchUpdate(orderTransferList);
            }
            return ResponseBuilder.list(orderTransferList);
        } catch (Exception e) {
            log.error(className + "saveReceiptInfo", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public PageResponse<OrderTransferListDto> pageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            if (null == page) {
                page = 1;
            }
            if (null == pageSize) {
                pageSize = 20;
            }
            PageHelper.startPage(page, pageSize);
            List<OrderTransferListDto> list = transferMapper.pageList(params);
            PageInfo pageInfo = new PageInfo<>(list);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(className + "pageList", e);
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public PageResponse<TransferReportListDto> reportPageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            if (null == page) {
                page = 1;
            }
            if (null == pageSize) {
                pageSize = 20;
            }
            PageHelper.startPage(page, pageSize);
            List<TransferReportListDto> list = transferMapper.reportPageList(params);
            PageInfo pageInfo = new PageInfo<>(list);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(className + "reportPageList", e);
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public Response<OrderTransferListDto> findTransferByOrderNo(String orderNo, String siteCode) {
        try {
            if (!StringUtils.hasText(orderNo)) {
                return ResponseBuilder.fail("单号不能为空！");
            }
            if (!StringUtils.hasText(siteCode)) {
                return ResponseBuilder.fail("站点不能为空！");
            }
            Response<OrderTransfer> orderTransferResponse = getByOrderNoAndTransferSite(orderNo, siteCode);
            if (orderTransferResponse.isSuccess() && orderTransferResponse.getBody() != null) {
                OrderTransferListDto listDto = BeanHelper.convert(orderTransferResponse.getBody(), OrderTransferListDto.class);
                Response<LogisticsOrderAllDto> response = logisticsOrderService.findDtoByOrderNo(orderNo);
                if (response.isSuccess() && response.getBody() != null) {
                    LogisticsOrderAllDto logisticsOrderAllDto = response.getBody();
                    listDto.setDeliveryAddress(logisticsOrderAllDto.getDeliveryAddress());
                    listDto.setDeliveryCompany(logisticsOrderAllDto.getDeliveryCompany());
                    listDto.setDeliveryContacts(logisticsOrderAllDto.getDeliveryContacts());
                    listDto.setDeliveryContactPhone(logisticsOrderAllDto.getDeliveryContactPhone());
                    listDto.setCustomerOrderNo(logisticsOrderAllDto.getCustomerOrderNo());
                    Response<Customer> customerResponse = customerService.getByCode(logisticsOrderAllDto.getCustomerCode());
                    if (customerResponse.isSuccess() && customerResponse.getBody() != null) {
                        listDto.setCustomerName(customerResponse.getBody().getName());
                    }
                }
                return ResponseBuilder.success(listDto);
            }
            return ResponseBuilder.fail("订单不存在！");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }


    @Override
    public Response<OrderTransfer> getByOrderNoAndTransferSite(String orderNo, String siteCode) {
        try {
            Assert.notNull(orderNo);
            Assert.notNull(siteCode);
            return ResponseBuilder.success(transferMapper.getByOrderNoAndTransferSite(orderNo, siteCode));
        } catch (Exception e) {
            log.error(className + "getByOrderNoAndTransferSite", e);
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<OrderTransfer> findByOrderNosAndSiteCode(List<String> orderNos, String siteCode) {
        try {
            Assert.notEmpty(orderNos);
            Assert.notNull(siteCode);
            return ResponseBuilder.list(transferMapper.findBySiteCodeAndOrderNos(siteCode, orderNos));
        } catch (Exception e) {
            log.error(className + "findByOrderNosAndSiteCode", e);
            return ResponseBuilder.listFail(e);
        }
    }


}

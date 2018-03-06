package com.lnet.tms.service.receipt;


import com.lnet.framework.core.*;
import com.lnet.framework.util.BeanHelper;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.ums.carrier.carrierDto.CarrierListDto;
import com.lnet.model.ums.customer.customerEntity.Customer;
import com.lnet.model.ums.organization.Organization;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.oms.order.orderDto.LogisticsOrderReceiptDto;
import com.lnet.model.tms.order.orderEntity.LnetOrderReceipt;
import com.lnet.model.tms.order.orderDto.LnetOrderReceiptDto;
import com.lnet.model.tms.order.orderEntity.OrderReceipt;
import com.lnet.model.tms.order.orderDto.OrderReceiptDto;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import com.lnet.tms.contract.spi.ReceiptService;
import com.lnet.tms.contract.spi.consgin.ConsignOrderService;
import com.lnet.tms.mapper.LnetOrderReceiptMapper;
import com.lnet.tms.mapper.OrderReceiptMapper;
import com.lnet.ums.contract.api.CarrierService;
import com.lnet.ums.contract.api.CustomerService;
import com.lnet.ums.contract.api.OrganizationService;
//import com.lnet.model.ums.carrier.carrierDto.CarrierListDto;
//import com.lnet.model.ums.customer.customerEntity.Customer;
//import com.lnet.model.ums.organization.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {
    @Resource
    LogisticsOrderService logisticsOrderService;
    @Resource
    OrganizationService organizationService;
    @Resource
    CustomerService customerService;
    @Autowired
    ConsignOrderService consignOrderService;
    @Resource
    CarrierService carrierService;
    @Autowired
    private LnetOrderReceiptMapper lnetOrderReceiptMapper;
    @Autowired
    private OrderReceiptMapper orderReceiptMapper;

    @Override
    public Response<String> uploadOrderReceipt(OrderReceiptDto orderReceiptDto) {
        try {
            OrderReceipt orderReceipt = BeanHelper.convert(orderReceiptDto, OrderReceipt.class);
            orderReceipt.setReceiptId(Snowflake.getInstance().next());
            orderReceipt.setUploadedTime(LocalDateTime.now());

            return orderReceiptMapper.insert(orderReceipt) > 0 ? ResponseBuilder.success(orderReceipt.getReceiptId(), "上传成功！") : ResponseBuilder.fail("上传失败！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response batchUploadOrderReceipt(List<OrderReceiptDto> orderReceiptDtos) {
        try {
            List<OrderReceipt> orderReceipts = orderReceiptDtos.stream().map(m -> toOrderReceipt(m)).collect(Collectors.toList());

            boolean isSuccess = false;
            if (null != orderReceipts && orderReceipts.size() > 0) {
                //每次批量插入的限制条数
                int batchSize = 100;
                int count = ((orderReceipts.size() - 1) / batchSize) + 1;
                List<OrderReceipt> tem = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                    int limits = i * batchSize < orderReceipts.size() ? batchSize : orderReceipts.size() - (i - 1) * batchSize;
                    tem = orderReceipts.stream().skip((i - 1) * batchSize).limit(limits).collect(Collectors.toList());
                    isSuccess = orderReceiptMapper.batchInsert(tem) == limits;
                    if (!isSuccess)
                        break;
                }
            }

            if (isSuccess)
                return ResponseBuilder.success("", "批量上传成功！");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail("批量上传失败！");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<String> uploadLnetOrderReceipt(LnetOrderReceiptDto lnetOrderReceiptDto) {
        try {
            LnetOrderReceipt lnetOrderReceipt = BeanHelper.convert(lnetOrderReceiptDto, LnetOrderReceipt.class);
            lnetOrderReceipt.setReceiptId(Snowflake.getInstance().next());
            lnetOrderReceipt.setUploadedTime(LocalDateTime.now());

            return lnetOrderReceiptMapper.insert(lnetOrderReceipt) > 0 ? ResponseBuilder.success(lnetOrderReceipt.getReceiptId(), "上传成功！") : ResponseBuilder.fail("上传失败！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response batchUploadLnetOrderReceipt(List<LnetOrderReceiptDto> lnetOrderReceiptDtos) {
        try {
            List<LnetOrderReceipt> lnetOrderReceipts = lnetOrderReceiptDtos.stream().map(e -> toLnetOrderReceipt(e)).collect(Collectors.toList());

            boolean isSuccess = false;
            if (null != lnetOrderReceipts && 0 < lnetOrderReceipts.size()) {
                //每次批量插入的限制条数
                int batchSize = 100;
                int count = ((lnetOrderReceipts.size() - 1) / batchSize) + 1;
                List<LnetOrderReceipt> tem = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                    int limitSize = lnetOrderReceipts.size() > i * batchSize ? batchSize : lnetOrderReceipts.size() - (i - 1) * batchSize;
                    tem = lnetOrderReceipts.stream().skip((i - 1) * batchSize).limit(limitSize).collect(Collectors.toList());
                    isSuccess = lnetOrderReceiptMapper.batchInsert(tem) == limitSize;
                    if (!isSuccess)
                        break;
                }
            }

            if (isSuccess)
                return ResponseBuilder.success("", "批量上传成功！");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail("批量上传失败！");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<LnetOrderReceiptDto> getLnetOrderReceipt(String lnetOrderNo) {
        try {
            Assert.hasText(lnetOrderNo);

            LnetOrderReceipt lnetOrderReceipt = lnetOrderReceiptMapper.getByLnetOrderNo(lnetOrderNo);
            Assert.notNull(lnetOrderReceipt, "尚未上传回单！");

            return ResponseBuilder.success(toLnetOrderReceiptDto(lnetOrderReceipt));
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<List<OrderReceiptDto>> getOrderReceipt(String customerCode, String orderNo) {
        try {
            List<OrderReceiptDto> dtos = new ArrayList<>();
            Assert.hasText(customerCode);
            Assert.hasText(orderNo);

            List<OrderReceipt> orderReceipts = orderReceiptMapper.getByCustomerCodeAndOrderNo(customerCode, orderNo);
            Assert.notEmpty(orderReceipts, "尚未上传回单！");

            if (null != orderReceipts && 0 < orderReceipts.size()) {
                orderReceipts.forEach(e -> {
                    dtos.add(toOrderReceiptDto(e));
                });
            }

            return ResponseBuilder.success(dtos);
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public ListResponse<OrderReceiptDto> getOrderReceipt(List<String> customerOrderNos) {
        final String paramsError = "参数为空！";
        final String emptyData = "未查询到数据！";
        try {
            if ((null != customerOrderNos) && (0 < customerOrderNos.size())) {
                List<OrderReceipt> orderReceipts = orderReceiptMapper.findByCustomerOrderNos(customerOrderNos);

                if ((null != orderReceipts) && (0 < orderReceipts.size())) {
                    //转换为dto
                    List<OrderReceiptDto> dtos = new ArrayList<>();
                    orderReceipts.forEach(e -> {
                        dtos.add(toOrderReceiptDto(e));
                    });
                    return ResponseBuilder.list(dtos);

                } else {
                    return ResponseBuilder.list(null, true, emptyData);
                }

            } else {
                return ResponseBuilder.listFail(paramsError);
            }
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public Response receiptScan(List<String> orderNos, String customerCode) {
        List<String> success = new ArrayList<>();//符合条件的订单
        List<String> notBelong = new ArrayList<>();//不属于此客户的订单
        List<String> statusError = new ArrayList<>();//不符合状态的订单
        String message = "";//结果信息

        List<LogisticsOrder> logisticsOrders = logisticsOrderService.findByNos(orderNos).getBody();

        if (logisticsOrders != null) {
            //过滤不属于此客户的订单
            notBelong = logisticsOrders.stream().filter(f -> !f.getCustomerCode().equals(customerCode)).map(m -> m.getCustomerOrderNo()).collect(Collectors.toList());

            logisticsOrders.stream().filter(f -> f.getCustomerCode().equals(customerCode)).forEach(e -> {
                // TODO: 2016/10/13 回单扫描时订单的状态判断

                if (e.getStatus().equals(LogisticsOrder.Status.SIGNED))
                    success.add(e.getOrderNo());
                else
                    statusError.add(e.getCustomerOrderNo());

                success.add(e.getOrderNo());
            });
        }

        if (!success.isEmpty()) {
            message += "扫描成功：" + String.join(",", success) + "\n";
            Response response = logisticsOrderService.receiptScan(success);
            if (!response.isSuccess())
                return response;
        }
        if (!notBelong.isEmpty())
            message += "不属于此客户：" + String.join(",", notBelong) + "\n";
        if (!statusError.isEmpty())
            message += "不为签收状态：" + String.join(",", statusError) + "\n";

        return ResponseBuilder.success("", message);
    }

    private OrderReceipt toOrderReceipt(OrderReceiptDto orderReceiptDto) {
        OrderReceipt orderReceipt = OrderReceipt.builder()
                .receiptId(Snowflake.getInstance().next())
                .orderNo(orderReceiptDto.getOrderNo())
                .customerCode(orderReceiptDto.getCustomerCode())
                .uploadedTime(LocalDateTime.now())
                .uploadedUserId(orderReceiptDto.getUploadedUserId())
                .contentType(orderReceiptDto.getContentType())
                .fileName(orderReceiptDto.getFileName())
                .filePath(orderReceiptDto.getFilePath())
                .thumbPath(orderReceiptDto.getThumbPath())
                .build();

        return orderReceipt;
    }

    private LnetOrderReceipt toLnetOrderReceipt(LnetOrderReceiptDto lnetOrderReceiptDto) {
        LnetOrderReceipt lnetOrderReceipt = LnetOrderReceipt.builder()
                .receiptId(Snowflake.getInstance().next())
                .lnetOrderNo(lnetOrderReceiptDto.getLnetOrderNo())
                .uploadedTime(LocalDateTime.now())
                .uploadedUserId(lnetOrderReceiptDto.getUploadedUserId())
                .contentType(lnetOrderReceiptDto.getContentType())
                .fileName(lnetOrderReceiptDto.getFileName())
                .filePath(lnetOrderReceiptDto.getFilePath())
                .thumbPath(lnetOrderReceiptDto.getThumbPath())
                .build();

        return lnetOrderReceipt;
    }

    private OrderReceiptDto toOrderReceiptDto(OrderReceipt orderReceipt) {
        OrderReceiptDto orderReceiptDto = OrderReceiptDto.builder()
                .orderNo(orderReceipt.getOrderNo())
                .customerCode(orderReceipt.getCustomerCode())
                .uploadedUserId(orderReceipt.getUploadedUserId())
                .uploadedTime(orderReceipt.getUploadedTime())
                .contentType(orderReceipt.getContentType())
                .fileName(orderReceipt.getFileName())
                .filePath(orderReceipt.getFilePath())
                .thumbPath(orderReceipt.getThumbPath())
                .build();

        return orderReceiptDto;
    }

    private LnetOrderReceiptDto toLnetOrderReceiptDto(LnetOrderReceipt lnetOrderReceipt) {
        LnetOrderReceiptDto lnetOrderReceiptDto = LnetOrderReceiptDto.builder()
                .lnetOrderNo(lnetOrderReceipt.getLnetOrderNo())
                .uploadedUserId(lnetOrderReceipt.getUploadedUserId())
                .uploadedTime(lnetOrderReceipt.getUploadedTime())
                .contentType(lnetOrderReceipt.getContentType())
                .fileName(lnetOrderReceipt.getFileName())
                .filePath(lnetOrderReceipt.getFilePath())
                .thumbPath(lnetOrderReceipt.getThumbPath())
                .build();

        return lnetOrderReceiptDto;
    }

    @Override
    public PageResponse<LogisticsOrderReceiptDto> searchForReceipt(KendoGridRequest request) {
        PageResponse<LogisticsOrderReceiptDto> pageResponse = logisticsOrderService.receiptQueryPageList(request.getPage(), request.getPageSize(), request.getParams());
        List<LogisticsOrderReceiptDto> dtos = pageResponse.getBody();
        List<String> customerOrderNos = dtos.stream().map(LogisticsOrderReceiptDto::getCustomerOrderNo).collect(Collectors.toList());

        List<Organization> branches = organizationService.getAllBranches("LNET").getBody();
        List<Customer> customers = customerService.getAvailable().getBody();
        List<CarrierListDto> carrierListDto = carrierService.getAll().getBody();

        List<OrderReceiptDto> receiptDtos = this.getOrderReceipt(customerOrderNos).getBody();
        //填充属性
        dtos.forEach(e -> {

            //填充branchName属性
            {
                Optional<Organization> organizationOptional = branches.stream().filter(f -> f.getCode().equals(e.getBranchCode())).findFirst();
                if (organizationOptional.isPresent()) {
                    e.setBranchName(organizationOptional.get().getName());
                }
            }

            //填充customerName
            {
                if (null != customers) {
                    Optional<Customer> optional = customers.stream().filter(f -> f.getCode().equals(e.getCustomerCode())).findFirst();
                    if (optional.isPresent()) {
                        e.setCustomerName(optional.get().getName());
                    }
                }
            }

            //填充carrierCode,carrierName,carrierNo属性
            {
                List<ConsignOrder> consignOrders = consignOrderService.findByOrderNo(e.getOrderNo()).getBody();// TODO: 2016/11/17 此处数据访问可优化

                if (consignOrders != null && 0 < consignOrders.size()) {
                    List<String> consignOrderNo = consignOrders.stream().map(m -> m.getConsignOrderNo()).collect(Collectors.toList());

                    Optional<CarrierListDto> optional = carrierListDto.stream().filter(f -> f.getCode().equals(consignOrders.get(0).getCarrierCode())).findFirst();
                    if (optional.isPresent())
                        e.setCarrierName(optional.get().getName());

                    e.setCarrierNo(String.join(",", consignOrderNo));
                    e.setCarrierCode(consignOrders.get(0).getCarrierCode());
                }
            }

            //填充订单月份
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                if (null != e.getOrderDate()) {
                    e.setOrderMonth(e.getOrderDate().format(formatter));
                }
            }

            //填充图片路径filePath，小图片路径thumbPath
            {
                Optional<OrderReceiptDto> optional = receiptDtos.stream().filter(f -> (f.getCustomerCode().equals(e.getCustomerCode())) && (f.getOrderNo().equals(e.getCustomerOrderNo()))).findFirst();
                if (optional.isPresent()) {
                    e.setFilePath("http://filex.lnetco.com/" + optional.get().getFilePath());
                    e.setThumbPath("http://filex.lnetco.com/" + optional.get().getThumbPath());
                }
            }

        });

        return pageResponse;
    }
}

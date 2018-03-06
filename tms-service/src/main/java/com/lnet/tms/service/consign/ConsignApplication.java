/*
package com.lnet.tms.service.consign;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.BeanHelper;
import com.lnet.microservices.address.domain.District;
import com.lnet.microservices.address.spi.DistrictService;
import com.lnet.microservices.carrier.contract.dto.CarrierListDto;
import com.lnet.microservices.carrier.contract.service.CarrierService;
import com.lnet.microservices.consign.contract.*;
import com.lnet.microservices.customer.domain.Customer;
import com.lnet.microservices.customer.domain.Project;
import com.lnet.microservices.customer.spi.CustomerService;
import com.lnet.microservices.dispatch.api.DispatchService;
import com.lnet.microservices.dispatch.contract.Dispatch;
import com.lnet.microservices.dispatch.contract.DispatchItem;
import com.lnet.microservices.infrastructure.domain.site.Site;
import com.lnet.microservices.infrastructure.domain.user.User;
import com.lnet.microservices.infrastructure.spi.ExpenseAccountService;
import com.lnet.microservices.infrastructure.spi.SiteService;
import com.lnet.microservices.infrastructure.spi.UserService;
import com.lnet.microservices.orm.domain.ConsignQueryDto;
import com.lnet.microservices.orm.domain.LogisticsOrder;
import com.lnet.microservices.orm.domain.LogisticsOrderAllDto;
import com.lnet.microservices.orm.spi.LogisticsOrderService;
import com.lnet.microservices.pack.api.PackageService;
import com.lnet.microservices.pack.contract.OrderPackageRecordDto;
import com.lnet.microservices.payable.api.PayableService;
import com.lnet.microservices.payable.contract.Payable;
import com.lnet.microservices.payable.contract.PayableAccount;
import com.lnet.microservices.progresstrace.domain.DeliveryOrder.DeliveryOrderTraceCreateModel;
import com.lnet.microservices.transfer.api.TransferService;
import com.lnet.microservices.transfer.contract.OrderTransfer;
import com.lnet.tms.application.common.ObjectQuery;
import com.lnet.tms.application.common.ReflectUtils;
import com.lnet.tms.application.progressTrace.ProgressTraceApplication;
import com.lnet.tms.application.progressTrace.ProgressTraceTerm;
import com.lnet.tms.application.quotation.PriceCalc;
import com.lnet.tms.contract.spi.consgin.ConsignOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConsignApplication {

    private static final String CONSIGNED_TRANSFER_ORDER_NO_LIST = "consignedTransferOrderNos";
    private static final String CONSIGNED_ORDER_NO_LIST = "consignedOrderNos";
    @Resource
    ConsignOrderService consignOrderService;
    @Resource
    LogisticsOrderService logisticsOrderService;
    @Resource
    CustomerService customerService;
    @Resource
    UserService userService;
    @Resource
    CarrierService carrierService;
    @Resource
    DistrictService districtService;
    @Resource
    PackageService packageService;
    @Resource
    TransferService transferService;
    @Resource
    SiteService siteService;
    @Resource
    PayableService payableService;
    @Resource
    DispatchService dispatchService;
    @Resource
    ExpenseAccountService expenseAccountService;
    @Resource
    PriceCalc priceCalc;
    @Autowired
    private ProgressTraceApplication progressTraceApplication;

    public PageResponse<ConsignOrderPageDto> pageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            PageResponse<ConsignOrderPageDto> response = consignOrderService.pageList(page, pageSize, params);
            if (!response.isSuccess()) {
                return ResponseBuilder.pageFail(response.getMessage());
            }
            List<ConsignOrderPageDto> pageData = response.getBody();
            if (pageData != null && pageData.size() > 0) {
                //所有承运商
                Response<List<CarrierListDto>> listCarrierResponse = carrierService.getAll();
                if (!listCarrierResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(listCarrierResponse.getMessage());
                }
                List<CarrierListDto> carrierListDtos = listCarrierResponse.getBody();
                //用户
                ListResponse<User> userListResponse = userService.getByOrgCode((String) params.get("branchCode"));
                if (!userListResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(userListResponse.getMessage());
                }
                List<User> userList = userListResponse.getBody();
                for (ConsignOrderPageDto dto : pageData) {
                    CarrierListDto carrier = ObjectQuery.findOne(carrierListDtos, "code", dto.getCarrierCode());
                    if (carrier != null) {
                        dto.setCarrierName(carrier.getName());
                    }
                    if (!StringUtils.isEmpty(dto.getDestCityCode())) {
                        List<District> districts = districtService.getSuperior(dto.getDestCityCode()).getBody();
                        if (districts != null) {
                            List<String> names = districts.stream().map(District::getName).collect(Collectors.toList());
                            dto.setDestCityName(String.join("", names));
                        }
                    }
                    User user = ObjectQuery.findOne(userList, "userId", dto.getCreatedBy());
                    if (user != null) {
                        dto.setCreateUserName(user.getFullName());
                    }
                }
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    public PageResponse<ConsignPayableDto> searchConsignPayable(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            PageResponse response = consignOrderService.pageList(page, pageSize, params);
            if (!response.isSuccess()) {
                return ResponseBuilder.pageFail(response.getMessage());
            }
            response.setBody(convertDto((List) response.getBody(), ""));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    public PageResponse<ConsignQueryDto> selectOrder(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            PageResponse<ConsignQueryDto> response = logisticsOrderService.consignQueryPageList(page, pageSize, params);
            if (!response.isSuccess()) {
                return ResponseBuilder.pageFail(response.getMessage());
            }
            List<ConsignQueryDto> list = response.getBody();
            if (list != null && list.size() > 0) {
                //客户
                Response<List<Customer>> customerListResponse = customerService.getAll();
                if (!customerListResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(customerListResponse.getMessage());
                }
                //打包信息
                List<String> orderNos = list.stream().map(dto -> dto.getOrderNo()).collect(Collectors.toList());
                ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
                if (!orderPackageRecordDtoListResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(orderPackageRecordDtoListResponse.getMessage());
                }
                List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
                //已开单箱数
                ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty((String) params.get("siteCode"), orderNos);
                if (!consignPackageQtyListResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(consignPackageQtyListResponse.getMessage());
                }
                List<OrderConsignPackageQty> consignPackageQtoList = consignPackageQtyListResponse.getBody();

                List<Customer> customers = customerListResponse.getBody();
                for (ConsignQueryDto dto : list) {
                    //填充客户名称
                    Customer customer = ObjectQuery.findOne(customers, "code", dto.getCustomerCode());
                    if (customer != null) {
                        dto.setCustomerName(customer.getName());
                    }
                    //填充打包数据
                    if (orderPackageRecordDtos != null && orderPackageRecordDtoListResponse.size() > 0 && !dto.isTransfer()) {
                        OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", dto.getOrderNo());
                        if (orderPackageRecordDto != null) {
                            dto.setTotalPackageQty(orderPackageRecordDto.getPackageQty());
                            dto.setTotalWeight(orderPackageRecordDto.getWeight());
                            dto.setTotalVolume(orderPackageRecordDto.getVolume());
                        }
                    }
                    //如订单有发运，则填充已发运箱数
                    if (consignPackageQtoList != null && consignPackageQtoList.size() > 0) {
                        OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(consignPackageQtoList, "orderNo", dto.getOrderNo());
                        if (orderConsignPackageQty != null) {
                            dto.setConsignPackageQty(orderConsignPackageQty.getConsigningPackageQty() == null ? 0 : orderConsignPackageQty.getConsigningPackageQty());
                        }
                    }
                    dto.setConsignPackageQty(dto.getConsignPackageQty() == null ? 0 : dto.getConsignPackageQty());
                }
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    private List<ConsignPayableDto> convertDto(List list, String branchCode) {
        List<ConsignPayableDto> result = BeanHelper.convertList(list, ConsignPayableDto.class);
        if (result != null && result.size() > 0) {
            //所有承运商
            Response<List<CarrierListDto>> listCarrierResponse = carrierService.getAll();
            if (!listCarrierResponse.isSuccess()) {
                return null;
            }
            List<CarrierListDto> carrierListDtos = listCarrierResponse.getBody();
            List<String> nos = result.stream().map(ConsignPayableDto::getConsignOrderNo).collect(Collectors.toList());
            //用户
            ListResponse<User> userListResponse = userService.getByOrgCode(branchCode);
            if (!userListResponse.isSuccess()) {
                return null;
            }
            List<User> userList = userListResponse.getBody();
            List<Payable> payableDtoList = payableService.findBySourceNos(nos, Payable.OwnerType.CARRIER.name()).getBody();
            for (ConsignPayableDto dto : result) {
                CarrierListDto carrier = ObjectQuery.findOne(carrierListDtos, "code", dto.getCarrierCode());
                if (carrier != null) {
                    dto.setCarrierName(carrier.getName());
                }
                if (!StringUtils.isEmpty(dto.getDestCityCode())) {
                    List<District> districts = districtService.getSuperior(dto.getDestCityCode()).getBody();
                    if (districts != null) {
                        List<String> names = districts.stream().map(District::getName).collect(Collectors.toList());
                        dto.setDestCityName(String.join("", names));
                    }
                }
                User user = ObjectQuery.findOne(userList, "userId", dto.getCreatedBy());
                if (user != null) {
                    dto.setCreateUserName(user.getFullName());
                }
                Payable payableDto = ObjectQuery.findOne(payableDtoList, "sourceNo", dto.getConsignOrderNo());
                if (payableDto != null) {
                    dto.setTotalPayable(payableDto.getTotalAmount());
                    dto.setTransport(getPayable(payableDto.getAccounts(), "120101"));
                    dto.setSend(getPayable(payableDto.getAccounts(), "120102"));
                    dto.setUpstairs(getPayable(payableDto.getAccounts(), "120103"));
                    dto.setPickup(getPayable(payableDto.getAccounts(), "120104"));
                    dto.setOther(getPayable(payableDto.getAccounts(), "120105"));
                    dto.setInspection(getPayable(payableDto.getAccounts(), "120106"));
                    dto.setUnloading(getPayable(payableDto.getAccounts(), "120107"));
                    dto.setReceipt(getPayable(payableDto.getAccounts(), "120108"));
                }
            }
        }
        return result;
    }

    private BigDecimal getPayable(List<PayableAccount> accounts, String code) {
        if (accounts != null) {
            for (PayableAccount account : accounts) {
                if (code.equals(account.getAccountCode())) {
                    return account.getAmount();
                }
            }
        }
        return new BigDecimal(0);
    }

    private Response createOrAddJudge(List<ConsignOrderItem> items, String siteCode) {
        try {
            if (null == items || 0 == items.size()) {
                return ResponseBuilder.fail("发运明细不能为空");
            }
            //region 判断订单状态是否为已审核、派车中、已提货
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            ListResponse<LogisticsOrder> listResponse = logisticsOrderService.findByNos(orderNos);
            if (!listResponse.isSuccess()) {
                return listResponse;
            }
            List<LogisticsOrder> orders = listResponse.getBody();
            ListResponse<OrderTransfer> orderTransferListResponse = transferService.findByOrderNosAndSiteCode(orderNos, siteCode);
            if (!orderTransferListResponse.isSuccess()) {
                return orderTransferListResponse;
            }
            List<OrderTransfer> orderTransfers = orderTransferListResponse.getBody();

            List<String> exceptionOrderNos = new ArrayList<>();
            List<String> currentSiteOrderNos = new ArrayList<>();
            List<String> transferSiteOrderNos = new ArrayList<>();

            for (ConsignOrderItem item : items) {
                LogisticsOrder order = ObjectQuery.findOne(orders, "orderNo", item.getOrderNo());
                if (order.getSiteCode().equals(siteCode)) {
                    if ((!order.getStatus().equals(LogisticsOrder.Status.ACCEPTED)
                            && !order.getStatus().equals(LogisticsOrder.Status.DISPATCHING)
                            && !order.getStatus().equals(LogisticsOrder.Status.COLLECTED))
                            || !LogisticsOrder.dispatchTypeEnum.OUT_CITY.equals(order.getDispatchType())) {
                        exceptionOrderNos.add(item.getOrderNo());
                    }
                    currentSiteOrderNos.add(item.getOrderNo());
                } else {
                    OrderTransfer orderTransfer = ObjectQuery.findOne(orderTransfers, "orderNo", item.getOrderNo());
                    if ((!OrderTransfer.statusEnum.ARRIVED.equals(orderTransfer.getStatus()) && !OrderTransfer.statusEnum.DISPATCHED.equals(orderTransfer.getStatus()))
                            || !LogisticsOrder.dispatchTypeEnum.OUT_CITY.name().equals(orderTransfer.getDispatchType())) {
                        exceptionOrderNos.add(item.getOrderNo());
                    }
                    transferSiteOrderNos.add(item.getOrderNo());
                }
            }
            if (exceptionOrderNos.size() > 0) {
                return ResponseBuilder.fail("订单" + String.join(",", exceptionOrderNos) + "状态异常，请选择已审单、已派车或已提货的本站点市外订单或调配到货的市外订单！");
            }
            //endregion
            //region 判断所有箱是否已开托运单
            //打包信息
            ListResponse<OrderConsignPackageQty> orderConsignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty(siteCode, orderNos);
            if (!orderConsignPackageQtyListResponse.isSuccess()) {
                return orderConsignPackageQtyListResponse;
            }
            List<OrderConsignPackageQty> orderConsignPackageQties = orderConsignPackageQtyListResponse.getBody();

            List<String> consignPackageExceptionOrderNos = new ArrayList<>();
            List<String> toConsigningOrderNos = new ArrayList<>();
            List<String> toConsignedTransferOrderNos = new ArrayList<>();

            if (currentSiteOrderNos.size() > 0) {
                ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(currentSiteOrderNos);
                if (!orderPackageRecordDtoListResponse.isSuccess()) {
                    return orderPackageRecordDtoListResponse;
                }
                List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
                currentSiteOrderNos.stream().forEach(
                        orderNo -> {
                            ConsignOrderItem item = ObjectQuery.findOne(items, "orderNo", orderNo);
                            OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", orderNo);
                            Integer totalPackageQty = orderPackageRecordDto.getPackageQty() == null ? 0 : orderPackageRecordDto.getPackageQty();
                            OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(orderConsignPackageQties, "orderNo", orderNo);
                            Integer consigningPackageQty = 0;
                            if (orderConsignPackageQty != null && orderConsignPackageQty.getConsigningPackageQty() != null) {
                                consigningPackageQty = orderConsignPackageQty.getConsigningPackageQty();
                            }
                            if ((totalPackageQty - consigningPackageQty) < item.getPackageQuantity()) {
                                consignPackageExceptionOrderNos.add(orderNo);
                            } else if ((totalPackageQty - consigningPackageQty) == item.getPackageQuantity()) {
                                toConsigningOrderNos.add(orderNo);
                            }
                        }
                );
            }
            if (transferSiteOrderNos.size() > 0) {
                transferSiteOrderNos.stream().forEach(orderNo -> {
                            ConsignOrderItem item = ObjectQuery.findOne(items, "orderNo", orderNo);
                            OrderTransfer orderTransfer = ObjectQuery.findOne(orderTransfers, "orderNo", orderNo);
                            Integer totalPackageQty = orderTransfer.getPackageQuantity() == null ? 0 : orderTransfer.getPackageQuantity();
                            OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(orderConsignPackageQties, "orderNo", orderNo);
                            Integer consigningPackageQty = 0;
                            if (orderConsignPackageQty != null && orderConsignPackageQty.getConsigningPackageQty() != null) {
                                consigningPackageQty = orderConsignPackageQty.getConsigningPackageQty();
                            }
                            if ((totalPackageQty - consigningPackageQty) < item.getPackageQuantity()) {
                                consignPackageExceptionOrderNos.add(orderNo);
                            } else if ((totalPackageQty - consigningPackageQty) == item.getPackageQuantity()) {
                                toConsignedTransferOrderNos.add(orderNo);
                            }
                        }
                );
            }


            if (consignPackageExceptionOrderNos.size() > 0) {
                return ResponseBuilder.fail("订单" + String.join(",", consignPackageExceptionOrderNos) + "的发运箱数不能大于总箱数和已开单箱数之差！");
            }
            Map<String, Object> map = new HashedMap();
            map.put("toConsigningOrderNos", toConsigningOrderNos);
            map.put("toConsignedTransferOrderNos", toConsignedTransferOrderNos);
            return ResponseBuilder.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response create(ConsignOrder consignOrder) {
        try {
            if (null == consignOrder) {
                return ResponseBuilder.fail("托运单不能为空");
            }
            if (!StringUtils.isEmpty(consignOrder.getTransferSiteCode()) && consignOrder.getSiteCode().equals(consignOrder.getTransferSiteCode())) {
                return ResponseBuilder.fail("中转站点不能为当前操作站点！");
            }

            List<ConsignOrderItem> items = consignOrder.getItems();
            Response response = this.createOrAddJudge(items, consignOrder.getSiteCode());
            if (!response.isSuccess()) {
                return response;
            }
            //endregion
            Response<ConsignOrder> createResponse = consignOrderService.create(consignOrder);
            //region创建成功后修改订单状态
            if (!createResponse.isSuccess()) {
                return createResponse;
            }

            Map<String, Object> map = (Map<String, Object>) response.getBody();
            List<String> toConsigningOrderNos = (List<String>) map.get("toConsigningOrderNos");
            List<String> toConsignedTransferOrderNos = (List<String>) map.get("toConsignedTransferOrderNos");
            if (toConsigningOrderNos.size() > 0) {
                ListResponse<LogisticsOrder> toConsigningResponse = logisticsOrderService.toConsigning(toConsigningOrderNos, consignOrder.getCreatedBy());
                if (!toConsigningResponse.isSuccess()) {
                    return ResponseBuilder.fail(toConsigningResponse.getMessage());
                }
            }
            if (toConsignedTransferOrderNos.size() > 0) {
                Response toConsignedResponse = transferService.toConsigned(consignOrder.getSiteCode(), toConsignedTransferOrderNos);
                if (!toConsignedResponse.isSuccess()) {
                    return toConsignedResponse;
                }
            }
            //endregion
            return createResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    private List<ConsignOrderItemDto> consignOrderItemConvert(List<ConsignOrderItem> items, String siteCode) {
        try {
            List<ConsignOrderItemDto> itemDtos = new ArrayList<>();
            if (items != null && items.size() > 0) {
                List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
                //订单信息
                ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
                if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                    return null;
                }
                List<LogisticsOrderAllDto> logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();
                //打包信息
                ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
                if (!orderPackageRecordDtoListResponse.isSuccess()) {
                    return null;
                }
                List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
                //已开单箱数
                ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty(siteCode, orderNos);
                if (!consignPackageQtyListResponse.isSuccess()) {
                    return null;
                }
                List<OrderConsignPackageQty> consignPackageQtoList = consignPackageQtyListResponse.getBody();
                //客户
                Response<List<Customer>> customerListResponse = customerService.getAll();
                if (!customerListResponse.isSuccess()) {
                    return null;
                }
                List<Customer> customers = customerListResponse.getBody();
                //中转订单
                ListResponse<OrderTransfer> orderTransferListResponse = transferService.findByOrderNosAndSiteCode(orderNos, siteCode);
                if (!orderPackageRecordDtoListResponse.isSuccess()) {
                    return null;
                }
                List<OrderTransfer> orderTransfers = orderTransferListResponse.getBody();

                for (ConsignOrderItem item : items) {
                    ConsignOrderItemDto consignOrderItemDto = (ConsignOrderItemDto) ReflectUtils.convert(item, ConsignOrderItemDto.class);
                    LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", item.getOrderNo());
                    Integer totalPackageQty = 0;
                    BigDecimal totalVolume = new BigDecimal(0);
                    BigDecimal totalWeight = new BigDecimal(0);
                    Integer consigningPackageQty = 0;
                    if (logisticsOrderAllDto != null) {
                        String customerCode = logisticsOrderAllDto.getCustomerCode();
                        if (!StringUtils.isEmpty(customerCode)) {
                            Customer customer = ObjectQuery.findOne(customers, "code", customerCode);
                            if (customer != null) {
                                consignOrderItemDto.setCustomerName(customer.getName());
                            }
                        }
                        consignOrderItemDto.setDestCityName(logisticsOrderAllDto.getDeliveryCity());
                        consignOrderItemDto.setCustomerOrderNo(logisticsOrderAllDto.getCustomerOrderNo());
                    }

                    //如有打包则查打包箱数，否则查询订单总箱数
                    if (logisticsOrderAllDto != null && logisticsOrderAllDto.getSiteCode().equals(siteCode)) {
                        OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", item.getOrderNo());
                        if (orderPackageRecordDto != null) {
                            totalPackageQty = orderPackageRecordDto.getPackageQty();
                            totalVolume = orderPackageRecordDto.getVolume();
                            totalWeight = orderPackageRecordDto.getWeight();
                        }
                    } else {
                        OrderTransfer orderTransfer = ObjectQuery.findOne(orderTransfers, "orderNo", item.getOrderNo());
                        if (orderTransfer != null) {
                            totalPackageQty = orderTransfer.getPackageQuantity();
                            totalVolume = orderTransfer.getVolume();
                            totalWeight = orderTransfer.getWeight();
                        }
                    }
                    OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(consignPackageQtoList, "orderNo", item.getOrderNo());
                    if (orderConsignPackageQty != null && orderConsignPackageQty.getConsigningPackageQty() != null) {
                        consigningPackageQty = orderConsignPackageQty.getConsigningPackageQty();
                    }
                    consignOrderItemDto.setTotalPackageQty(totalPackageQty);
                    consignOrderItemDto.setTotalWeight(totalWeight);
                    consignOrderItemDto.setTotalVolume(totalVolume);
                    consignOrderItemDto.setConsignPackageQty(consigningPackageQty);
                    itemDtos.add(consignOrderItemDto);
                }
            }
            return itemDtos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ListResponse<ConsignOrderItemDto> findItemDtoListByOrderNos(List<String> orderNos, String siteCode) {
        try {
            List<ConsignOrderItemDto> itemDtos = new ArrayList<>();
            if (orderNos != null && orderNos.size() > 0) {
                //打包信息
                ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
                if (!orderPackageRecordDtoListResponse.isSuccess()) {
                    return ResponseBuilder.listFail(orderPackageRecordDtoListResponse.getMessage());
                }
                List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
                if (orderPackageRecordDtos != null && orderPackageRecordDtos.size() > 0) {
                    orderPackageRecordDtos.stream().forEach(
                            orderPackageRecordDto -> {
                                if (!orderNos.contains(orderPackageRecordDto.getOrderNo())) {
                                    orderNos.add(orderPackageRecordDto.getOrderNo());
                                }
                            }
                    );
                }
                //订单信息
                ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
                if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                    return ResponseBuilder.listFail(logisticsOrderAllDtoListResponse.getMessage());
                }
                List<LogisticsOrderAllDto> logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();
                //已开单箱数
                ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty(siteCode, orderNos);
                if (!consignPackageQtyListResponse.isSuccess()) {
                    return ResponseBuilder.listFail(consignPackageQtyListResponse.getMessage());
                }
                List<OrderConsignPackageQty> consignPackageQtoList = consignPackageQtyListResponse.getBody();
                //客户
                Response<List<Customer>> customerListResponse = customerService.getAll();
                if (!customerListResponse.isSuccess()) {
                    return ResponseBuilder.listFail(customerListResponse.getMessage());
                }
                List<Customer> customers = customerListResponse.getBody();
                //中转订单
                ListResponse<OrderTransfer> orderTransferListResponse = transferService.findByOrderNosAndSiteCode(orderNos, siteCode);
                if (!orderTransferListResponse.isSuccess()) {
                    return ResponseBuilder.listFail(orderTransferListResponse.getMessage());
                }
                List<OrderTransfer> orderTransfers = orderTransferListResponse.getBody();
                //派车单明细，获取预派车承运商
                ListResponse<DispatchItem> dispatchItemListResponse = dispatchService.findItemsByOrderNos(siteCode, orderNos);
                if (!dispatchItemListResponse.isSuccess()) {
                    return ResponseBuilder.listFail(dispatchItemListResponse.getMessage());
                }
                List<DispatchItem> dispatchItems = dispatchItemListResponse.getBody();

                for (String orderNo : orderNos) {
                    ConsignOrderItemDto dto = new ConsignOrderItemDto();
                    Integer totalPackageQty = 0;
                    BigDecimal totalVolume = new BigDecimal(0);
                    BigDecimal totalWeight = new BigDecimal(0);
                    Integer consigningPackageQty = 0;

                    Optional<LogisticsOrderAllDto> logisticsOrderAllDtoOptional = logisticsOrderAllDtos.stream().filter(
                            logisticsOrderAllDto ->
                                    orderNo.equals(logisticsOrderAllDto.getOrderNo())
                                            && siteCode.equals(logisticsOrderAllDto.getSiteCode())
                                            && (LogisticsOrder.Status.ACCEPTED.equals(logisticsOrderAllDto.getStatus()) ||
                                            LogisticsOrder.Status.DISPATCHING.equals(logisticsOrderAllDto.getStatus()) ||
                                            LogisticsOrder.Status.COLLECTED.equals(logisticsOrderAllDto.getStatus()))
                                            && LogisticsOrder.dispatchTypeEnum.OUT_CITY.equals(logisticsOrderAllDto.getDispatchType())
                    ).findFirst();
                    //本站点订单
                    if (logisticsOrderAllDtoOptional.isPresent() && logisticsOrderAllDtoOptional.get() != null) {
                        LogisticsOrderAllDto logisticsOrderAllDto = logisticsOrderAllDtoOptional.get();
                        dto.setOrderNo(orderNo);
                        String customerCode = logisticsOrderAllDto.getCustomerCode();
                        if (!StringUtils.isEmpty(customerCode)) {
                            Customer customer = ObjectQuery.findOne(customers, "code", customerCode);
                            if (customer != null) {
                                dto.setCustomerName(customer.getName());
                            }
                        }
                        dto.setDestCityName(logisticsOrderAllDto.getDeliveryProvince() + logisticsOrderAllDto.getDeliveryCity() +
                                StringUtils.defaultString(logisticsOrderAllDto.getDeliveryDistrict())
                                + StringUtils.defaultString(logisticsOrderAllDto.getDeliveryStreet()));
                        dto.setCustomerOrderNo(logisticsOrderAllDto.getCustomerOrderNo());
                        dto.setOrginCode(logisticsOrderAllDto.getOrginCode());
                        dto.setDestinationCode(logisticsOrderAllDto.getDestinationCode());
                        dto.setDeliveryContacts(logisticsOrderAllDto.getDeliveryContacts());
                        dto.setDeliveryContactPhone(logisticsOrderAllDto.getDeliveryContactPhone());
                        dto.setDeliveryAddress(logisticsOrderAllDto.getDeliveryAddress());
                        //填充打包数据
                        OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", orderNo);
                        if (orderPackageRecordDto != null) {
                            totalPackageQty = orderPackageRecordDto.getPackageQty();
                            totalVolume = orderPackageRecordDto.getVolume();
                            totalWeight = orderPackageRecordDto.getWeight();
                        }
                    } else {
                        //中转订单
                        Optional<OrderTransfer> orderTransferOptional = orderTransfers.stream().filter(
                                orderTransfer -> orderNo.equals(orderTransfer.getOrderNo())
                                        && LogisticsOrder.dispatchTypeEnum.OUT_CITY.name().equals(orderTransfer.getDispatchType())
                                        && siteCode.equals(orderTransfer.getTransferSiteCode())
                                        && (OrderTransfer.statusEnum.ARRIVED.equals(orderTransfer.getStatus()) || OrderTransfer.statusEnum.DISPATCHED.equals(orderTransfer.getStatus()))
                        ).findFirst();
                        if (orderTransferOptional.isPresent() && orderTransferOptional.get() != null) {
                            OrderTransfer orderTransfer = orderTransferOptional.get();
                            LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", orderNo);
                            dto.setOrderNo(orderNo);
                            String customerCode = logisticsOrderAllDto.getCustomerCode();
                            if (!StringUtils.isEmpty(customerCode)) {
                                Customer customer = ObjectQuery.findOne(customers, "code", customerCode);
                                if (customer != null) {
                                    dto.setCustomerName(customer.getName());
                                }
                            }
                            dto.setDestCityName(logisticsOrderAllDto.getDeliveryProvince() + logisticsOrderAllDto.getDeliveryCity() +
                                    StringUtils.defaultString(logisticsOrderAllDto.getDeliveryDistrict())
                                    + StringUtils.defaultString(logisticsOrderAllDto.getDeliveryStreet()));
                            dto.setCustomerOrderNo(logisticsOrderAllDto.getCustomerOrderNo());
                            dto.setOrginCode(logisticsOrderAllDto.getOrginCode());
                            dto.setDestinationCode(logisticsOrderAllDto.getDestinationCode());
                            dto.setDeliveryContacts(logisticsOrderAllDto.getDeliveryContacts());
                            dto.setDeliveryContactPhone(logisticsOrderAllDto.getDeliveryContactPhone());
                            dto.setDeliveryAddress(logisticsOrderAllDto.getDeliveryAddress());
                            //填充打包数据
                            totalPackageQty = orderTransfer.getPackageQuantity();
                            totalVolume = orderTransfer.getVolume();
                            totalWeight = orderTransfer.getWeight();
                        }
                    }
                    if (!StringUtils.isEmpty(dto.getOrderNo())) {
                        OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(consignPackageQtoList, "orderNo", orderNo);
                        if (orderConsignPackageQty != null && orderConsignPackageQty.getConsigningPackageQty() != null) {
                            consigningPackageQty = orderConsignPackageQty.getConsigningPackageQty();
                        }
                        DispatchItem dispatchItem = ObjectQuery.findOne(dispatchItems, "orderNo", orderNo);
                        if (dispatchItem != null) {
                            dto.setCarrierCode(dispatchItem.getCarrierCode());
                        }
                        dto.setTotalPackageQty(totalPackageQty);
                        dto.setTotalWeight(totalWeight);
                        dto.setTotalVolume(totalVolume);
                        dto.setConsignPackageQty(consigningPackageQty);
                        dto.setPackageQuantity((dto.getTotalPackageQty() == null ? 0 : dto.getTotalPackageQty()) - (dto.getConsignPackageQty() == null ? 0 : dto.getConsignPackageQty()));
                        dto.setVolume(dto.getTotalVolume());
                        dto.setWeight(dto.getTotalWeight());
                        dto.setReceiptPageNumber(1);
                        itemDtos.add(dto);
                    }
                }
            }
            return ResponseBuilder.list(itemDtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    public Response<Map<String, Object>> get(String consignOrderId) {
        try {
            Response<ConsignOrder> consignOrderResponse = consignOrderService.get(consignOrderId);
            if (!consignOrderResponse.isSuccess()) {
                return ResponseBuilder.fail(consignOrderResponse.getMessage());
            }
            ConsignOrder consignOrder = consignOrderResponse.getBody();
            Map<String, Object> map = new HashedMap();
            map.put("consignOrder", consignOrder);
            List<ConsignOrderItem> items = consignOrder.getItems();
            if (items != null && items.size() > 0) {
                map.put("items", this.consignOrderItemConvert(items, consignOrder.getSiteCode()));
            }
            return ResponseBuilder.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    */
/**
     * 托运单发运
     *
     * @param consignOrder 托运单对象
     * @param operatorId   当前操作人ID
     * @return
     *//*

    public Response<ConsignOrder> consign(ConsignOrder consignOrder, String operatorId) {
        try {
            if (null == consignOrder) {
                return ResponseBuilder.fail("托运单不能为空");
            }
            if (null == consignOrder.getItems() || 0 == consignOrder.getItems().size()) {
                return ResponseBuilder.fail("托运单明细不能为空");
            }

            String siteCode = consignOrder.getSiteCode();
            if (!StringUtils.isEmpty(consignOrder.getTransferSiteCode()) && siteCode.equals(consignOrder.getTransferSiteCode())) {
                return ResponseBuilder.fail("中转站点不能为当前操作站点！");
            }

            Response<ConsignOrder> response = new Response<>();
            ConsignOrderOperationTime operationTime = consignOrder.getOperationTime();
            //托运单可在创建时直接发运，此时调用新增方法，反之调用更新方法
            if (StringUtils.isNotEmpty(consignOrder.getConsignOrderId())) {
                response = this.update(consignOrder, operatorId);
            } else {
                consignOrder.setCreatedBy(operatorId);
                response = this.create(consignOrder);
            }
            if (!response.isSuccess()) {
                return response;
            }
            consignOrder = response.getBody();
            consignOrder.setOperationTime(operationTime);
            //托运单发运
            Response<ConsignOrder> consignOrderResponse = consignOrderService.consign(consignOrder, operatorId);
            if (!consignOrderResponse.isSuccess()) {
                return consignOrderResponse;
            }
            // region 修改订单状态(当前公司订单状态修改到已发运，中转订单修改到中转已发运)
            List<ConsignOrderItem> items = consignOrder.getItems();
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            //订单信息
            ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
            if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                return ResponseBuilder.fail(logisticsOrderAllDtoListResponse.getMessage());
            }
            List<LogisticsOrderAllDto> logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();
            //打包信息
            ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
            if (!orderPackageRecordDtoListResponse.isSuccess()) {
                return ResponseBuilder.fail(orderPackageRecordDtoListResponse.getMessage());
            }
            List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
            //已发运箱数
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty(consignOrder.getSiteCode(), orderNos);
            if (!consignPackageQtyListResponse.isSuccess()) {
                return ResponseBuilder.fail(consignPackageQtyListResponse.getMessage());
            }
            List<OrderConsignPackageQty> consignPackageQtoList = consignPackageQtyListResponse.getBody();
            //中转订单
            ListResponse<OrderTransfer> orderTransferListResponse = transferService.findByOrderNosAndSiteCode(orderNos, consignOrder.getSiteCode());
            if (!orderTransferListResponse.isSuccess()) {
                return ResponseBuilder.fail(orderTransferListResponse.getMessage());
            }
            List<OrderTransfer> orderTransfers = orderTransferListResponse.getBody();
            String currentSiteCode = consignOrder.getSiteCode();
            Map<String, List<String>> map = this.consignModifyOrderStatus(items, currentSiteCode, logisticsOrderAllDtos, orderPackageRecordDtos, orderTransfers, consignPackageQtoList);
            List<String> consignedOrderNos = map.get(CONSIGNED_ORDER_NO_LIST);
            List<String> consignedTransferOrderNos = map.get(CONSIGNED_TRANSFER_ORDER_NO_LIST);
            if (consignedOrderNos != null && consignedOrderNos.size() > 0) {
                Response updateStatusResponse = logisticsOrderService.toConsigned(consignedOrderNos, operatorId);
                if (!updateStatusResponse.isSuccess()) {
                    return updateStatusResponse;
                }
            }
            if (consignedTransferOrderNos != null && consignedTransferOrderNos.size() > 0) {
                Response toTransferConsignedResponse = logisticsOrderService.toTransferConsigned(consignedTransferOrderNos, operatorId);//不建议使用数字命名
                if (!toTransferConsignedResponse.isSuccess()) {
                    return toTransferConsignedResponse;
                }
            }
            //endregion
            //region 如果有中转，则生成中转数据
            ListResponse<OrderTransfer> buildTransferResponse = this.consignTransfer(consignOrder, logisticsOrderAllDtos, operatorId);
            if (!buildTransferResponse.isSuccess()) {
                return ResponseBuilder.fail(buildTransferResponse.getMessage());
            }
            List<OrderTransfer> orderTransferList = buildTransferResponse.getBody();
            if (orderTransferList != null && orderTransferList.size() > 0) {
                ListResponse<OrderTransfer> transferCreateResponse = transferService.batchCreate(orderTransferList);
                if (!transferCreateResponse.isSuccess()) {
                    return ResponseBuilder.fail(transferCreateResponse.getMessage());
                }
            }
            //endregion

            //region  计算并保存应付
            Response<Payable> payableResponse = priceCalc.calculatePayable(consignOrder);
            if (!payableResponse.isSuccess()) {
                return ResponseBuilder.fail(payableResponse.getMessage());
            }
            Response<Payable> payableCreateResponse = payableService.create(payableResponse.getBody());
            if (!payableCreateResponse.isSuccess()) {
                return ResponseBuilder.fail(payableCreateResponse.getMessage());
            }
            //endregion
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    */
/**
     * 发运时如有中转，则生成中转数据
     *
     * @param consignOrder          托运单
     * @param logisticsOrderAllDtos 托运单明细对应的订单信息列表
     * @param operatorId            操作人ID
     * @return 中转信息列表
     *//*

    private ListResponse<OrderTransfer> consignTransfer(ConsignOrder consignOrder, List<LogisticsOrderAllDto> logisticsOrderAllDtos, String operatorId) {
        List<OrderTransfer> orderTransferList = new ArrayList<>();
        String transferOrganizationCode = consignOrder.getTransferOrganizationCode();
        String transferSiteCode = consignOrder.getTransferSiteCode();
        if ((!StringUtils.isEmpty(transferOrganizationCode)) && (!StringUtils.isEmpty(transferSiteCode))) {
            Response<Site> siteResponse = siteService.getByCode(consignOrder.getTransferSiteCode());
            if (!siteResponse.isSuccess()) {
                return ResponseBuilder.listFail(siteResponse.getMessage());
            }
            Site site = siteResponse.getBody();
            if (null == site) {
                return ResponseBuilder.listFail("站点不存在");
            }
            consignOrder.getItems().stream().forEach(item -> {
                LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", item.getOrderNo());
                LogisticsOrder.dispatchTypeEnum dispatchType = logisticsOrderService.getDispatchType(logisticsOrderAllDto.getDestinationCode(), site.getDistrictCode());

                OrderTransfer orderTransfer = OrderTransfer.builder()
                        .orderNo(item.getOrderNo())
                        .volume(item.getVolume())
                        .weight(item.getWeight())
                        .packageQuantity(item.getPackageQuantity())
                        .receiptPageNumber(item.getReceiptPageNumber())
                        .transferSiteCode(transferSiteCode)
                        .transferOrganizationCode(transferOrganizationCode)
                        .carrierCode(consignOrder.getCarrierCode())
                        .consignOrderNo(consignOrder.getConsignOrderNo())
                        .createBy(operatorId)
                        .dispatchType(dispatchType.name())
                        .build();
                orderTransferList.add(orderTransfer);
            });
        }
        return ResponseBuilder.list(orderTransferList);
    }

    public Response<ConsignOrder> update(ConsignOrder consignOrder, String operatorId) {
        try {
            if (null == consignOrder) {
                return ResponseBuilder.fail("托运单不能为空");
            }
            if (null == consignOrder.getItems() || 0 == consignOrder.getItems().size()) {
                return ResponseBuilder.fail("托运单明细不能为空");
            }
            if (StringUtils.isEmpty(consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单ID不能为空");
            }

            if (!StringUtils.isEmpty(consignOrder.getTransferSiteCode()) && consignOrder.getSiteCode().equals(consignOrder.getTransferSiteCode())) {
                return ResponseBuilder.fail("中转站点不能为当前操作站点！");
            }
            //region 对照原有托运单明细，筛选出已去除的订单和新加的订单，判断新加的订单是否满足发运条件
            Response<ConsignOrder> consignOrderResponse = consignOrderService.get(consignOrder.getConsignOrderId());
            if (!consignOrderResponse.isSuccess()) {
                return consignOrderResponse;
            }
            ConsignOrder oldConsignOrder = consignOrderResponse.getBody();
            if (null == oldConsignOrder) {
                return ResponseBuilder.fail("托运单不存在");
            }
            if (!ConsignOrder.consignStatus.NEW.equals(oldConsignOrder.getStatus())) {
                return ResponseBuilder.fail("托运单不能进行修改操作");
            }
            List<ConsignOrderItem> oldItems = oldConsignOrder.getItems();
            List<ConsignOrderItem> newItems = consignOrder.getItems();
            List<String> newItemOrderNos = newItems.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<String> oldItemOrderNos = oldItems.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<String> addOrderNos = newItems.stream().filter(newItem -> !oldItemOrderNos.contains(newItem.getOrderNo()))
                    .map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<String> removeOrderNos = oldItems.stream().filter(oldItem ->
                    !newItemOrderNos.contains(oldItem.getOrderNo())).map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());

            List<ConsignOrderItem> addItems = newItems.stream().filter(
                    newItem -> addOrderNos.contains(newItem.getOrderNo())
            ).collect(Collectors.toList());
            List<ConsignOrderItem> updateNewItems = newItems.stream().filter(
                    newItem -> !addOrderNos.contains(newItem.getOrderNo())
            ).collect(Collectors.toList());
            List<ConsignOrderItem> removeItems = oldItems.stream().filter(
                    oldItem -> removeOrderNos.contains(oldItem.getOrderNo())
            ).collect(Collectors.toList());
            List<ConsignOrderItem> updateOldItems = oldItems.stream().filter(
                    oldItem -> !removeOrderNos.contains(oldItem.getOrderNo())
            ).collect(Collectors.toList());

            List<String> updateAndRemoveOrderNos = new ArrayList<>();
            updateAndRemoveOrderNos.addAll(removeOrderNos);
            updateAndRemoveOrderNos.addAll(newItemOrderNos);

            List<LogisticsOrderAllDto> logisticsOrderAllDtos = new ArrayList<>();
            if (updateAndRemoveOrderNos.size() > 0) {
                ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(updateAndRemoveOrderNos);
                if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                    return ResponseBuilder.fail(logisticsOrderAllDtoListResponse.getMessage());
                }
                logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();
            }

            List<String> toConsigningOrderNos = new ArrayList<>();
            List<String> toConsignedTransferOrderNos = new ArrayList<>();
            List<String> toArrivedTransferOrderNos = new ArrayList<>();
            List<String> toAcceptedOrderNos = new ArrayList<>();

            if (addItems != null && addItems.size() > 0) {
                Response createOrAddJudgeResponse = this.createOrAddJudge(addItems, consignOrder.getSiteCode());
                if (!createOrAddJudgeResponse.isSuccess()) {
                    return createOrAddJudgeResponse;
                }
                Map<String, Object> map = (Map<String, Object>) createOrAddJudgeResponse.getBody();
                toConsigningOrderNos = (List<String>) map.get("toConsigningOrderNos");
                toConsignedTransferOrderNos = (List<String>) map.get("toConsignedTransferOrderNos");
            }

            List<String> consignPackageExceptionOrderNos = new ArrayList<>();
            if (updateOldItems != null && updateOldItems.size() > 0) {
                List<ConsignOrderItemDto> itemDtos = this.consignOrderItemConvert(updateOldItems, consignOrder.getSiteCode());
                for (ConsignOrderItemDto itemDto : itemDtos) {
                    Optional<ConsignOrderItem> updateNewItemOptional = updateNewItems.stream().filter(newItem -> itemDto.getOrderNo().equals(newItem.getOrderNo())).findFirst();
                    if (!updateNewItemOptional.isPresent()) {
                        return ResponseBuilder.fail("查询订单明细出错");
                    }
                    ConsignOrderItem updateNewItem = updateNewItemOptional.get();
                    LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", itemDto.getOrderNo());

                    if ((itemDto.getTotalPackageQty() - (itemDto.getConsignPackageQty() - itemDto.getPackageQuantity())) < updateNewItem.getPackageQuantity()) {
                        consignPackageExceptionOrderNos.add(itemDto.getOrderNo());
                    } else if ((itemDto.getTotalPackageQty() - (itemDto.getConsignPackageQty() - itemDto.getPackageQuantity())) == updateNewItem.getPackageQuantity()) {
                        if (logisticsOrderAllDto.getSiteCode().equals(consignOrder.getSiteCode())) {
                            toConsigningOrderNos.add(itemDto.getOrderNo());
                        } else {
                            toConsignedTransferOrderNos.add(itemDto.getOrderNo());
                        }
                    } else {
                        if (logisticsOrderAllDto.getSiteCode().equals(consignOrder.getSiteCode())) {
                            toAcceptedOrderNos.add(itemDto.getOrderNo());
                        } else {
                            toArrivedTransferOrderNos.add(itemDto.getOrderNo());
                        }
                    }
                }
            }
            if (removeItems != null && removeItems.size() > 0) {
                for (ConsignOrderItem item : removeItems) {
                    LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", item.getOrderNo());
                    if (logisticsOrderAllDto.getSiteCode().equals(consignOrder.getSiteCode())) {
                        toAcceptedOrderNos.add(item.getOrderNo());
                    } else {
                        toArrivedTransferOrderNos.add(item.getOrderNo());
                    }
                }
            }

            if (consignPackageExceptionOrderNos.size() > 0) {
                return ResponseBuilder.fail("订单" + String.join(",", consignPackageExceptionOrderNos) + "的发运箱数不能大于总箱数和已开单箱数之差！");
            }

            consignOrder.setModifiedBy(operatorId);
            ConsignOrderModifyDto modifyDto = ConsignOrderModifyDto.builder()
                    .consignOrder(consignOrder)
                    .addItems(addItems)
                    .removeItems(removeItems)
                    .updateNewItems(updateNewItems)
                    .updateOldItems(updateOldItems)
                    .operatorId(operatorId)
                    .build();
            //endregion
            //region 托运单修改
            Response<ConsignOrder> updateResponse = consignOrderService.update(modifyDto);
            if (!updateResponse.isSuccess()) {
                return updateResponse;
            }
            //endregion
            //region 修改订单状态
            if (toConsigningOrderNos.size() > 0) {
                ListResponse<LogisticsOrder> toConsigningResponse = logisticsOrderService.toConsigning(toConsigningOrderNos, operatorId);
                if (!toConsigningResponse.isSuccess()) {
                    return ResponseBuilder.fail(toConsigningResponse.getMessage());
                }
            }
            if (toAcceptedOrderNos.size() > 0) {
                ListResponse<LogisticsOrder> toAcceptedResponse = logisticsOrderService.toAccepted(toAcceptedOrderNos, operatorId);
                if (!toAcceptedResponse.isSuccess()) {
                    return ResponseBuilder.fail(toAcceptedResponse.getMessage());
                }
            }
            if (toArrivedTransferOrderNos.size() > 0) {
                Response removeToArrivedResponse = transferService.removeToArrived(toArrivedTransferOrderNos, consignOrder.getSiteCode());
                if (!removeToArrivedResponse.isSuccess()) {
                    return removeToArrivedResponse;
                }
            }
            if (toConsignedTransferOrderNos.size() > 0) {
                Response toConsignedResponse = transferService.toConsigned(consignOrder.getSiteCode(), toConsignedTransferOrderNos);
                if (!toConsignedResponse.isSuccess()) {
                    return toConsignedResponse;
                }
            }
            return updateResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response startUp(List<String> consignOrderIds, LocalDateTime startUpTime, String operatorId, String siteCode, String operatorName) {
        try {
            //托运单启运
            ListResponse<ConsignOrder> startUpResponse = consignOrderService.batchStartUp(consignOrderIds, startUpTime, operatorId);
            if (!startUpResponse.isSuccess()) {
                return ResponseBuilder.fail(startUpResponse.getMessage());
            }

            //添加启运的跟踪信息
            List<ConsignOrder> consignOrders = startUpResponse.getBody();
            List<DeliveryOrderTraceCreateModel> traceCreateModels = consignOrders.stream().map(m -> {
                DeliveryOrderTraceCreateModel createModel = DeliveryOrderTraceCreateModel.builder()
                        .shipper(m.getCarrierCode())
                        .deliveryNo(m.getConsignOrderNo())
                        .operator(operatorName)
                        .operateTime(LocalDateTime.now())
                        .operateAddress("启运地点待补充")
                        .description(ProgressTraceTerm.startup)
                        .build();
                return createModel;
            }).collect(Collectors.toList());
            progressTraceApplication.batchAddDeliveryOrderTrace(traceCreateModels);

            // region 修改订单状态到已启运
            ListResponse<ConsignOrderItem> itemListResponse = consignOrderService.findItemsByIds(consignOrderIds);
            if (!itemListResponse.isSuccess()) {
                return ResponseBuilder.fail(itemListResponse.getMessage());
            }
            List<ConsignOrderItem> items = itemListResponse.getBody();
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            //订单信息
            ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
            if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                return ResponseBuilder.fail(logisticsOrderAllDtoListResponse.getMessage());
            }
            List<LogisticsOrderAllDto> logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();
            //打包信息
            ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
            if (!orderPackageRecordDtoListResponse.isSuccess()) {
                return ResponseBuilder.fail(orderPackageRecordDtoListResponse.getMessage());
            }
            List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
            //已发运箱数
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty(siteCode, orderNos);
            if (!consignPackageQtyListResponse.isSuccess()) {
                return ResponseBuilder.fail(consignPackageQtyListResponse.getMessage());
            }
            List<OrderConsignPackageQty> consignPackageQtoList = consignPackageQtyListResponse.getBody();

            List<String> startUpOrderNos = new ArrayList<>();
            for (ConsignOrderItem item : items) {
                LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", item.getOrderNo());
                if (logisticsOrderAllDto.getSiteCode().equals(siteCode)) {
                    Integer totalPackageQty = 0;
                    Integer startUpPackageQty = 0;
                    //如有打包则查打包箱数，否则查询订单总箱数
                    OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", item.getOrderNo());
                    if (orderPackageRecordDto != null) {
                        totalPackageQty = orderPackageRecordDto.getPackageQty();
                    }
                    OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(consignPackageQtoList, "orderNo", item.getOrderNo());
                    if (orderConsignPackageQty != null && orderConsignPackageQty.getStartupPackageQty() != null) {
                        startUpPackageQty = orderConsignPackageQty.getStartupPackageQty();
                    }
                    if (totalPackageQty.equals(startUpPackageQty)) {
                        startUpOrderNos.add(item.getOrderNo());
                    }
                }
            }

            if (startUpOrderNos.size() > 0) {
                Response response = logisticsOrderService.toSetOut(startUpOrderNos, operatorId);
                if (!response.isSuccess()) {
                    return response;
                }
            }
            //endregion
            return ResponseBuilder.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response arrive(List<String> consignOrderIds, LocalDateTime arriveTime, String operatorId, String siteCode) {
        try {
            ListResponse<ConsignOrder> arriveResponse = consignOrderService.batchArrived(consignOrderIds, arriveTime, operatorId);
            if (!arriveResponse.isSuccess()) {
                return ResponseBuilder.fail(arriveResponse.getMessage());
            }
            // region 修改订单状态到达
            ListResponse<ConsignOrderItem> itemListResponse = consignOrderService.findItemsByIds(consignOrderIds);
            if (!itemListResponse.isSuccess()) {
                return ResponseBuilder.fail(itemListResponse.getMessage());
            }
            List<ConsignOrderItem> items = itemListResponse.getBody();
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            //订单信息
            ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
            if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                return logisticsOrderAllDtoListResponse;
            }
            List<LogisticsOrderAllDto> logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();
            //打包信息
            ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
            if (!orderPackageRecordDtoListResponse.isSuccess()) {
                return orderPackageRecordDtoListResponse;
            }
            List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
            //已发运箱数
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty(siteCode, orderNos);
            if (!consignPackageQtyListResponse.isSuccess()) {
                return consignPackageQtyListResponse;
            }
            List<OrderConsignPackageQty> consignPackageQtoList = consignPackageQtyListResponse.getBody();

            List<String> arrivedOrderNos = new ArrayList<>();
            for (ConsignOrderItem item : items) {
                LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", item.getOrderNo());
                if (logisticsOrderAllDto.getSiteCode().equals(siteCode)) {
                    Integer totalPackageQty = 0;
                    Integer arrivedPackageQty = 0;
                    if (logisticsOrderAllDto != null) {
                        totalPackageQty = logisticsOrderAllDto.getTotalPackageQty();
                    }
                    //如有打包则查打包箱数，否则查询订单总箱数
                    OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", item.getOrderNo());
                    if (orderPackageRecordDto != null) {
                        totalPackageQty = orderPackageRecordDto.getPackageQty();
                    }
                    OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(consignPackageQtoList, "orderNo", item.getOrderNo());
                    if (orderConsignPackageQty != null && orderConsignPackageQty.getArrivedPackageQty() != null) {
                        arrivedPackageQty = orderConsignPackageQty.getArrivedPackageQty();
                    }
                    if (totalPackageQty.equals(arrivedPackageQty)) {
                        arrivedOrderNos.add(item.getOrderNo());
                    }
                }
            }

            if (arrivedOrderNos.size() > 0) {
                Response response = logisticsOrderService.toArrived(arrivedOrderNos, operatorId);
                if (!response.isSuccess()) {
                    return response;
                }
            }
            //endregion
            return ResponseBuilder.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response finish(List<String> consignOrderIds, LocalDateTime finishTime, String operatorId) {
        try {
            return consignOrderService.batchFinished(consignOrderIds, finishTime, operatorId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response cancel(String consignOrderId, String operatorId, String notes, String siteCode) {
        try {
            Response<ConsignOrder> consignOrderResponse = consignOrderService.get(consignOrderId);
            if (!consignOrderResponse.isSuccess()) {
                return consignOrderResponse;
            }
            Response cancelResponse = consignOrderService.cancel(consignOrderId, operatorId, notes);
            if (!cancelResponse.isSuccess()) {
                return cancelResponse;
            }
            List<ConsignOrderItem> items = consignOrderResponse.getBody().getItems();
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());

            ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
            if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                return logisticsOrderAllDtoListResponse;
            }
            List<LogisticsOrderAllDto> logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();

            List<String> toAcceptedOrderNos = new ArrayList<>();
            List<String> toArrivedTransferOrderNos = new ArrayList<>();
            if (orderNos != null && orderNos.size() > 0) {
                for (String orderNo : orderNos) {
                    LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", orderNo);
                    if (logisticsOrderAllDto.getSiteCode().equals(siteCode)) {
                        toAcceptedOrderNos.add(orderNo);
                    } else {
                        toArrivedTransferOrderNos.add(orderNo);
                    }
                }
            }

            ListResponse<LogisticsOrder> toAcceptedResponse = logisticsOrderService.toAccepted(orderNos, operatorId);
            if (!toAcceptedResponse.isSuccess()) {
                return ResponseBuilder.fail(toAcceptedResponse.getMessage());
            }
            if (toArrivedTransferOrderNos.size() > 0) {
                Response removeToArrivedResponse = transferService.removeToArrived(toArrivedTransferOrderNos, siteCode);
                if (!removeToArrivedResponse.isSuccess()) {
                    return removeToArrivedResponse;
                }
            }

            return ResponseBuilder.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response updateOrderNo(String consignOrderId, String consignOrderNo, String operatorId) {
        try {
            if (StringUtils.isEmpty(consignOrderId)) {
                return ResponseBuilder.fail("托运单ID不能为空");
            }
            if (StringUtils.isEmpty(consignOrderNo)) {
                return ResponseBuilder.fail("托运单号不能为空");
            }
            return consignOrderService.updateOrderNo(consignOrderId, consignOrderNo, operatorId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public ListResponse<ConsignQueryDto> queryConsign(Map<String, Object> params) {
        return logisticsOrderService.consignQuery(params);
    }

    public ListResponse<OrderMonthDto> findOrderByMonth(String createdBy) {
        return consignOrderService.findOrderByMonth(createdBy);
    }

    public Response getByNo(String carrieCode, String consignOrderNo) {
        Response response = consignOrderService.getByNo(carrieCode, consignOrderNo);
        ConsignOrder consignOrder = (ConsignOrder) response.getBody();

        Response<CarrierListDto> carrierListDtoResponse = carrierService.getByCode(consignOrder.getCarrierCode());
        CarrierListDto carrierListDto = carrierListDtoResponse.getBody();
        consignOrder.setCarrierName(carrierListDto.getName());

        return response;
    }

    public ListResponse<ConsignOrderItem> findItemByIds(List<String> consignOrderIds) {
        return consignOrderService.findItemsByIds(consignOrderIds);
    }

    public ListResponse<ConsignOrder> payableModifyQuery(String carrierCode) {
        try {
            if (StringUtils.isEmpty(carrierCode)) {
                return ResponseBuilder.listFail("承运商编码不能为空");
            }
            return consignOrderService.payableModifyQuery(carrierCode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    public Response<ConsignOrder> getById(String consignOrderId) {
        return consignOrderService.get(consignOrderId);
    }

    public Response batchConsign(BatchConsignModel model, boolean isMerge) {
        try {
            if (null == model) {
                return ResponseBuilder.fail("批量发运为空");
            }
            if (null == model.getItems() || 0 == model.getItems().size()) {
                return ResponseBuilder.fail("批量发运明细为空");
            }
            if (StringUtils.isEmpty(model.getCarrierCode())) {
                return ResponseBuilder.fail("承运商编码不能为空");
            }
            List<BatchConsignItemModel> items = model.getItems();

            //region判断能否发运
            List<ConsignOrderItem> consignOrderItemList = new ArrayList<>();
            items.stream().forEach(item -> {
                ConsignOrderItem consignOrderItem = (ConsignOrderItem) ReflectUtils.convert(item, ConsignOrderItem.class);
                consignOrderItemList.add(consignOrderItem);
            });
            Response response = this.createOrAddJudge(consignOrderItemList, model.getSiteCode());
            if (!response.isSuccess()) {
                return response;
            }
            //endregion

            List<String> orderNos = items.stream().map(BatchConsignItemModel::getOrderNo).collect(Collectors.toList());
            ListResponse<LogisticsOrderAllDto> logisticsOrderListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
            if (!logisticsOrderListResponse.isSuccess()) {
                return logisticsOrderListResponse;
            }
            List<LogisticsOrderAllDto> orders = logisticsOrderListResponse.getBody();

            items.stream().forEach(item -> {
                if (!isMerge) {
                    if (StringUtils.isEmpty(item.getConsignOrderNo())) {
                        item.setConsignOrderNo(item.getOrderNo());
                    }
                } else {
                    LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(orders, "orderNo", item.getOrderNo());
                    item.setConsignOrderNo(logisticsOrderAllDto.getCustomerCode() + logisticsOrderAllDto.getOrderDate() + (StringUtils.isEmpty(logisticsOrderAllDto.getDeliveryCompany()) ? logisticsOrderAllDto.getCustomerOrderNo() : logisticsOrderAllDto.getDeliveryCompany()));
                }
            });

            Map<String, List<BatchConsignItemModel>> groupMap =
                    items.stream().collect(Collectors.groupingBy(BatchConsignItemModel::getConsignOrderNo));

            Response<CarrierListDto> carrierListDtoResponse = carrierService.getByCode(model.getCarrierCode());
            if (!carrierListDtoResponse.isSuccess()) {
                return carrierListDtoResponse;
            }
            CarrierListDto carrierListDto = carrierListDtoResponse.getBody();

            List<ConsignOrder> consignOrders = new ArrayList<>();
            for (Map.Entry entry : groupMap.entrySet()) {
                String consignOrderNo = (String) entry.getKey();
                List<BatchConsignItemModel> itemModels = (List<BatchConsignItemModel>) entry.getValue();
                List<ConsignOrderItem> consignOrderItems = new ArrayList<>();
                Integer receiptPageNumber = 0;
                Integer totalPackageQuantity = 0;
                BigDecimal totalVolume = new BigDecimal(0);
                BigDecimal totalWeight = new BigDecimal(0);
                LogisticsOrderAllDto order = null;
                for (int i = 0; i < itemModels.size(); i++) {
                    BatchConsignItemModel itemModel = itemModels.get(i);
                    ConsignOrderItem consignOrderItem = (ConsignOrderItem) ReflectUtils.convert(itemModel, ConsignOrderItem.class);
                    consignOrderItems.add(consignOrderItem);
                    receiptPageNumber = receiptPageNumber + (itemModel.getReceiptPageNumber() == null ? 0 : itemModel.getReceiptPageNumber());
                    totalPackageQuantity = totalPackageQuantity + (itemModel.getPackageQuantity() == null ? 0 : itemModel.getPackageQuantity());
                    totalVolume = totalVolume.add(itemModel.getVolume() == null ? new BigDecimal(0) : itemModel.getVolume());
                    totalWeight = totalWeight.add(itemModel.getWeight() == null ? new BigDecimal(0) : itemModel.getWeight());
                    if (i == 0) {
                        order = ObjectQuery.findOne(orders, "orderNo", itemModel.getOrderNo());
                    }
                }
                ConsignOrder consignOrder = ConsignOrder.builder()
                        .consignOrderNo(consignOrderNo)
                        .carrierCode(model.getCarrierCode())
                        .totalPackageQuantity(totalPackageQuantity)
                        .receiptPageNumber(receiptPageNumber)
                        .totalVolume(totalVolume)
                        .totalWeight(totalWeight)
                        .branchCode(model.getBranchCode())
                        .siteCode(model.getSiteCode())
                        .isTemporaryNo(true)
                        .settlementCycle(carrierListDto.getSettleCycle() == null ? null : carrierListDto.getSettleCycle().name())
                        .paymentType(carrierListDto.getPaymentType() == null ? null : carrierListDto.getPaymentType().name())
                        .calculateType(carrierListDto.getCalculateType() == null ? null : carrierListDto.getCalculateType().name())
                        .transportType(model.getTransportType())
                        .handoverType(Project.HandoverTypeEnum.DOOR_TO_DOOR.name())
                        .startCityCode(order.getOrginCode())
                        .destCityCode(order.getDestinationCode())
                        .consignee(order.getDeliveryContacts())
                        .consigneeAddress(order.getDeliveryAddress())
                        .consigneePhone(order.getDeliveryContactPhone())
                        .whetherHaveUpstairsFee(model.getWhetherHaveUpstairsFee())
                        .createdBy(model.getOperatorId())
                        .items(consignOrderItems)
                        .build();
                consignOrders.add(consignOrder);
            }
            ListResponse<ConsignOrder> batchCreateResponse = consignOrderService.batchCreate(consignOrders);
            if (!batchCreateResponse.isSuccess()) {
                return ResponseBuilder.fail(batchCreateResponse.getMessage());
            }
            consignOrders = batchCreateResponse.getBody
                    ();
            consignOrders.stream().forEach(consignOrder -> {
                ConsignOrderOperationTime operationTime = consignOrder.getOperationTime();
                operationTime.setConsignTime(model.getConsignTime());
                operationTime.setFeedbackConsignTime(model.getFeedbackConsignTime());
            });
            ListResponse<ConsignOrder> batchConsignResponse = consignOrderService.batchConsign(consignOrders, model.getOperatorId());
            if (!batchConsignResponse.isSuccess()) {
                return ResponseBuilder.fail(batchConsignResponse.getMessage());
            }
            //region 修改订单状态
            //打包信息
            ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
            if (!orderPackageRecordDtoListResponse.isSuccess()) {
                return orderPackageRecordDtoListResponse;
            }
            List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
            //已发运箱数
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = consignOrderService.findOrderConsignPackageQty(model.getSiteCode(), orderNos);
            if (!consignPackageQtyListResponse.isSuccess()) {
                return consignPackageQtyListResponse;
            }
            List<OrderConsignPackageQty> consignPackageQtoList = consignPackageQtyListResponse.getBody();
            //中转
            ListResponse<OrderTransfer> orderTransferListResponse = transferService.findByOrderNosAndSiteCode(orderNos, model.getSiteCode());
            if (!orderTransferListResponse.isSuccess()) {
                return orderTransferListResponse;
            }
            List<OrderTransfer> orderTransfers = orderTransferListResponse.getBody();
            String currentSiteCode = model.getSiteCode();
            Map<String, List<String>> map = this.consignModifyOrderStatus(consignOrderItemList, currentSiteCode, orders, orderPackageRecordDtos, orderTransfers, consignPackageQtoList);
            List<String> consignedOrderNos = map.get(CONSIGNED_ORDER_NO_LIST);
            List<String> consignedTransferOrderNos = map.get(CONSIGNED_TRANSFER_ORDER_NO_LIST);
            if (consignedOrderNos.size() > 0) {
                Response updateStatusResponse = logisticsOrderService.toConsigned(consignedOrderNos, model.getOperatorId());
                if (!updateStatusResponse.isSuccess()) {
                    return updateStatusResponse;
                }
            }
            if (consignedTransferOrderNos.size() > 0) {
                Response toTransferConsignedResponse = logisticsOrderService.toTransferConsigned(consignedTransferOrderNos, model.getOperatorId());
                if (!toTransferConsignedResponse.isSuccess()) {
                    return toTransferConsignedResponse;
                }
            }

            //region  保存应付
            Response<List<Payable>> calculatePayable = priceCalc.calculatePayable(consignOrders);
            if (!calculatePayable.isSuccess()) {
                return ResponseBuilder.fail(calculatePayable.getMessage());
            }
            Response<List<Payable>> payableResponses = payableService.batchCreate(calculatePayable.getBody());
            if (!payableResponses.isSuccess()) {
                return payableResponses;
            }
            //endregion
            return ResponseBuilder.success(consignOrders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    private Map<String, List<String>> consignModifyOrderStatus(List<ConsignOrderItem> items, String siteCode,
                                                               List<LogisticsOrderAllDto> orders,
                                                               List<OrderPackageRecordDto> orderPackageRecordDtos,
                                                               List<OrderTransfer> orderTransfers, List<OrderConsignPackageQty> consignPackageQtoList) {
        List<String> consignedOrderNos = new ArrayList<>();
        List<String> consignedTransferOrderNos = new ArrayList<>();
        for (ConsignOrderItem item : items) {
            LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(orders, "orderNo", item.getOrderNo());

            Integer totalPackageQty = 0;
            Integer consignedPackageQty = 0;
            if (siteCode.equals(logisticsOrderAllDto.getSiteCode())) {
                //打包箱数
                OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", item.getOrderNo());
                if (orderPackageRecordDto != null) {
                    totalPackageQty = orderPackageRecordDto.getPackageQty();
                }
                OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(consignPackageQtoList, "orderNo", item.getOrderNo());
                if (orderConsignPackageQty != null && orderConsignPackageQty.getConsignedPackageQty() != null) {
                    consignedPackageQty = orderConsignPackageQty.getConsignedPackageQty();
                }
                if (totalPackageQty.equals(consignedPackageQty)) {
                    consignedOrderNos.add(item.getOrderNo());
                }
            } else {
                Optional<OrderTransfer> optional = orderTransfers.stream().filter(
                        orderTransfer ->
                                orderTransfer.getTransferSiteCode().equals(siteCode)
                                        && item.getOrderNo().equals(orderTransfer.getOrderNo())
                ).findFirst();
                if (optional.isPresent()) {
                    OrderTransfer orderTransfer = optional.get();
                    totalPackageQty = orderTransfer.getPackageQuantity() == null ? 0 : orderTransfer.getPackageQuantity();
                    OrderConsignPackageQty orderConsignPackageQty = ObjectQuery.findOne(consignPackageQtoList, "orderNo", item.getOrderNo());
                    if (orderConsignPackageQty != null && orderConsignPackageQty.getConsignedPackageQty() != null) {
                        consignedPackageQty = orderConsignPackageQty.getConsignedPackageQty();
                    }
                    if (totalPackageQty.equals(consignedPackageQty)) {
                        consignedTransferOrderNos.add(item.getOrderNo());
                    }
                }
            }
        }
        Map<String, List<String>> map = new HashMap();
        map.put(CONSIGNED_ORDER_NO_LIST, consignedOrderNos);
        map.put(CONSIGNED_TRANSFER_ORDER_NO_LIST, consignedTransferOrderNos);
        return map;
    }

    public List<ConsignOrderItemDto> createByDriverQuery(String vehicleId, String branchCode, String siteCode) {
        try {
            if (StringUtils.isEmpty(vehicleId) || StringUtils.isEmpty(branchCode) || StringUtils.isEmpty(siteCode)) {
                return null;
            }
            LocalDate today = LocalDate.now();
            ListResponse<Dispatch> dispatchListResponse = dispatchService.searchDispatch(branchCode, siteCode, null, vehicleId, today.minusDays(3).toString());
            if (!dispatchListResponse.isSuccess()) {
                return null;
            }
            List<Dispatch> dispatches = dispatchListResponse.getBody();
            if (dispatches != null && dispatches.size() > 0) {
                List<String> dispatchIds = dispatches.stream().map(Dispatch::getDispatchId).collect(Collectors.toList());
                ListResponse<DispatchItem> itemListResponse = dispatchService.findItems(dispatchIds);
                if (!itemListResponse.isSuccess()) {
                    return null;
                }
                List<DispatchItem> items = itemListResponse.getBody();
                if (null == items || 0 == items.size()) {
                    return null;
                }
                List<String> orderNos = items.stream().filter(
                        item -> DispatchItem.orderDispatchTypeEnum.CONSIGN.equals(item.getOrderDispatchType())
                                || DispatchItem.orderDispatchTypeEnum.COLLECTINGCONSIGN.equals(item.getOrderDispatchType())
                ).map(DispatchItem::getOrderNo).collect(Collectors.toList());
                ListResponse<ConsignOrderItemDto> listResponse = this.findItemDtoListByOrderNos(orderNos, siteCode);
                if (!listResponse.isSuccess()) {
                    return null;
                }
                return listResponse.getBody();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
*/

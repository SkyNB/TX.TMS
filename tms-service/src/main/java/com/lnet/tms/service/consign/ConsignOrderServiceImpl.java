package com.lnet.tms.service.consign;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lnet.model.base.District;
import com.lnet.base.contract.spi.DistrictService;
import com.lnet.cnaps.contract.api.PayableService;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.BeanHelper;
import com.lnet.framework.util.ObjectQuery;
import com.lnet.framework.util.ReflectUtils;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.cnaps.payEntity.Payable;
import com.lnet.model.cnaps.payEntity.PayableAccount;
import com.lnet.model.tms.consign.consignDto.*;
import com.lnet.model.tms.consign.consignEntity.*;
import com.lnet.model.tms.delivery.DeliveryOrderTraceCreateModel;
import com.lnet.model.tms.dispatch.dispatchEntity.Dispatch;
import com.lnet.model.tms.dispatch.dispatchEntity.DispatchItem;
import com.lnet.model.tms.pack.packDto.OrderPackageRecordDto;
import com.lnet.model.ums.customer.customerEntity.Project;
import com.lnet.model.ums.site.Site;
import com.lnet.model.ums.transprotation.transprotationDto.LogisticsOrderAllDto;
import com.lnet.model.ums.user.User;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.oms.order.orderDto.ConsignQueryDto;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.model.tms.order.orderEntity.OrderTransfer;
import com.lnet.tms.contract.spi.ProgressTraceService;
import com.lnet.tms.contract.spi.TransferService;
import com.lnet.tms.contract.spi.consgin.ConsignOrderService;
import com.lnet.tms.contract.spi.consgin.PriceCalc;
import com.lnet.tms.contract.spi.dispatch.DispatchService;
import com.lnet.tms.contract.spi.pack.PackageService;
import com.lnet.tms.mapper.*;
import com.lnet.tms.service.progressTrace.ProgressTraceTerm;
import com.lnet.ums.contract.api.*;
import com.lnet.model.ums.carrier.carrierDto.CarrierListDto;
import com.lnet.model.ums.customer.customerEntity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class ConsignOrderServiceImpl implements ConsignOrderService {

    private final String className = this.getClass().getSimpleName() + ".";
    private static final String CONSIGNED_TRANSFER_ORDER_NO_LIST = "consignedTransferOrderNos";
    private static final String CONSIGNED_ORDER_NO_LIST = "consignedOrderNos";

    @Autowired
    ConsignOrderMapper consignOrderMapper;
    @Autowired
    ConsignOrderOperationTimeMapper operationTimeMapper;
    @Autowired
    ConsignOrderLogMapper logMapper;
    @Autowired
    ConsignOrderItemMapper itemMapper;
    @Autowired
    OrderConsignPackageQtyMapper consignPackageQtyMapper;

    @Autowired
    ProgressTraceService progressTraceService;
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
    @Autowired
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

    @Override
    public ListResponse<ConsignOrder> findByIds(List<String> consignOrderIds) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderMapper.findByIds(consignOrderIds);
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "findByIds", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    private ListResponse<ConsignOrderItem> findItemsByIds(List<String> consignOrderIds) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrderItem> items = itemMapper.findByConsignOrderIds(consignOrderIds);
            return ResponseBuilder.list(items);
        } catch (Exception e) {
            log.error(className + "findItemsByIds", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<OrderConsignPackageQty> findOrderConsignPackageQty(String siteCode, List<String> orderNos) {
        try {
            Assert.notEmpty(orderNos);
            Assert.notNull(siteCode);
            return ResponseBuilder.list(consignPackageQtyMapper.findBySiteAndOrderNo(siteCode, orderNos));
        } catch (Exception e) {
            log.error(className + "findOrderConsignPackageQty", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> findByNos(List<String> consignOrderNos) {
        try {
            Assert.notEmpty(consignOrderNos);
            return ResponseBuilder.list(consignOrderMapper.findByOrderNos(consignOrderNos));
        } catch (Exception e) {
            log.error(className + "findByNos", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public Response<ConsignOrder> getByNo(String carrierCode, String consignOrderNo) {
        try {
            Assert.notNull(carrierCode);
            Assert.notNull(consignOrderNo);
            ConsignOrder consignOrder = consignOrderMapper.getByNo(carrierCode, consignOrderNo);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            List<ConsignOrderItem> items = itemMapper.findByConsignOrderId(consignOrder.getConsignOrderId());
            consignOrder.setItems(items);
            ConsignOrderOperationTime operationTime = operationTimeMapper.get(consignOrder.getConsignOrderId());
            consignOrder.setOperationTime(operationTime);
            List<ConsignOrderLog> logs = logMapper.findByConsignOrderId(consignOrder.getConsignOrderId());
            consignOrder.setLogs(logs);
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "getByNo", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> findByOrderNo(String orderNo) {
        try {
            Assert.hasText(orderNo);

            return ResponseBuilder.list(consignOrderMapper.findByOrderNo(orderNo));
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public Response getTodayConsignCount(String branchCode, String siteCode) {

        return ResponseBuilder.success(consignOrderMapper.getTodayConsignCount(branchCode, siteCode));
    }

    @Override
    public ListResponse<ConsignDetailDto> findDetailsByOrderNos(List<String> orderNos) {
        try {
            Assert.notEmpty(orderNos);
            List<ConsignDetailDto> result = new ArrayList<>();
            int count = orderNos.size() / 1000;
            for (int i = 1; i <= count; i++) {
                result.addAll(consignOrderMapper.findItemsByOrderNos(orderNos.subList(1000 * (i - 1), 1000 * i)));
            }
            result.addAll(consignOrderMapper.findItemsByOrderNos(orderNos.subList(1000 * count, orderNos.size())));
            return ResponseBuilder.list(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public ListResponse<ConsignOrderLog> findConsignOrderLog(String orderNo) {
        final String validateMessage = "参数异常！";
        try {
            if (org.apache.commons.lang3.StringUtils.isBlank(orderNo))
                return ResponseBuilder.listFail(validateMessage);

            List<ConsignOrderLog> result = new ArrayList<>();//所有托运单的日志

            List<ConsignOrder> consignOrders = consignOrderMapper.findByOrderNo(orderNo);
            consignOrders.forEach(e -> {
                List<ConsignOrderLog> logs = logMapper.findByConsignOrderId(e.getConsignOrderId());
                logs.forEach(log -> {
                    log.setOperationContent("托运单号：" + e.getConsignOrderNo() + "。" + log.getOperationContent());
                });

                if (null != logs && 0 < logs.size())
                    result.addAll(logs);
            });

            return ResponseBuilder.list(result);
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public List<ConsignOrderItemDto> createByDriverQuery(String vehicleId, String branchCode, String siteCode) {
        try {
            if (org.apache.commons.lang3.StringUtils.isEmpty(vehicleId) || org.apache.commons.lang3.StringUtils.isEmpty(branchCode) || org.apache.commons.lang3.StringUtils.isEmpty(siteCode)) {
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

    @Override
    public Response arrive(List<String> consignOrderIds, LocalDateTime arriveTime, String operatorId, String siteCode) {
        try {
            ListResponse<ConsignOrder> arriveResponse = batchArrived(consignOrderIds, arriveTime, operatorId);
            if (!arriveResponse.isSuccess()) {
                return ResponseBuilder.fail(arriveResponse.getMessage());
            }
            // region 修改订单状态到达
            ListResponse<ConsignOrderItem> itemListResponse = findItemsByIds(consignOrderIds);
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
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = findOrderConsignPackageQty(siteCode, orderNos);
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

    @Override
    public Response startUp(List<String> consignOrderIds, LocalDateTime startUpTime, String operatorId, String siteCode, String operatorName) {
        try {
            //托运单启运
            ListResponse<ConsignOrder> startUpResponse = batchStartUp(consignOrderIds, startUpTime, operatorId);
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
            progressTraceService.batchAddDeliveryOrderTrace(traceCreateModels);

            // region 修改订单状态到已启运
            ListResponse<ConsignOrderItem> itemListResponse = findItemsByIds(consignOrderIds);
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
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = findOrderConsignPackageQty(siteCode, orderNos);
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
                ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = findOrderConsignPackageQty(siteCode, orderNos);
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
                    ConsignOrderItemDto consignOrderItemDto = BeanHelper.convert(item, ConsignOrderItemDto.class);
                    LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", item.getOrderNo());
                    Integer totalPackageQty = 0;
                    BigDecimal totalVolume = new BigDecimal(0);
                    BigDecimal totalWeight = new BigDecimal(0);
                    Integer consigningPackageQty = 0;
                    if (logisticsOrderAllDto != null) {
                        String customerCode = logisticsOrderAllDto.getCustomerCode();
                        if (!org.apache.commons.lang3.StringUtils.isEmpty(customerCode)) {
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

    public PageResponse<ConsignOrderPageDto> pageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<ConsignOrderPageDto> pageData = consignOrderMapper.pageList(params);
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
                    if (!org.apache.commons.lang3.StringUtils.isEmpty(dto.getDestCityCode())) {
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
            PageInfo pageInfo = new PageInfo<>(pageData);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    public PageResponse<ConsignPayableDto> searchConsignPayable(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            PageResponse response = this.pageList(page, pageSize, params);
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
                ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = this.findOrderConsignPackageQty((String) params.get("siteCode"), orderNos);
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
                if (!org.apache.commons.lang3.StringUtils.isEmpty(dto.getDestCityCode())) {
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
            ListResponse<OrderConsignPackageQty> orderConsignPackageQtyListResponse = this.findOrderConsignPackageQty(siteCode, orderNos);
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
            if (!org.apache.commons.lang3.StringUtils.isEmpty(consignOrder.getTransferSiteCode()) && consignOrder.getSiteCode().equals(consignOrder.getTransferSiteCode())) {
                return ResponseBuilder.fail("中转站点不能为当前操作站点！");
            }

            List<ConsignOrderItem> items = consignOrder.getItems();
            Response response = this.createOrAddJudge(items, consignOrder.getSiteCode());
            if (!response.isSuccess()) {
                return response;
            }
            //endregion
            Response<ConsignOrder> createResponse = createConsignOrder(consignOrder);
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
                ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = this.findOrderConsignPackageQty(siteCode, orderNos);
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
                        if (!org.apache.commons.lang3.StringUtils.isEmpty(customerCode)) {
                            Customer customer = ObjectQuery.findOne(customers, "code", customerCode);
                            if (customer != null) {
                                dto.setCustomerName(customer.getName());
                            }
                        }
                        dto.setDestCityName(logisticsOrderAllDto.getDeliveryProvince() + logisticsOrderAllDto.getDeliveryCity() +
                                org.apache.commons.lang3.StringUtils.defaultString(logisticsOrderAllDto.getDeliveryDistrict())
                                + org.apache.commons.lang3.StringUtils.defaultString(logisticsOrderAllDto.getDeliveryStreet()));
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
                            if (!org.apache.commons.lang3.StringUtils.isEmpty(customerCode)) {
                                Customer customer = ObjectQuery.findOne(customers, "code", customerCode);
                                if (customer != null) {
                                    dto.setCustomerName(customer.getName());
                                }
                            }
                            dto.setDestCityName(logisticsOrderAllDto.getDeliveryProvince() + logisticsOrderAllDto.getDeliveryCity() +
                                    org.apache.commons.lang3.StringUtils.defaultString(logisticsOrderAllDto.getDeliveryDistrict())
                                    + org.apache.commons.lang3.StringUtils.defaultString(logisticsOrderAllDto.getDeliveryStreet()));
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
                    if (!org.apache.commons.lang3.StringUtils.isEmpty(dto.getOrderNo())) {
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
            Response<ConsignOrder> consignOrderResponse = this.getConsignOrder(consignOrderId);
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

    public Response<ConsignOrder> consign(ConsignOrder consignOrder, String operatorId) {
        try {
            if (null == consignOrder) {
                return ResponseBuilder.fail("托运单不能为空");
            }
            if (null == consignOrder.getItems() || 0 == consignOrder.getItems().size()) {
                return ResponseBuilder.fail("托运单明细不能为空");
            }

            String siteCode = consignOrder.getSiteCode();
            if (!org.apache.commons.lang3.StringUtils.isEmpty(consignOrder.getTransferSiteCode()) && siteCode.equals(consignOrder.getTransferSiteCode())) {
                return ResponseBuilder.fail("中转站点不能为当前操作站点！");
            }

            Response<ConsignOrder> response = new Response<>();
            ConsignOrderOperationTime operationTime = consignOrder.getOperationTime();
            //托运单可在创建时直接发运，此时调用新增方法，反之调用更新方法
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(consignOrder.getConsignOrderId())) {
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
            Response<ConsignOrder> consignOrderResponse = this.consignConsignOrder(consignOrder, operatorId);
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
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = this.findOrderConsignPackageQty(consignOrder.getSiteCode(), orderNos);
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

    //发运时如有中转，则生成中转数据
    private ListResponse<OrderTransfer> consignTransfer(ConsignOrder consignOrder, List<LogisticsOrderAllDto> logisticsOrderAllDtos, String operatorId) {
        List<OrderTransfer> orderTransferList = new ArrayList<>();
        String transferOrganizationCode = consignOrder.getTransferOrganizationCode();
        String transferSiteCode = consignOrder.getTransferSiteCode();
        if ((!org.apache.commons.lang3.StringUtils.isEmpty(transferOrganizationCode)) && (!org.apache.commons.lang3.StringUtils.isEmpty(transferSiteCode))) {
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
            if (org.apache.commons.lang3.StringUtils.isEmpty(consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单ID不能为空");
            }

            if (!org.apache.commons.lang3.StringUtils.isEmpty(consignOrder.getTransferSiteCode()) && consignOrder.getSiteCode().equals(consignOrder.getTransferSiteCode())) {
                return ResponseBuilder.fail("中转站点不能为当前操作站点！");
            }
            //region 对照原有托运单明细，筛选出已去除的订单和新加的订单，判断新加的订单是否满足发运条件
            Response<ConsignOrder> consignOrderResponse = this.getConsignOrder(consignOrder.getConsignOrderId());
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
            Response<ConsignOrder> updateResponse = this.update(modifyDto);
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

    public Response finish(List<String> consignOrderIds, LocalDateTime finishTime, String operatorId) {
        try {
            return this.batchFinished(consignOrderIds, finishTime, operatorId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response cancel(String consignOrderId, String operatorId, String notes, String siteCode) {
        try {
            Response<ConsignOrder> consignOrderResponse = this.getConsignOrder(consignOrderId);
            if (!consignOrderResponse.isSuccess()) {
                return consignOrderResponse;
            }
            Response cancelResponse = this.cancelConsignOrder(consignOrderId, operatorId, notes);
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
            if (org.apache.commons.lang3.StringUtils.isEmpty(consignOrderId)) {
                return ResponseBuilder.fail("托运单ID不能为空");
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(consignOrderNo)) {
                return ResponseBuilder.fail("托运单号不能为空");
            }
            ConsignOrder consignOrder = consignOrderMapper.get(consignOrderId);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            if (consignOrder.getIsTemporaryNo() == null || !consignOrder.getIsTemporaryNo()) {
                return ResponseBuilder.fail("托运单单号非临时单号，不能替换！");
            }
            if (consignOrderMapper.orderNoIsExists(consignOrder.getCarrierCode(), consignOrderNo, consignOrderId)) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            Assert.isTrue(consignOrderMapper.updateOrderNo(consignOrderId, consignOrderNo) == 1);
            ConsignOrderLog log = createLog(consignOrder, "托运单单号更新，原临时单号：" + consignOrder.getConsignOrderNo(), operatorId, false);
            Assert.isTrue(logMapper.insert(log) == 1);
            return ResponseBuilder.success();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(className + "updateOrderNo", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public ListResponse<ConsignQueryDto> queryConsign(Map<String, Object> params) {
        return logisticsOrderService.consignQuery(params);
    }

    public ListResponse<OrderMonthDto> findOrderByMonth(String createdBy) {
        try {
            Assert.hasText(createdBy);
            return ResponseBuilder.list(consignOrderMapper.findOrderByMonth(createdBy));
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    public Response getDetailByNo(String carrieCode, String consignOrderNo) {
        Response response = this.getByNo(carrieCode, consignOrderNo);
        ConsignOrder consignOrder = (ConsignOrder) response.getBody();

        Response<CarrierListDto> carrierListDtoResponse = carrierService.getByCode(consignOrder.getCarrierCode());
        CarrierListDto carrierListDto = carrierListDtoResponse.getBody();
        consignOrder.setCarrierName(carrierListDto.getName());

        return response;
    }

    public ListResponse<ConsignOrderItem> findItemByIds(List<String> consignOrderIds) {
        return this.findItemsByIds(consignOrderIds);
    }

    @Override
    public ListResponse<ConsignOrder> payableModifyQuery(String carrierCode) {
        try {
            if (org.apache.commons.lang3.StringUtils.isEmpty(carrierCode)) {
                return ResponseBuilder.listFail("承运商编码不能为空");
            }
            List<String> statusList = new ArrayList<>();
            statusList.add(ConsignOrder.consignStatus.CONSIGNED.name());
            statusList.add(ConsignOrder.consignStatus.IN_TRANSIT.name());
            statusList.add(ConsignOrder.consignStatus.ARRIVED.name());
            statusList.add(ConsignOrder.consignStatus.FINISHED.name());
            return ResponseBuilder.list(consignOrderMapper.findByCarrierCode(carrierCode, statusList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    public Response<ConsignOrder> getById(String consignOrderId) {
        return this.getConsignOrder(consignOrderId);
    }

    public Response batchConsign(BatchConsignModel model, boolean isMerge) {
        try {
            if (null == model) {
                return ResponseBuilder.fail("批量发运为空");
            }
            if (null == model.getItems() || 0 == model.getItems().size()) {
                return ResponseBuilder.fail("批量发运明细为空");
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(model.getCarrierCode())) {
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
                    if (org.apache.commons.lang3.StringUtils.isEmpty(item.getConsignOrderNo())) {
                        item.setConsignOrderNo(item.getOrderNo());
                    }
                } else {
                    LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(orders, "orderNo", item.getOrderNo());
                    item.setConsignOrderNo(logisticsOrderAllDto.getCustomerCode() + logisticsOrderAllDto.getOrderDate() + (org.apache.commons.lang3.StringUtils.isEmpty(logisticsOrderAllDto.getDeliveryCompany()) ? logisticsOrderAllDto.getCustomerOrderNo() : logisticsOrderAllDto.getDeliveryCompany()));
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
            ListResponse<ConsignOrder> batchCreateResponse = this.batchCreate(consignOrders);
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
            ListResponse<ConsignOrder> batchConsignResponse = this.batchConsignOrder(consignOrders, model.getOperatorId());
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
            ListResponse<OrderConsignPackageQty> consignPackageQtyListResponse = this.findOrderConsignPackageQty(model.getSiteCode(), orderNos);
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

    //创建托运单
    private Response<ConsignOrder> createConsignOrder(ConsignOrder consignOrder) {
        try {
            if (null == consignOrder) {
                return ResponseBuilder.fail("托运单不能为空！");
            }
            if (null == consignOrder.getItems() || 0 == consignOrder.getItems().size()) {
                return ResponseBuilder.fail("托运单明细不能为空！");
            }
            if (!StringUtils.hasText(consignOrder.getBranchCode())) {
                return ResponseBuilder.fail("托运单所属分支不能为空！");
            }
            if (!StringUtils.hasText(consignOrder.getCarrierCode())) {
                return ResponseBuilder.fail("托运单的承运商不能为空！");
            }
            if (!StringUtils.hasText(consignOrder.getConsignOrderNo())) {
                return ResponseBuilder.fail("托运单号不能为空！");
            }
            //判断托运单单号是否已存在
            if (consignOrderMapper.orderNoIsExists(consignOrder.getCarrierCode(), consignOrder.getConsignOrderNo(), consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            consignOrder.setConsignOrderId(Snowflake.getInstance().next());
            consignOrder.setCreateDate(LocalDateTime.now());
            consignOrder.setStatus(ConsignOrder.consignStatus.NEW);
            consignOrder.getItems().forEach(item -> {
                item.setConsignOrderId(consignOrder.getConsignOrderId());
                item.setItemId(Snowflake.getInstance().next());
            });
            int insertResult = consignOrderMapper.insert(consignOrder);
            if (insertResult != 1) {
                return ResponseBuilder.fail("托运单保存失败！");
            }
            Assert.isTrue(itemMapper.batchInsert(consignOrder.getItems()) == consignOrder.getItems().size());
            ConsignOrderOperationTime operationTime = ConsignOrderOperationTime.builder()
                    .consignOrderId(consignOrder.getConsignOrderId())
                    .build();
            Assert.isTrue(operationTimeMapper.insert(operationTime) == 1);
            ConsignOrderLog log = createLog(consignOrder, "创建托运单", consignOrder.getCreatedBy(), false);
            Assert.isTrue(logMapper.insert(log) == 1);
            consignOrder.setOperationTime(operationTime);
            //region托运单明细开单箱数
            List<ConsignOrderItem> items = consignOrder.getItems();
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQtyList = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
            Map<String, Object> map = this.getCreateOrReplace(items, orderConsignPackageQtyList, consignOrder.getSiteCode(), consignOrder.getBranchCode());
            List<OrderConsignPackageQty> addList = (List<OrderConsignPackageQty>) map.get("addList");
            List<OrderConsignPackageQty> updateList = (List<OrderConsignPackageQty>) map.get("updateList");

            if (addList.size() > 0) {
                Assert.isTrue(consignPackageQtyMapper.batchInsert(addList) == addList.size());
            }
            if (updateList.size() > 0) {
                consignPackageQtyMapper.batchUpdateConsigningQty(updateList);
            }
            //endregion
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "create", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    //批量创建托运单
    private ListResponse<ConsignOrder> batchCreate(List<ConsignOrder> consignOrders) {
        try {
            Assert.notEmpty(consignOrders);
            consignOrders.forEach(consignOrder -> {
                Assert.notEmpty(consignOrder.getItems());
                Assert.hasText(consignOrder.getBranchCode());
                Assert.hasText(consignOrder.getCarrierCode());
                Assert.hasText(consignOrder.getConsignOrderNo());
            });
            //region 判断批量创建时，同一个承运商的订单号是否有重复
            Set<String> repeatOrderNos = new HashSet<>();
            for (int i = 0; i < consignOrders.size() - 1; i++) {
                String carrierCode = consignOrders.get(i).getCarrierCode();
                String consignOrderNo = consignOrders.get(i).getConsignOrderNo();
                for (int j = i + 1; j < consignOrders.size(); j++) {
                    String consignOrderNo1 = consignOrders.get(j).getConsignOrderNo();
                    String carrierCode1 = consignOrders.get(j).getCarrierCode();
                    if (carrierCode.equals(carrierCode1) && consignOrderNo.equals(consignOrderNo1)) {
                        repeatOrderNos.add(consignOrderNo);
                    }
                }
            }
            if (repeatOrderNos.size() > 0) {
                return ResponseBuilder.listFail("托运单单号" + String.join(",", repeatOrderNos) + "重复");
            }
            //endregion
            //region 判断托运单单号是否已存在
            List<String> consignOrderNos = consignOrders.stream().map(ConsignOrder::getConsignOrderNo).collect(Collectors.toList());
            List<ConsignOrder> consignOrders1 = consignOrderMapper.findByOrderNos(consignOrderNos);
            List<String> exitsOrderNos = new ArrayList<>();
            for (ConsignOrder consignOrder : consignOrders) {
                for (ConsignOrder consignOrder1 : consignOrders1) {
                    if (consignOrder.getConsignOrderNo().equals(consignOrder1.getConsignOrderNo()) &&
                            consignOrder.getCarrierCode().equals(consignOrder1.getCarrierCode())) {
                        exitsOrderNos.add(consignOrder.getConsignOrderNo());
                    }
                }
            }
            if (exitsOrderNos.size() > 0) {
                return ResponseBuilder.listFail("托运单单号" + String.join(",", exitsOrderNos) + "已存在");
            }
            List<ConsignOrderItem> items = new ArrayList<>();
            List<ConsignOrderOperationTime> operationTimes = new ArrayList<>();
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setConsignOrderId(Snowflake.getInstance().next());
                consignOrder.setCreateDate(LocalDateTime.now());
                consignOrder.setStatus(ConsignOrder.consignStatus.NEW);
                consignOrder.getItems().forEach(item -> {
                    item.setConsignOrderId(consignOrder.getConsignOrderId());
                    item.setItemId(Snowflake.getInstance().next());
                });
                items.addAll(consignOrder.getItems());
                ConsignOrderOperationTime operationTime = ConsignOrderOperationTime.builder()
                        .consignOrderId(consignOrder.getConsignOrderId())
                        .build();
                operationTimes.add(operationTime);
                consignOrder.setOperationTime(operationTime);
                ConsignOrderLog log = createLog(consignOrder, "创建托运单", consignOrder.getCreatedBy(), false);
                logs.add(log);
            });
            Assert.isTrue(consignOrderMapper.batchInsert(consignOrders) == consignOrders.size());
            Assert.isTrue(itemMapper.batchInsert(items) == items.size());
            Assert.isTrue(operationTimeMapper.batchInsert(operationTimes) == operationTimes.size());
            Assert.isTrue(logMapper.batchInsert(logs) == logs.size());
            //endregion
            //region托运单明细开单箱数
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQtyList = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
            Map<String, Object> map = this.getCreateOrReplace(items, orderConsignPackageQtyList, consignOrders.get(0).getSiteCode(), consignOrders.get(0).getBranchCode());
            List<OrderConsignPackageQty> addList = (List<OrderConsignPackageQty>) map.get("addList");
            List<OrderConsignPackageQty> updateList = (List<OrderConsignPackageQty>) map.get("updateList");
            if (addList.size() > 0) {
                Assert.isTrue(consignPackageQtyMapper.batchInsert(addList) == addList.size());
            }
            if (updateList.size() > 0) {
                consignPackageQtyMapper.batchUpdateConsigningQty(updateList);
            }
            //endregion
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchCreate", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    //更新托运单
    private Response<ConsignOrder> update(ConsignOrderModifyDto consignOrderModifyDto) {
        try {
            Assert.notNull(consignOrderModifyDto);
            ConsignOrder consignOrder = consignOrderModifyDto.getConsignOrder();
            Assert.notNull(consignOrder);
            Assert.notNull(consignOrder.getConsignOrderId());
            Assert.notEmpty(consignOrder.getItems());
            ConsignOrder oldConsignOrder = consignOrderMapper.get(consignOrder.getConsignOrderId());
            if (!ConsignOrder.consignStatus.NEW.equals(oldConsignOrder.getStatus())) {
                return ResponseBuilder.fail("托运单已发运或已取消，不能操作修改！");
            }
            if (consignOrderMapper.orderNoIsExists(consignOrder.getCarrierCode(), consignOrder.getConsignOrderNo(), consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            consignOrder.setModifyDate(LocalDateTime.now());
            Assert.isTrue(consignOrderMapper.update(consignOrder) == 1);
            //删除原有的订单明细
            Assert.isTrue(itemMapper.deleteByConsignOrderId(consignOrder.getConsignOrderId()) > 0);
            //新增新的订单明细
            consignOrder.getItems().forEach(item -> {
                item.setItemId(Snowflake.getInstance().next());
                item.setConsignOrderId(consignOrder.getConsignOrderId());
            });
            Assert.isTrue(itemMapper.batchInsert(consignOrder.getItems()) == consignOrder.getItems().size());
            StringBuffer operationContent = new StringBuffer();
            operationContent.append("修改托运单");
            if (consignOrderModifyDto.getAddItems() != null && consignOrderModifyDto.getAddItems().size() > 0) {
                List<String> orderNos = consignOrderModifyDto.getAddItems().stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
                operationContent.append("，增加订单" + String.join(",", orderNos));
            }
            if (consignOrderModifyDto.getRemoveItems() != null && consignOrderModifyDto.getRemoveItems().size() > 0) {
                List<String> orderNos = consignOrderModifyDto.getRemoveItems().stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
                operationContent.append("，减少订单" + String.join(",", orderNos));
            }
            ConsignOrderLog log = createLog(consignOrder, operationContent.toString(), consignOrder.getModifiedBy(), false);
            Assert.isTrue(logMapper.insert(log) == 1);

            //region托运单明细开单箱数
            String siteCode = consignOrder.getSiteCode();
            String branchCode = consignOrder.getBranchCode();
            List<ConsignOrderItem> addItems = consignOrderModifyDto.getAddItems();
            List<ConsignOrderItem> removeItems = consignOrderModifyDto.getRemoveItems();
            List<ConsignOrderItem> updateOldItems = consignOrderModifyDto.getUpdateOldItems();
            List<ConsignOrderItem> updateNewItems = consignOrderModifyDto.getUpdateNewItems();

            List<ConsignOrderItem> items = consignOrder.getItems();
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            if (removeItems != null && removeItems.size() > 0) {
                orderNos.addAll(removeItems.stream().map(removeItem -> removeItem.getOrderNo()).collect(Collectors.toList()));
            }
            List<OrderConsignPackageQty> orderConsignPackageQtyList = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
            List<OrderConsignPackageQty> addListAll = new ArrayList<>();
            List<OrderConsignPackageQty> updateListAll = new ArrayList<>();

            if (addItems != null && addItems.size() > 0) {
                Map<String, Object> map = this.getCreateOrReplace(addItems, orderConsignPackageQtyList, siteCode, branchCode);
                List<OrderConsignPackageQty> addList = (List<OrderConsignPackageQty>) map.get("addList");
                List<OrderConsignPackageQty> updateList = (List<OrderConsignPackageQty>) map.get("updateList");
                if (addList != null && addList.size() > 0) {
                    addListAll.addAll(addList);
                }
                if (updateList != null && updateList.size() > 0) {
                    updateListAll.addAll(updateList);
                }
            }
            if (removeItems != null && removeItems.size() > 0) {
                for (ConsignOrderItem item : removeItems) {
                    OrderConsignPackageQty orderConsignPackageQty = null;
                    if (orderConsignPackageQtyList != null && orderConsignPackageQtyList.size() > 0) {
                        for (OrderConsignPackageQty orderConsignPackageQty1 : orderConsignPackageQtyList) {
                            if (item.getOrderNo().equals(orderConsignPackageQty1.getOrderNo())) {
                                orderConsignPackageQty = orderConsignPackageQty1;
                                break;
                            }
                        }
                    }
                    Assert.notNull(orderConsignPackageQty);
                    orderConsignPackageQty.setConsigningPackageQty((orderConsignPackageQty.getConsigningPackageQty() == null ? 0 : orderConsignPackageQty.getConsigningPackageQty()) - item.getPackageQuantity());
                    updateListAll.add(orderConsignPackageQty);
                }
            }
            if (updateOldItems != null && updateOldItems.size() > 0) {
                for (ConsignOrderItem oldItem : updateOldItems) {
                    OrderConsignPackageQty orderConsignPackageQty = null;
                    ConsignOrderItem newItem = null;
                    if (orderConsignPackageQtyList != null && orderConsignPackageQtyList.size() > 0) {
                        for (OrderConsignPackageQty orderConsignPackageQty1 : orderConsignPackageQtyList) {
                            if (oldItem.getOrderNo().equals(orderConsignPackageQty1.getOrderNo())) {
                                orderConsignPackageQty = orderConsignPackageQty1;
                                break;
                            }
                        }
                    }
                    if (updateNewItems != null && updateNewItems.size() > 0) {
                        for (ConsignOrderItem newItem1 : updateNewItems) {
                            if (oldItem.getOrderNo().equals(newItem1.getOrderNo())) {
                                newItem = newItem1;
                                break;
                            }
                        }
                    }
                    Assert.notNull(orderConsignPackageQty);
                    Assert.notNull(newItem);
                    orderConsignPackageQty.setConsigningPackageQty((orderConsignPackageQty.getConsigningPackageQty() == null ? 0 : orderConsignPackageQty.getConsigningPackageQty())
                            + newItem.getPackageQuantity() - oldItem.getPackageQuantity());
                    updateListAll.add(orderConsignPackageQty);
                }
            }

            if (addListAll.size() > 0) {
                Assert.isTrue(consignPackageQtyMapper.batchInsert(addListAll) == addListAll.size());
            }
            if (updateListAll.size() > 0) {
                consignPackageQtyMapper.batchUpdateConsigningQty(updateListAll);
            }
            //endregion
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "update", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    private Map<String, Object> getCreateOrReplace(List<ConsignOrderItem> items,
                                                   List<OrderConsignPackageQty> orderConsignPackageQties,
                                                   String siteCode, String branchCode) throws Exception {
        try {
            Map<String, Object> map = new HashedMap();
            if (items != null && items.size() > 0) {
                List<OrderConsignPackageQty> addList = new ArrayList<>();
                List<OrderConsignPackageQty> updateList = new ArrayList<>();
                for (ConsignOrderItem item : items) {
                    OrderConsignPackageQty orderConsignPackageQty = new OrderConsignPackageQty();
                    if (orderConsignPackageQties != null && orderConsignPackageQties.size() > 0) {
                        for (OrderConsignPackageQty orderConsignPackageQty1 : orderConsignPackageQties) {
                            if (item.getOrderNo().equals(orderConsignPackageQty1.getOrderNo())) {
                                orderConsignPackageQty = orderConsignPackageQty1;
                                break;
                            }
                        }
                    }
                    //新增或更新
                    if (StringUtils.isEmpty(orderConsignPackageQty.getId())) {
                        orderConsignPackageQty.setId(Snowflake.getInstance().next());
                        orderConsignPackageQty.setOrderNo(item.getOrderNo());
                        orderConsignPackageQty.setConsigningPackageQty(item.getPackageQuantity());
                        orderConsignPackageQty.setSiteCode(siteCode);
                        orderConsignPackageQty.setBranchCode(branchCode);
                        addList.add(orderConsignPackageQty);
                    } else {
                        orderConsignPackageQty.setConsigningPackageQty((orderConsignPackageQty.getConsigningPackageQty() == null ? 0 : orderConsignPackageQty.getConsigningPackageQty()) + item.getPackageQuantity());
                        updateList.add(orderConsignPackageQty);
                    }
                }
                map.put("addList", addList);
                map.put("updateList", updateList);
            }
            return map;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    //托运单发运
    private Response<ConsignOrder> consignConsignOrder(ConsignOrder consignOrder, String operatorId) {
        try {
            Assert.notNull(consignOrder);
            Assert.notNull(consignOrder.getConsignOrderId());
            Assert.notNull(consignOrder.getOperationTime());
            ConsignOrder oldConsignOrder = consignOrderMapper.get(consignOrder.getConsignOrderId());
            if (!ConsignOrder.consignStatus.NEW.equals(oldConsignOrder.getStatus())) {
                return ResponseBuilder.fail("托运单已发运或已取消，不能操作发运！");
            }
            if (consignOrderMapper.orderNoIsExists(consignOrder.getCarrierCode(), consignOrder.getConsignOrderNo(), consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            consignOrder.setStatus(ConsignOrder.consignStatus.CONSIGNED);
            Assert.isTrue(consignOrderMapper.update(consignOrder) == 1);
            ConsignOrderOperationTime operationTime = operationTimeMapper.get(consignOrder.getConsignOrderId());
            operationTime.setConsignTime(consignOrder.getOperationTime().getConsignTime());
            operationTime.setFeedbackConsignTime(consignOrder.getOperationTime().getFeedbackConsignTime());
            operationTime.setPredictArriveTime(consignOrder.getOperationTime().getPredictArriveTime());
            consignOrder.setOperationTime(operationTime);
            Assert.isTrue(operationTimeMapper.update(operationTime) == 1);
            //日志
            ConsignOrderLog log = createLog(consignOrder, "托运单发运", operatorId, false);
            Assert.isTrue(logMapper.insert(log) == 1);
            //发运箱数
            List<String> orderNos = consignOrder.getItems().stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
            List<OrderConsignPackageQty> updateConsignQtyList = new ArrayList<>();
            consignOrder.getItems().stream().forEach(item ->
                    {
                        Optional<OrderConsignPackageQty> orderConsignPackageQtyOptional =
                                orderConsignPackageQties.stream().filter(orderConsignPackageQty -> orderConsignPackageQty.getOrderNo().equals(item.getOrderNo())).findFirst();
                        Assert.isTrue(orderConsignPackageQtyOptional.isPresent());
                        OrderConsignPackageQty orderConsignPackageQty = orderConsignPackageQtyOptional.get();
                        orderConsignPackageQty.setConsignedPackageQty(
                                (orderConsignPackageQty.getConsignedPackageQty() == null ? 0 : orderConsignPackageQty.getConsignedPackageQty()) + item.getPackageQuantity());
                        updateConsignQtyList.add(orderConsignPackageQty);
                    }
            );
            if (updateConsignQtyList.size() > 0) {
                consignPackageQtyMapper.batchUpdateConsignedQty(updateConsignQtyList);
            }
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "consign", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    //批量发运
    private ListResponse<ConsignOrder> batchConsignOrder(List<ConsignOrder> consignOrders, String operatorId) {
        try {
            Assert.notEmpty(consignOrders);
            consignOrders.stream().forEach(consignOrder -> {
                Assert.notNull(consignOrder);
                Assert.notNull(consignOrder.getConsignOrderId());
                Assert.notNull(consignOrder.getOperationTime());
            });
            List<String> consignOrderIds = consignOrders.stream().map(ConsignOrder::getConsignOrderId).collect(Collectors.toList());
            List<ConsignOrder> oldConsignOrders = consignOrderMapper.findByIds(consignOrderIds);
            List<String> statusExceptionOrderNos = new ArrayList<>();
            for (ConsignOrder consignOrder : oldConsignOrders) {
                if (!ConsignOrder.consignStatus.NEW.equals(consignOrder.getStatus())) {
                    statusExceptionOrderNos.add(consignOrder.getConsignOrderNo());
                }
            }
            if (statusExceptionOrderNos.size() > 0) {
                return ResponseBuilder.listFail("托运单" + String.join(",", statusExceptionOrderNos) + "已发运或已取消，不能操作发运！");
            }
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.CONSIGNED);
            });
            Assert.isTrue(consignOrderMapper.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.CONSIGNED) == consignOrderIds.size());
            List<ConsignOrderOperationTime> operationTimes = consignOrders.stream().map(ConsignOrder::getOperationTime).collect(Collectors.toList());
            operationTimeMapper.batchUpdateConsignTime(operationTimes);
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.CONSIGNED);
                ConsignOrderLog log = createLog(consignOrder, "托运单发运", operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logMapper.batchInsert(logs) == logs.size());
            //发运箱数
            List<ConsignOrderItem> items = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> items.addAll(consignOrder.getItems()));
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
            List<OrderConsignPackageQty> updateConsignQtyList = new ArrayList<>();
            items.stream().forEach(item ->
                    {
                        Optional<OrderConsignPackageQty> orderConsignPackageQtyOptional =
                                orderConsignPackageQties.stream().filter(orderConsignPackageQty -> orderConsignPackageQty.getOrderNo().equals(item.getOrderNo())).findFirst();
                        Assert.isTrue(orderConsignPackageQtyOptional.isPresent());
                        OrderConsignPackageQty orderConsignPackageQty = orderConsignPackageQtyOptional.get();
                        orderConsignPackageQty.setConsignedPackageQty(
                                (orderConsignPackageQty.getConsignedPackageQty() == null ? 0 : orderConsignPackageQty.getConsignedPackageQty()) + item.getPackageQuantity());
                        updateConsignQtyList.add(orderConsignPackageQty);
                    }
            );
            if (updateConsignQtyList.size() > 0) {
                consignPackageQtyMapper.batchUpdateConsignedQty(updateConsignQtyList);
            }
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchConsign", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    //批量启运
    private ListResponse<ConsignOrder> batchStartUp(List<String> consignOrderIds, LocalDateTime startUpTime, String operatorId) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderMapper.findByIds(consignOrderIds);
            List<String> statusExceptionOrderNos = new ArrayList<>();
            for (ConsignOrder consignOrder : consignOrders) {
                if (!ConsignOrder.consignStatus.CONSIGNED.equals(consignOrder.getStatus())) {
                    statusExceptionOrderNos.add(consignOrder.getConsignOrderNo());
                }
            }
            if (statusExceptionOrderNos.size() > 0) {
                return ResponseBuilder.listFail(String.join(",", statusExceptionOrderNos) + "不能操作发运，请选择已发运的托运单！");
            }
            Assert.isTrue(consignOrderMapper.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.IN_TRANSIT) == consignOrderIds.size());
            Assert.isTrue(operationTimeMapper.batchUpdateStartUpTime(consignOrderIds, startUpTime) == consignOrderIds.size());
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.IN_TRANSIT);
                ConsignOrderLog log = createLog(consignOrder, "托运单启运，启运时间：" + startUpTime, operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logMapper.batchInsert(logs) == logs.size());
            //启运箱数
            List<ConsignOrderItem> items = itemMapper.findByConsignOrderIds(consignOrderIds);
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
            List<OrderConsignPackageQty> updateStartUpQtyList = new ArrayList<>();
            items.stream().forEach(item ->
                    {
                        Optional<OrderConsignPackageQty> orderConsignPackageQtyOptional =
                                orderConsignPackageQties.stream().filter(orderConsignPackageQty -> orderConsignPackageQty.getOrderNo().equals(item.getOrderNo())).findFirst();
                        Assert.isTrue(orderConsignPackageQtyOptional.isPresent());
                        OrderConsignPackageQty orderConsignPackageQty = orderConsignPackageQtyOptional.get();
                        orderConsignPackageQty.setStartupPackageQty(
                                (orderConsignPackageQty.getStartupPackageQty() == null ? 0 : orderConsignPackageQty.getStartupPackageQty()) + item.getPackageQuantity());
                        updateStartUpQtyList.add(orderConsignPackageQty);
                    }
            );
            if (updateStartUpQtyList.size() > 0) {
                consignPackageQtyMapper.batchUpdateStartUpQty(updateStartUpQtyList);
            }
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchStartUp", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    //托运单批量到达
    private ListResponse<ConsignOrder> batchArrived(List<String> consignOrderIds, LocalDateTime arriveTime, String operatorId) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderMapper.findByIds(consignOrderIds);
            List<String> statusExceptionOrderNos = new ArrayList<>();
            for (ConsignOrder consignOrder : consignOrders) {
                if (!ConsignOrder.consignStatus.IN_TRANSIT.equals(consignOrder.getStatus())) {
                    statusExceptionOrderNos.add(consignOrder.getConsignOrderNo());
                }
            }
            if (statusExceptionOrderNos.size() > 0) {
                return ResponseBuilder.listFail(String.join(",", statusExceptionOrderNos) + "不能操作到达，请选择在途中的托运单！");
            }
            Assert.isTrue(consignOrderMapper.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.ARRIVED) == consignOrderIds.size());
            Assert.isTrue(operationTimeMapper.batchUpdateArriveTime(consignOrderIds, arriveTime) == consignOrderIds.size());
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.ARRIVED);
                ConsignOrderLog log = createLog(consignOrder, "托运单到达,到达时间：" + arriveTime, operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logMapper.batchInsert(logs) == logs.size());
            //启运箱数
            List<ConsignOrderItem> items = itemMapper.findByConsignOrderIds(consignOrderIds);
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
            List<OrderConsignPackageQty> updateStartUpQtyList = new ArrayList<>();
            items.stream().forEach(item ->
                    {
                        Optional<OrderConsignPackageQty> orderConsignPackageQtyOptional =
                                orderConsignPackageQties.stream().filter(orderConsignPackageQty -> orderConsignPackageQty.getOrderNo().equals(item.getOrderNo())).findFirst();
                        Assert.isTrue(orderConsignPackageQtyOptional.isPresent());
                        OrderConsignPackageQty orderConsignPackageQty = orderConsignPackageQtyOptional.get();
                        orderConsignPackageQty.setArrivedPackageQty(
                                (orderConsignPackageQty.getArrivedPackageQty() == null ? 0 : orderConsignPackageQty.getArrivedPackageQty()) + item.getPackageQuantity());
                        updateStartUpQtyList.add(orderConsignPackageQty);
                    }
            );
            if (updateStartUpQtyList.size() > 0) {
                consignPackageQtyMapper.batchUpdateArriveQty(updateStartUpQtyList);
            }
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchArrived", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    //批量完成
    private ListResponse<ConsignOrder> batchFinished(List<String> consignOrderIds, LocalDateTime finishTime, String operatorId) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderMapper.findByIds(consignOrderIds);
            List<String> statusExceptionOrderNos = new ArrayList<>();
            for (ConsignOrder consignOrder : consignOrders) {
                if (!ConsignOrder.consignStatus.ARRIVED.equals(consignOrder.getStatus())
                        && !ConsignOrder.consignStatus.IN_TRANSIT.equals(consignOrder.getStatus())) {
                    statusExceptionOrderNos.add(consignOrder.getConsignOrderNo());
                }
            }
            if (statusExceptionOrderNos.size() > 0) {
                return ResponseBuilder.listFail(String.join(",", statusExceptionOrderNos) + "不能操作完成，请选择在途或到达的托运单！");
            }
            Assert.isTrue(consignOrderMapper.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.FINISHED) == consignOrderIds.size());
            Assert.isTrue(operationTimeMapper.batchUpdateFinishTime(consignOrderIds, finishTime) == consignOrderIds.size());
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.FINISHED);
                ConsignOrderLog log = createLog(consignOrder, "托运单完成,完成时间：" + finishTime, operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logMapper.batchInsert(logs) == logs.size());
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchFinished", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    //取消托运单
    private Response<ConsignOrder> cancelConsignOrder(String consignOrderId, String operatorId, String notes) {
        try {
            Assert.notNull(consignOrderId);
            ConsignOrder consignOrder = consignOrderMapper.get(consignOrderId);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            if (!ConsignOrder.consignStatus.NEW.equals(consignOrder.getStatus())) {
                return ResponseBuilder.fail("托运单不能操作取消，请选择开单中的托运单！");
            }
            List<ConsignOrderItem> items = itemMapper.findByConsignOrderId(consignOrderId);
            Assert.isTrue(consignOrderMapper.updateStatus(consignOrderId, ConsignOrder.consignStatus.CANCELED) == 1);
            Assert.isTrue(itemMapper.deleteByConsignOrderId(consignOrderId) > 0);
            consignOrder.setStatus(ConsignOrder.consignStatus.CANCELED);
            ConsignOrderLog log = createLog(consignOrder, "托运单取消,取消原因：" + notes, operatorId, false);
            Assert.isTrue(logMapper.insert(log) == 1);
            //发运箱数
            List<OrderConsignPackageQty> updateList = new ArrayList<>();
            if (items != null && items.size() > 0) {
                for (ConsignOrderItem item : items) {
                    OrderConsignPackageQty orderConsignPackageQty = null;
                    List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
                    List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyMapper.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
                    if (orderConsignPackageQties != null && orderConsignPackageQties.size() > 0) {
                        for (OrderConsignPackageQty orderConsignPackageQty1 : orderConsignPackageQties) {
                            if (item.getOrderNo().equals(orderConsignPackageQty1.getOrderNo())) {
                                orderConsignPackageQty = orderConsignPackageQty1;
                                break;
                            }
                        }
                    }
                    Assert.notNull(orderConsignPackageQty);
                    orderConsignPackageQty.setConsigningPackageQty((orderConsignPackageQty.getConsigningPackageQty() == null ? 0 : orderConsignPackageQty.getConsigningPackageQty()) - item.getPackageQuantity());
                    updateList.add(orderConsignPackageQty);
                }
            }
            if (updateList.size() > 0) {
                consignPackageQtyMapper.batchUpdateConsigningQty(updateList);
            }
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "cancel", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    //根据ID查询托运单
    private Response<ConsignOrder> getConsignOrder(String consignOrderId) {
        try {
            Assert.notNull(consignOrderId);
            ConsignOrder consignOrder = consignOrderMapper.get(consignOrderId);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            List<ConsignOrderItem> items = itemMapper.findByConsignOrderId(consignOrderId);
            consignOrder.setItems(items);
            ConsignOrderOperationTime operationTime = operationTimeMapper.get(consignOrderId);
            consignOrder.setOperationTime(operationTime);
            List<ConsignOrderLog> logs = logMapper.findByConsignOrderId(consignOrderId);
            consignOrder.setLogs(logs);
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "get", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    private ConsignOrderLog createLog(ConsignOrder consignOrder, String operationContent,
                                      String operatorId, Boolean whetherFeedback) {
        ConsignOrderLog log = ConsignOrderLog.builder()
                .logId(Snowflake.getInstance().next())
                .consignOrderId(consignOrder.getConsignOrderId())
                .operationTime(LocalDateTime.now())
                .status(consignOrder.getStatus())
                .operatorId(operatorId)
                .operationContent(operationContent)
                .whetherFeedback(whetherFeedback)
                .build();
        return log;
    }

}

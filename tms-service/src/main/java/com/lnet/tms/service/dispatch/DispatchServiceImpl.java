package com.lnet.tms.service.dispatch;

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
import com.lnet.model.cnaps.payEntity.PayableProportion;
import com.lnet.model.cnaps.payEntity.PayableProportionAccount;
import com.lnet.model.oms.order.orderDto.DispatchQueryDto;
import com.lnet.model.oms.order.orderDto.OrderDispatchDto;
import com.lnet.model.oms.order.orderEntity.CollectingInstruction;
import com.lnet.model.tms.dispatch.dispatchDto.*;
import com.lnet.model.tms.dispatch.dispatchEntity.*;
import com.lnet.model.tms.order.orderEntity.OrderTransfer;
import com.lnet.model.tms.pack.packDto.OrderPackageRecordDto;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchItemPayableDto;
import com.lnet.model.ums.customer.customerEntity.Customer;
import com.lnet.model.ums.expense.ExpenseAccount;
import com.lnet.model.ums.organization.Organization;
import com.lnet.model.ums.transprotation.transprotationDto.LogisticsOrderAllDto;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.model.ums.user.User;
import com.lnet.model.ums.vehicle.vehicleDto.VehicleListDto;
import com.lnet.oms.contract.api.CollectingInstructionService;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.tms.contract.spi.TransferService;
import com.lnet.tms.contract.spi.consgin.ConsignOrderService;
import com.lnet.tms.contract.spi.dispatch.DispatchService;
import com.lnet.tms.contract.spi.pack.PackageService;
import com.lnet.tms.mapper.*;
import com.lnet.ums.contract.api.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class DispatchServiceImpl implements DispatchService {

    private final String className = this.getClass().getSimpleName() + ".";

    private static final String DISPATCH_PAYABLE_PARENT_CODE = "120200";
    private static final String TO_DISPATCHING_INSTRUCTION_NO_LIST = "toDispatchingInstructionNos";
    private static final String TO_DISPATCHING_ORDER_NO_LIST = "toDispatchingOrderNos";
    private static final String TO_DISTRIBUTION_IN_ORDER_NO_LIST = "toDistributionInOrderNos";
    private static final String TO_COLLECTING_INSTRUCTION_NO_LIST = "toCollectingInstructionNos";
    private static final String TO_DISPATCHED_TRANSFER_ORDER_NO_LIST = "toDispatchedTransferOrderNos";
    @Autowired
    DispatchMapper dispatchMapper;

    @Autowired
    DispatchItemMapper dispatchItemMapper;

    @Autowired
    DispatchLogMapper dispatchLogMapper;

    @Autowired
    DispatchFollowMapper dispatchFollowMapper;

    @Autowired
    DispatchPackageMapper dispatchPackageMapper;

    @Autowired
    OrderDispatchPackageQtyMapper orderDispatchPackageQtyMapper;

    @Autowired
    DispatchFeeDetailMapper feeDetailMapper;

    @Autowired
    PackageService packageService;
    @Autowired
    CustomerService customerService;

    @Resource
    ConsignOrderService consignOrderService;

    @Resource
    VehicleService vehicleService;

    @Autowired
    TransferService transferService;

    @Resource
    PayableService payableService;
//    @Resource
//    Notificatio

    @Resource
    UserService userService;

    @Resource
    CollectingInstructionService collectingInstructionService;

    @Resource
    LogisticsOrderService logisticsOrderService;

    @Resource
    OrganizationService organizationService;

    @Resource
    DistrictService districtService;

    @Resource
    ExpenseAccountService expenseAccountService;

    private Response<Dispatch> createDispatch(Dispatch dispatch) {
        try {
            Assert.notEmpty(dispatch.getItems());

            dispatch.setDispatchId(Snowflake.getInstance().next());
            String dispatchNumber = "DI" + new DecimalFormat("00000000").format(dispatchMapper.getDispatchSequenceNo());
            dispatch.setDispatchNumber(dispatchNumber);
            dispatch.setCreatedDate(LocalDateTime.now());
            if (dispatch.getDriver() == null) {
                dispatch.setStatus(Dispatch.statusEnum.NEW);
            } else {
                dispatch.setStatus(Dispatch.statusEnum.ASSIGNED);
                dispatch.setAssignDate(LocalDateTime.now());
            }
            Assert.isTrue(dispatchMapper.insert(dispatch) > 0, "创建派车单失败");
            dispatch.getItems().stream().forEach(item -> {
                item.setDispatchId(dispatch.getDispatchId());
                item.setDispatchItemId(Snowflake.getInstance().next());
            });
            Assert.isTrue(dispatchItemMapper.batchInsert(dispatch.getItems()) > 0, "创建派车单明细失败");
            if (dispatch.getFeeDetails() != null && dispatch.getFeeDetails().size() > 0) {
                dispatch.getFeeDetails().stream().forEach(feeDetail -> {
                    feeDetail.setDispatchId(dispatch.getDispatchId());
                    feeDetail.setDispatchFeeDetailId(Snowflake.getInstance().next());
                });
                Assert.isTrue(feeDetailMapper.batchInsert(dispatch.getFeeDetails()) == dispatch.getFeeDetails().size());
            }
            if (dispatch.getFollows() != null && dispatch.getFollows().size() > 0) {
                dispatch.getFollows().stream().forEach(follow -> {
                    follow.setDispatchId(dispatch.getDispatchId());
                    follow.setDispatchFollowId(Snowflake.getInstance().next());
                });
                Assert.isTrue(dispatchFollowMapper.batchInsert(dispatch.getFollows()) > 0, "创建派车单跟车人失败");
            }
            if (dispatch.getPackages() != null && dispatch.getPackages().size() > 0) {
                dispatch.getPackages().stream().forEach(dispatchPackage -> {
                    dispatchPackage.setDispatchId(dispatch.getDispatchId());
                    dispatchPackage.setDispatchPackageId(Snowflake.getInstance().next());
                });
                Assert.isTrue(dispatchPackageMapper.batchInsert(dispatch.getPackages()) > 0, "创建派车单箱明细失败");
            }
            DispatchLog log = this.buildLog(dispatch, dispatch.getCreatedBy(), "创建");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "创建派车单日志失败");
            //region派车箱数
            List<DispatchItem> items = dispatch.getItems();
            List<String> orderNos = items.stream().map(DispatchItem::getOrderNo).collect(Collectors.toList());
            List<OrderDispatchPackageQty> orderDispatchPackageQtyList = orderDispatchPackageQtyMapper.findByOrderNos(dispatch.getSiteCode(), orderNos);
            Map<String, Object> map = this.getCreateOrReplace(items, orderDispatchPackageQtyList, dispatch.getSiteCode(), dispatch.getBranchCode());
            List<OrderDispatchPackageQty> addList = (List<OrderDispatchPackageQty>) map.get("addList");
            List<OrderDispatchPackageQty> updateList = (List<OrderDispatchPackageQty>) map.get("updateList");

            if (addList.size() > 0) {
                Assert.isTrue(orderDispatchPackageQtyMapper.batchInsert(addList) == addList.size());
            }
            if (updateList.size() > 0) {
                orderDispatchPackageQtyMapper.batchUpdateDispatchingQty(updateList);
            }
            //endregion
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "create", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    private Map<String, Object> getCreateOrReplace(List<DispatchItem> items,
                                                   List<OrderDispatchPackageQty> orderDispatchPackageQties,
                                                   String siteCode, String branchCode) throws Exception {
        try {
            Map<String, Object> map = new HashedMap();
            if (items != null && items.size() > 0) {
                List<OrderDispatchPackageQty> addList = new ArrayList<>();
                List<OrderDispatchPackageQty> updateList = new ArrayList<>();
                for (DispatchItem item : items) {
                    OrderDispatchPackageQty orderDispatchPackageQty = new OrderDispatchPackageQty();
                    if (orderDispatchPackageQties != null && orderDispatchPackageQties.size() > 0) {
                        for (OrderDispatchPackageQty orderDispatchPackageQty1 : orderDispatchPackageQties) {
                            if (item.getOrderNo().equals(orderDispatchPackageQty1.getOrderNo())) {
                                orderDispatchPackageQty = orderDispatchPackageQty1;
                                break;
                            }
                        }
                    }
                    //新增或更新
                    if (StringUtils.isEmpty(orderDispatchPackageQty.getId())) {
                        orderDispatchPackageQty.setId(Snowflake.getInstance().next());
                        orderDispatchPackageQty.setOrderNo(item.getOrderNo());
                        orderDispatchPackageQty.setDispatchingPackageQty(item.getPackageQuantity());
                        orderDispatchPackageQty.setSiteCode(siteCode);
                        orderDispatchPackageQty.setBranchCode(branchCode);
                        addList.add(orderDispatchPackageQty);
                    } else {
                        orderDispatchPackageQty.setDispatchingPackageQty((orderDispatchPackageQty.getDispatchingPackageQty() == null ? 0 : orderDispatchPackageQty.getDispatchingPackageQty()) + item.getPackageQuantity());
                        updateList.add(orderDispatchPackageQty);
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

    private Response<Map<String, Object>> createOrAddItem(List<DispatchItem> items, String siteCode, Dispatch.statusEnum dispatchStatus) {
        try {
            //已派车箱数
            List<String> allOrderNos = items.stream().map(DispatchItem::getOrderNo).collect(Collectors.toList());
            ListResponse<OrderDispatchPackageQty> dispatchPackageQtyListResponse = findOrderDispatchPackageQty(siteCode, allOrderNos);
            if (!dispatchPackageQtyListResponse.isSuccess()) {
                return ResponseBuilder.fail(dispatchPackageQtyListResponse.getMessage());
            }
            List<OrderDispatchPackageQty> orderDispatchPackageQties = dispatchPackageQtyListResponse.getBody();
            //打包数据
            ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(allOrderNos);
            if (!orderPackageRecordDtoListResponse.isSuccess()) {
                return ResponseBuilder.fail(orderPackageRecordDtoListResponse.getMessage());
            }
            List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();

            //订单单号集合,判断订单状态是否为已审单
            List<String> statusExceptionOrderNos = new ArrayList<>();
            List<String> orderNos = items.stream().filter(
                    item -> DispatchItem.orderTypeEnum.LOGISTICS_ORDER.equals(item.getOrderType())
            ).map(DispatchItem::getOrderNo).collect(Collectors.toList());

            List<LogisticsOrderAllDto> logisticsOrderAllDtos = new ArrayList<>();
            List<OrderTransfer> orderTransfers = new ArrayList<>();
            if (orderNos != null && orderNos.size() > 0) {
                ListResponse<LogisticsOrderAllDto> orderListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
                if (!orderListResponse.isSuccess()) {
                    return ResponseBuilder.fail(orderListResponse.getMessage());
                }
                logisticsOrderAllDtos = orderListResponse.getBody();

                ListResponse<OrderTransfer> orderTransferListResponse = transferService.findByOrderNosAndSiteCode(orderNos, siteCode);
                if (!orderTransferListResponse.isSuccess()) {
                    return ResponseBuilder.fail(orderTransferListResponse.getMessage());
                }
                orderTransfers = orderTransferListResponse.getBody();
                //本公司订单为已审单
                List<String> exceptionOrderNos = logisticsOrderAllDtos.stream().filter(
                        order -> !LogisticsOrder.Status.ACCEPTED.equals(order.getStatus())
                                && siteCode.equals(order.getSiteCode())
                ).map(LogisticsOrderAllDto::getOrderNo).collect(Collectors.toList());
                if (exceptionOrderNos != null && exceptionOrderNos.size() > 0) {
                    statusExceptionOrderNos.addAll(exceptionOrderNos);
                }
                //中转订单为已到货
                if (orderTransfers != null && orderTransfers.size() > 0) {
                    List<String> exceptionOrderNoList = orderTransfers.stream().filter(
                            orderTransfer -> !OrderTransfer.statusEnum.ARRIVED.equals(orderTransfer.getStatus())
                    ).map(OrderTransfer::getOrderNo).collect(Collectors.toList());
                    if (exceptionOrderNoList != null && exceptionOrderNoList.size() > 0) {
                        statusExceptionOrderNos.addAll(exceptionOrderNoList);
                    }
                }
            }
            //提货指令，判断提货指令状态是否为已确认
            List<String> instructionNos = items.stream().filter(
                    item -> DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())
            ).map(DispatchItem::getOrderNo).collect(Collectors.toList());
            List<CollectingInstruction> instructions = new ArrayList<>();
            if (instructionNos != null && instructionNos.size() > 0) {
                ListResponse<CollectingInstruction> instructionListResponse = collectingInstructionService.findByNos(instructionNos);
                if (!instructionListResponse.isSuccess()) {
                    return ResponseBuilder.fail(instructionListResponse.getMessage());
                }
                instructions = instructionListResponse.getBody();
                List<String> exceptionInstructionNos = instructions.stream().filter(
                        instruction -> !CollectingInstruction.InstructionStatus.CONFIRMED.equals(instruction.getStatus())
                ).map(CollectingInstruction::getInstructionNo).collect(Collectors.toList());
                if (exceptionInstructionNos != null && exceptionInstructionNos.size() > 0) {
                    statusExceptionOrderNos.addAll(exceptionInstructionNos);
                }
            }

            if (statusExceptionOrderNos.size() > 0) {
                return ResponseBuilder.fail(String.join("," + statusExceptionOrderNos) + "状态异常，请选择已审单的订单，已确认的提货指令已经已到货的中转订单");
            }
            List<String> dispatchPackageQtyException = new ArrayList<>();
            List<String> toDispatchingOrderNos = new ArrayList<>();//派车中订单号
            List<String> toDistributionInOrderNos = new ArrayList<>();//在途中订单号

            List<String> toDispatchingInstructionNos = new ArrayList<>();//派车中提货指令号
            List<String> toCollectingInstructionNos = new ArrayList<>();//提货中提货指令号

            List<String> toDispatchedTransferOrderNos = new ArrayList<>();//已派车的中转订单号

            for (DispatchItem item : items) {
                OrderDispatchPackageQty orderDispatchPackageQty = ObjectQuery.findOne(orderDispatchPackageQties, "orderNo", item.getOrderNo());
                Integer dispatchingPackageQty = 0;
                Integer totalPackageQty = 0;
                Integer distributionPackageQty = 0;
                Integer collectingPackageQty = 0;
                if (orderDispatchPackageQty != null) {
                    if (orderDispatchPackageQty.getDispatchingPackageQty() != null) {
                        dispatchingPackageQty = orderDispatchPackageQty.getDispatchingPackageQty();
                    }
                    if (orderDispatchPackageQty.getDistributionInPackageQty() != null) {
                        distributionPackageQty = orderDispatchPackageQty.getDistributionInPackageQty();
                    }
                    if (orderDispatchPackageQty.getCollectingPackageQty() != null) {
                        collectingPackageQty = orderDispatchPackageQty.getCollectingPackageQty();
                    }
                }


                OrderTransfer orderTransfer = null;
                if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                    CollectingInstruction instruction = ObjectQuery.findOne(instructions, "instructionNo", item.getOrderNo());
                    if (instruction != null) {
                        totalPackageQty = instruction.getConfirmedTotalPackageQty();
                    }
                } else {
                    orderTransfer = ObjectQuery.findOne(orderTransfers, "orderNo", item.getOrderNo());
                    //中转订单取中转箱数，订单取打包箱数
                    if (orderTransfer != null) {
                        totalPackageQty = orderTransfer.getPackageQuantity();
                    } else {
                        OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", item.getOrderNo());
                        if (orderPackageRecordDto != null) {
                            totalPackageQty = orderPackageRecordDto.getPackageQty();
                        }
                    }
                }

                if ((totalPackageQty - dispatchingPackageQty) < item.getPackageQuantity()) {
                    dispatchPackageQtyException.add(item.getOrderNo());
                } else if ((totalPackageQty - dispatchingPackageQty) == item.getPackageQuantity()) {
                    if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                        if (dispatchStatus != null && Dispatch.statusEnum.INTRANSIT.equals(dispatchStatus)
                                && ((totalPackageQty - collectingPackageQty) == item.getPackageQuantity())) {
                            toCollectingInstructionNos.add(item.getOrderNo());
                        } else {
                            toDispatchingInstructionNos.add(item.getOrderNo());
                        }
                    } else {
                        LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", item.getOrderNo());
                        if (orderTransfer == null) {
                            //非中转订单
                            if (dispatchStatus != null && Dispatch.statusEnum.INTRANSIT.equals(dispatchStatus)
                                    && ((totalPackageQty - distributionPackageQty) == item.getPackageQuantity())
                                    && LogisticsOrder.dispatchTypeEnum.IN_CITY.equals(logisticsOrderAllDto.getDispatchType())) {
                                toDistributionInOrderNos.add(item.getOrderNo());
                            } else {
                                toDispatchingOrderNos.add(item.getOrderNo());
                            }
                        } else {
                            //中转订订单
                            if (dispatchStatus != null && Dispatch.statusEnum.INTRANSIT.equals(dispatchStatus)
                                    && ((totalPackageQty - distributionPackageQty) == item.getPackageQuantity())
                                    && LogisticsOrder.dispatchTypeEnum.IN_CITY.name().equals(orderTransfer.getDispatchType())) {
                                toDistributionInOrderNos.add(item.getOrderNo());
                            }
                            toDispatchedTransferOrderNos.add(item.getOrderNo());
                        }
                    }
                }
            }
            if (dispatchPackageQtyException.size() > 0) {
                return ResponseBuilder.fail("订单或指令" + String.join("," + dispatchPackageQtyException) + "派车箱数有误！");
            }
            Map<String, Object> map = new HashedMap();
            map.put(TO_DISPATCHING_INSTRUCTION_NO_LIST, toDispatchingInstructionNos);
            map.put(TO_DISPATCHING_ORDER_NO_LIST, toDispatchingOrderNos);
            map.put(TO_COLLECTING_INSTRUCTION_NO_LIST, toCollectingInstructionNos);
            map.put(TO_DISTRIBUTION_IN_ORDER_NO_LIST, toDistributionInOrderNos);
            map.put(TO_DISPATCHED_TRANSFER_ORDER_NO_LIST, toDispatchedTransferOrderNos);
            return ResponseBuilder.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public ListResponse<DispatchItem> findItemsByOrderNos(String siteCode, List<String> orderNos) {
        try {
            Assert.hasText(siteCode);
            Assert.notNull(orderNos);
            return ResponseBuilder.list(dispatchItemMapper.findByOrderNosAndSiteCode(siteCode, orderNos));
        } catch (Exception e) {
            log.error(className + "findItemsByOrderNos", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public Response<Dispatch> create(Dispatch dispatch) {
        try {
            if (null == dispatch) {
                return ResponseBuilder.fail("派车单不能为空！");
            }
            if (null == dispatch.getItems() || 0 == dispatch.getItems().size()) {
                return ResponseBuilder.fail("派车单明细不能为空！");
            }
            Response judgeResponse = createOrAddItem(dispatch.getItems(), dispatch.getSiteCode(), null);
            if (!judgeResponse.isSuccess()) {
                return judgeResponse;
            }
            Map<String, Object> map = (Map<String, Object>) judgeResponse.getBody();
            List<String> toDispatchingInstructionNos = (List<String>) map.get(TO_DISPATCHING_INSTRUCTION_NO_LIST);
            List<String> toDispatchingOrderNos = (List<String>) map.get(TO_DISPATCHING_ORDER_NO_LIST);
            List<String> toDispatchedTransferOrderNos = (List<String>) map.get(TO_DISPATCHED_TRANSFER_ORDER_NO_LIST);
            //创建派车单
            List<DispatchFeeDetail> feeDetails = dispatch.getFeeDetails();
            BigDecimal totalFee = new BigDecimal(0);
            if (feeDetails != null && feeDetails.size() > 0) {
                for (DispatchFeeDetail feeDetail : feeDetails) {
                    totalFee = totalFee.add(feeDetail.getAmount() == null ? new BigDecimal(0) : feeDetail.getAmount());
                }
            }
            dispatch.setTotalFee(totalFee);
            Response<Dispatch> response = createDispatch(dispatch);
            if (!response.isSuccess()) {
                return response;
            }
            //修改车辆状态
            Response vehicleStatusChangeResponse = vehicleService.changeStatusToBusy(dispatch.getVehicleNumber());
            // 派车单创建成功后，给司机发送通知
            Response<VehicleListDto> vehicleListDtoResponse = vehicleService.get(dispatch.getVehicleId());
            if (!vehicleListDtoResponse.isSuccess()) {
                return ResponseBuilder.fail(vehicleListDtoResponse.getMessage());
            }
            //// TODO: 2017/1/6
           /* if (null != vehicleListDtoResponse && StringUtils.hasText(vehicleListDtoResponse.getBody().getUserId())) {
                Response<User> userResponse = userService.get(vehicleListDtoResponse.getBody().getUserId());
                if ((userResponse.isSuccess()) && (null != userResponse.getBody()) && (StringUtils.hasText(userResponse.getBody().getUsername()))) {
                    notificationService.send("出车任务", "收到派车任务，派车单单号：" + response.getBody().getDispatchNumber(), dispatch.getCreatedBy(), userResponse.getBody().getUsername());
                }
            }*/
            if (toDispatchingOrderNos != null && toDispatchingOrderNos.size() > 0) {
                Response toDispatchingResponse = logisticsOrderService.toDispatching(toDispatchingOrderNos, dispatch.getCreatedBy());
                if (!toDispatchingResponse.isSuccess()) {
                    return toDispatchingResponse;
                }
            }
            if (toDispatchingInstructionNos != null && toDispatchingInstructionNos.size() > 0) {
                Response toDispatchingResponse = collectingInstructionService.toDispatching(toDispatchingInstructionNos);
                if (!toDispatchingResponse.isSuccess()) {
                    return toDispatchingResponse;
                }
            }
            if (toDispatchedTransferOrderNos != null && toDispatchedTransferOrderNos.size() > 0) {
                Response toDispatchedResponse = transferService.toDispatched(dispatch.getSiteCode(), toDispatchedTransferOrderNos);
                if (!toDispatchedResponse.isSuccess()) {
                    return toDispatchedResponse;
                }
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<Dispatch> get(String dispatchId) {
        try {
            Assert.hasText(dispatchId);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch != null) {
                List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatchId);
                List<DispatchFollow> follows = dispatchFollowMapper.findByDispatchId(dispatchId);
                List<DispatchLog> logs = dispatchLogMapper.findByDispatchId(dispatchId);
                List<DispatchPackage> packages = dispatchPackageMapper.findByDispatchId(dispatchId);
                List<DispatchFeeDetail> feeDetails = feeDetailMapper.findByDispatchId(dispatchId);
                dispatch.setItems(items);
                dispatch.setFollows(follows);
                dispatch.setLogs(logs);
                dispatch.setPackages(packages);
                dispatch.setFeeDetails(feeDetails);
                return ResponseBuilder.success(dispatch);
            }
            return ResponseBuilder.fail("派车单不存在！");
        } catch (Exception e) {
            log.error(className + "get", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> addOrders(DispatchOperateModel model) {
        try {
            Assert.hasText(model.getDispatchId());
            Assert.notEmpty(model.getItems());

            Dispatch dispatch = dispatchMapper.get(model.getDispatchId());
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            if (Dispatch.statusEnum.CANCELED.equals(status) || Dispatch.statusEnum.FINISHED.equals(status)) {
                return ResponseBuilder.fail("派车单已完成或已取消，不能操作加单！");
            }
            model.getItems().stream().forEach(item -> {
                item.setDispatchId(dispatch.getDispatchId());
                item.setDispatchItemId(Snowflake.getInstance().next());
                //修改派车单总体积、总重量、总箱数
                dispatch.setTotalPackageQuantity(dispatch.getTotalPackageQuantity() + (item.getPackageQuantity() == null ? 0 : item.getPackageQuantity()));
                dispatch.setTotalVolume(dispatch.getTotalVolume().add(item.getVolume() == null ? new BigDecimal(0) : item.getVolume()));
                dispatch.setTotalWeight(dispatch.getTotalWeight().add(item.getWeight() == null ? new BigDecimal(0) : item.getWeight()));
            });
            if (model.getPackages() != null && model.getPackages().size() > 0) {
                model.getPackages().stream().forEach(dispatchPackage -> {
                    dispatchPackage.setDispatchId(dispatch.getDispatchId());
                    dispatchPackage.setDispatchPackageId(Snowflake.getInstance().next());
                });
            }
            dispatch.setModifiedBy(model.getOperatorId());
            dispatch.setModifiedDate(LocalDateTime.now());

            Assert.isTrue(dispatchItemMapper.batchInsert(model.getItems()) > 0);
            if (model.getPackages() != null && model.getPackages().size() > 0) {
                Assert.isTrue(dispatchPackageMapper.batchInsert(model.getPackages()) > 0);
            }
            Assert.isTrue(dispatchMapper.update(dispatch) > 0);

            List<DispatchItem> items = model.getItems();
            List<String> orderNos = items.stream().map(DispatchItem::getOrderNo).collect(Collectors.toList());

            DispatchLog log = this.buildLog(dispatch, model.getOperatorId(), "加单" + String.join(",", orderNos));
            Assert.isTrue(dispatchLogMapper.insert(log) > 0);

            //region派车箱数
            List<OrderDispatchPackageQty> orderDispatchPackageQtyList = orderDispatchPackageQtyMapper.findByOrderNos(dispatch.getSiteCode(), orderNos);
            Map<String, Object> map = this.getCreateOrReplace(items, orderDispatchPackageQtyList, dispatch.getSiteCode(), dispatch.getBranchCode());
            List<OrderDispatchPackageQty> addList = (List<OrderDispatchPackageQty>) map.get("addList");
            List<OrderDispatchPackageQty> updateList = (List<OrderDispatchPackageQty>) map.get("updateList");
            if (addList.size() > 0) {
                Assert.isTrue(orderDispatchPackageQtyMapper.batchInsert(addList) == addList.size());
            }
            if (updateList.size() > 0) {
                orderDispatchPackageQtyMapper.batchUpdateDispatchingQty(updateList);
            }
            //如果派车单已在途，则修改订单的在途箱数和提货指令的在提箱数
            if (Dispatch.statusEnum.INTRANSIT.equals(dispatch.getStatus())) {
                List<OrderDispatchPackageQty> updateCollectingQty = new ArrayList<>();
                List<OrderDispatchPackageQty> updateDistributionInQty = new ArrayList<>();
                List<OrderDispatchPackageQty> orderDispatchPackageQties = orderDispatchPackageQtyMapper.findByOrderNos(dispatch.getSiteCode(), orderNos);
                items.stream().forEach(item -> {
                    Optional<OrderDispatchPackageQty> optional = orderDispatchPackageQties.stream().filter(
                            orderDispatchPackageQty -> orderDispatchPackageQty.getOrderNo().equals(item.getOrderNo())
                    ).findFirst();
                    Assert.isTrue(optional.isPresent());
                    OrderDispatchPackageQty orderDispatchPackageQty = optional.get();
                    if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                        orderDispatchPackageQty.setCollectingPackageQty(
                                (orderDispatchPackageQty.getCollectingPackageQty() == null ? 0 : orderDispatchPackageQty.getCollectingPackageQty()) + item.getPackageQuantity());
                        updateCollectingQty.add(orderDispatchPackageQty);
                    } else {
                        orderDispatchPackageQty.setDistributionInPackageQty(
                                (orderDispatchPackageQty.getDistributionInPackageQty() == null ? 0 : orderDispatchPackageQty.getDistributionInPackageQty()) + item.getPackageQuantity());
                        updateDistributionInQty.add(orderDispatchPackageQty);
                    }
                });
                if (updateCollectingQty.size() > 0) {
                    orderDispatchPackageQtyMapper.batchUpdateCollectingQty(updateCollectingQty);
                }
                if (updateDistributionInQty.size() > 0) {
                    orderDispatchPackageQtyMapper.batchUpdateDistributionInQty(updateDistributionInQty);
                }
            }
            //endregion
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "addOrders", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> removeOrders(String dispatchId, List<String> orderNos, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Assert.notEmpty(orderNos);

            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            if (Dispatch.statusEnum.CANCELED.equals(status) || Dispatch.statusEnum.FINISHED.equals(status)) {
                return ResponseBuilder.fail("派车单已完成或已取消，不能操作减单！");
            }

            //判断要减掉的订单是否存在当前派车单中
            List<DispatchItem> oldItems = dispatchItemMapper.findByDispatchId(dispatchId);
            List<DispatchItem> deleteItems = new ArrayList<>();
            List<String> notOrderNos = new ArrayList<>();
            for (String orderNo : orderNos) {
                Boolean isExists = false;
                for (DispatchItem item : oldItems) {
                    if (orderNo.equals(item.getOrderNo())) {
                        isExists = true;
                        deleteItems.add(item);
                        break;
                    }
                }
                if (!isExists) {
                    notOrderNos.add(orderNo);
                }
            }
            if (notOrderNos.size() > 0) {
                return ResponseBuilder.fail("订单" + String.join(",", notOrderNos) + "不存在于派车单中！");
            }
            //删除派车单订单明细
            List<String> deleteItemIds = new ArrayList<>();
            deleteItems.stream().forEach(item -> {
                deleteItemIds.add(item.getDispatchItemId());
                dispatch.setTotalPackageQuantity(dispatch.getTotalPackageQuantity() - (item.getPackageQuantity() == null ? 0 : item.getPackageQuantity()));
                dispatch.setTotalVolume(dispatch.getTotalVolume().subtract(item.getVolume() == null ? new BigDecimal(0) : item.getVolume()));
                dispatch.setTotalWeight(dispatch.getTotalWeight().subtract(item.getWeight() == null ? new BigDecimal(0) : item.getWeight()));
            });
            //删除派车单包明细
            List<String> deletePackageIds = new ArrayList<>();
            List<DispatchPackage> dispatchPackages = dispatchPackageMapper.findByDispatchId(dispatchId);
            if (dispatchPackages != null && dispatchPackages.size() > 0) {
                for (DispatchPackage dispatchPackage : dispatchPackages) {
                    Boolean isDelete = false;
                    for (String orderNo : orderNos) {
                        if (dispatchPackage.getOrderNo().equals(orderNo)) {
                            isDelete = true;
                            break;
                        }
                    }
                    if (isDelete) {
                        deletePackageIds.add(dispatchPackage.getDispatchPackageId());
                    }
                }
            }
            if (deletePackageIds.size() > 0) {
                Assert.isTrue(dispatchPackageMapper.batchDelete(deletePackageIds) > 0);
            }
            Assert.isTrue(dispatchItemMapper.batchDelete(deleteItemIds) > 0);

            dispatch.setModifiedBy(operatorId);
            dispatch.setModifiedDate(LocalDateTime.now());

            Assert.isTrue(dispatchMapper.update(dispatch) > 0);
            DispatchLog log = this.buildLog(dispatch, operatorId, "减单" + String.join(",", orderNos));
            Assert.isTrue(dispatchLogMapper.insert(log) > 0);
            //修改派车箱数
            List<OrderDispatchPackageQty> orderDispatchPackageQties = orderDispatchPackageQtyMapper.findByOrderNos(dispatch.getSiteCode(), orderNos);
            orderDispatchPackageQties.stream().forEach(orderDispatchPackageQty -> {
                Optional<DispatchItem> optional = deleteItems.stream().filter(item ->
                        item.getOrderNo().equals(orderDispatchPackageQty.getOrderNo())
                ).findFirst();
                Assert.isTrue(optional.isPresent());
                DispatchItem item = optional.get();
                orderDispatchPackageQty.setDispatchingPackageQty(orderDispatchPackageQty.getDispatchingPackageQty() - item.getPackageQuantity());
                if (Dispatch.statusEnum.INTRANSIT.equals(dispatch.getStatus())) {
                    if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                        orderDispatchPackageQty.setCollectingPackageQty(orderDispatchPackageQty.getCollectingPackageQty() - item.getPackageQuantity());
                    } else {
                        orderDispatchPackageQty.setDistributionInPackageQty(orderDispatchPackageQty.getDistributionInPackageQty() - item.getPackageQuantity());
                    }
                }
            });
            orderDispatchPackageQtyMapper.batchUpdate(orderDispatchPackageQties);
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "removeOrders", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> assign(Dispatch dispatch) {
        try {
            Assert.notNull(dispatch);
            Assert.hasText(dispatch.getDriver());
            Assert.hasText(dispatch.getDriverPhone());
            Dispatch dispatch1 = dispatchMapper.get(dispatch.getDispatchId());
            if (dispatch1 == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch1.getStatus();
            if (!Dispatch.statusEnum.NEW.equals(status) && !Dispatch.statusEnum.ASSIGNED.equals(status)) {
                return ResponseBuilder.fail("派车单司机已接收或已取消，不能操作指派！");
            }
            dispatch.setStatus(Dispatch.statusEnum.ASSIGNED);
            dispatch.setAssignDate(LocalDateTime.now());
            Assert.isTrue(dispatchMapper.update(dispatch) > 0);
            DispatchLog log = this.buildLog(dispatch, dispatch.getModifiedBy(), "指派司机");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0);
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "assign", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<List<Dispatch>> findForDriver(String vehicleId, Dispatch.statusEnum status) {
        try {
            Assert.hasText(vehicleId);
            List<Dispatch> dispatches = dispatchMapper.getByVehicleIdAndStatus(vehicleId, status);
            return ResponseBuilder.success(dispatches);
        } catch (Exception e) {
            log.error(className + "findForDriver", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public PageResponse<DispatchDto> pageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<DispatchDto> list = dispatchMapper.pageList(params);
            PageInfo pageInfo = new PageInfo<>(list);

            if (list != null && list.size() > 0) {
                ListResponse<User> userListResponse = userService.getByOrgCode((String) params.get("branchCode"));
                if (!userListResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(userListResponse.getMessage());
                }
                List<User> userList = userListResponse.getBody();
                list.stream().forEach(dispatchDto -> {
                    if (dispatchDto.getCreatedBy() != null) {
                        User user = ObjectQuery.findOne(userList, "userId", dispatchDto.getCreatedBy());
                        if (user != null) {
                            dispatchDto.setCreateUserName(user.getFullName());
                        }
                    }
                });
            }
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    @Override
    public Response<Dispatch> accept(String dispatchId, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            if (!Dispatch.statusEnum.ASSIGNED.equals(dispatch.getStatus())) {
                return ResponseBuilder.fail("派车单未派车或已接受、已取消，不能操作接受！");
            }
            //TODO 判断接受操作人是否为当前派车单的司机 APPLICATION
            dispatch.setStatus(Dispatch.statusEnum.ACCEPT);
            dispatch.setAcceptDate(LocalDateTime.now());
            Assert.isTrue(dispatchMapper.update(dispatch) > 0);
            DispatchLog log = this.buildLog(dispatch, operatorId, "接受");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0);
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "accept", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> reject(String dispatchId, String operatorId, String notes) {
        try {
            Assert.hasText(dispatchId);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            if (!Dispatch.statusEnum.ASSIGNED.equals(dispatch.getStatus())) {
                return ResponseBuilder.fail("派车单未派车或已接受、已取消，不能操作拒绝！");
            }
            //TODO 判断操作人是否为当前派车单的司机 APPLICATION
            dispatch.setStatus(Dispatch.statusEnum.NEW);
            Assert.isTrue(dispatchMapper.update(dispatch) > 0);
            DispatchLog log = this.buildLog(dispatch, operatorId, "拒绝：" + notes);
            Assert.isTrue(dispatchLogMapper.insert(log) > 0);
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "reject", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> loadOrders(String dispatchId, List<String> orderNos, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Assert.notEmpty(orderNos);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            Assert.notNull(status);
            if (!Dispatch.statusEnum.ASSIGNED.equals(status) && !Dispatch.statusEnum.ACCEPT.equals(status)
                    && !Dispatch.statusEnum.LOADING.equals(status)) {
                return ResponseBuilder.fail("请选择已派车、已接受或装车中的派车单！");
            }
            List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatchId);
            List<DispatchPackage> packages = dispatchPackageMapper.findByDispatchId(dispatchId);
            Assert.notEmpty(items);
            //校验装车订单是否存在于当前派车单中
            List<String> notOrderNos = new ArrayList<>();
            for (String orderNo : orderNos) {
                boolean isExistItem = false;
                for (DispatchItem item : items) {
                    if (item.getOrderNo().equals(orderNo)) {
                        isExistItem = true;
                        break;
                    }
                }
                if (!isExistItem) {
                    notOrderNos.add(orderNo);
                }
            }
            if (notOrderNos.size() > 0) {
                return ResponseBuilder.fail("订单" + String.join(",", notOrderNos) + "不存在于当前派车单中！");
            }
            //标记订单明细、包明细已装车
            for (DispatchItem item : items) {
                Boolean isLoaded = false;
                for (String orderNo : orderNos) {
                    if (item.getOrderNo().equals(orderNo)) {
                        isLoaded = true;
                        break;
                    }
                }
                if (isLoaded) {
                    //标记订单已装车
                    item.setIsLoaded(true);
                    //标记包明细已装车
                    if (packages != null && packages.size() > 0) {
                        for (DispatchPackage dispatchPackage : packages) {
                            if (dispatchPackage.getOrderNo().equals(item.getOrderNo())) {
                                dispatchPackage.setIsLoaded(true);
                            }
                        }
                    }
                }
            }
            dispatchItemMapper.batchUpdate(items);
            dispatch.setStatus(Dispatch.statusEnum.LOADING);
            if (packages != null && packages.size() > 0) {
                dispatchPackageMapper.batchUpdate(packages);
            }
            //判断是否所有明细已装车
            Boolean isAllLoaded = true;
            for (DispatchItem item : items) {
                if (item.getIsLoaded() == null || !item.getIsLoaded()) {
                    isAllLoaded = false;
                    break;
                }
            }
            if (isAllLoaded) {
                dispatch.setStatus(Dispatch.statusEnum.LOADED);
            }
            Assert.isTrue(dispatchMapper.update(dispatch) > 0, "更新派车单失败");

            DispatchLog log = buildLog(dispatch, operatorId, "装车");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "新增派车单日志失败");

            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "loadOrders", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> loadPackages(String dispatchId, List<String> packageNos, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Assert.notEmpty(packageNos);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            Assert.notNull(status);
            if (!Dispatch.statusEnum.ASSIGNED.equals(status) && !Dispatch.statusEnum.ACCEPT.equals(status)
                    && !Dispatch.statusEnum.LOADING.equals(status)) {
                return ResponseBuilder.fail("请选择已派车、已接受或装车中的派车单！");
            }
            List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatchId);
            List<DispatchPackage> packages = dispatchPackageMapper.findByDispatchId(dispatchId);
            Assert.notEmpty(items, "当前派车单订单明细为空");
            Assert.notEmpty(packageNos, "当前派车单包明细为空");
            //校验装车订单是否存在于当前派车单中
            List<String> notPackageNos = new ArrayList<>();
            for (String packageNo : packageNos) {
                boolean isExist = false;
                for (DispatchPackage dispatchPackage : packages) {
                    if (dispatchPackage.getPackageNo().equals(packageNo)) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    notPackageNos.add(packageNo);
                }
            }
            if (notPackageNos.size() > 0) {
                return ResponseBuilder.fail("箱" + String.join(",", notPackageNos) + "不存在于当前派车单中！");
            }

            for (DispatchPackage dispatchPackage : packages) {
                boolean isLoaded = false;
                for (String packageNo : packageNos) {
                    if (packageNo.equals(dispatchPackage.getPackageNo())) {
                        isLoaded = true;
                        break;
                    }
                }
                if (isLoaded) {
                    dispatchPackage.setIsLoaded(true);
                }
            }
            //判断订单的包是否全部装车
            boolean isAllLoaded = true;
            for (DispatchItem item : items) {
                boolean isLoaded = true;
                for (DispatchPackage dispatchPackage : packages) {
                    if (item.getOrderNo().equals(dispatchPackage.getOrderNo())) {
                        if (dispatchPackage.getIsLoaded() == null || !dispatchPackage.getIsLoaded()) {
                            isLoaded = false;
                            break;
                        }
                    }
                }
                if (isLoaded) {
                    item.setIsLoaded(true);
                } else {
                    isAllLoaded = false;
                }
            }
            dispatchPackageMapper.batchUpdate(packages);
            dispatchItemMapper.batchUpdate(items);
            //是否全部已装车
            if (isAllLoaded) {
                dispatch.setStatus(Dispatch.statusEnum.LOADED);
            } else {
                dispatch.setStatus(Dispatch.statusEnum.LOADING);
            }
            Assert.isTrue(dispatchMapper.update(dispatch) > 0, "更新派车单失败");
            DispatchLog log = buildLog(dispatch, operatorId, "装车");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "新增派车单日志失败");
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "loadPackages", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> finishLoading(String dispatchId, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            Assert.notNull(status);
            if (!Dispatch.statusEnum.LOADING.equals(status)) {
                return ResponseBuilder.fail("请选择装车中的派车单！");
            }
            /*List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatchId);
            List<DispatchPackage> packages = dispatchPackageMapper.findByDispatchId(dispatchId);
            Assert.notEmpty(items);

            List<String> notLoadedOrderNos = new ArrayList<>();
            List<String> notLoadedItemIds = new ArrayList<>();
            List<DispatchItem> deleteItems = new ArrayList<>();
            for (DispatchItem item : items) {
                if (item.getIsLoaded() == null || !item.getIsLoaded()) {
                    notLoadedOrderNos.add(item.getOrderNo());
                    notLoadedItemIds.add(item.getDispatchItemId());
                    deleteItems.add(item);
                }
            }
            if (notLoadedItemIds.size() > 0) {
                if (packages != null && packages.size() > 0) {
                    //校验是否存在未装车的订单有一部分箱已装车，如有提示异常
                    List<String> exceptionPackageNos = new ArrayList<>();
                    List<String> notLoadPackageIds = new ArrayList<>();
                    for (DispatchPackage dispatchPackage : packages) {
                        if (notLoadedOrderNos.contains(dispatchPackage.getOrderNo()) && dispatchPackage.getIsLoaded() != null && dispatchPackage.getIsLoaded()) {
                            exceptionPackageNos.add(dispatchPackage.getPackageNo());
                        }
                        if (dispatchPackage.getIsLoaded() == null || !dispatchPackage.getIsLoaded()) {
                            notLoadPackageIds.add(dispatchPackage.getDispatchPackageId());
                        }
                    }
                    if (exceptionPackageNos.size() > 0) {
                        return ResponseBuilder.fail("未装车订单中，箱号" + String.join("，", exceptionPackageNos) + "已扫描装车");
                    }
                    Assert.isTrue(dispatchPackageMapper.batchDelete(notLoadPackageIds) > 0, "删除未装车箱失败");
                }
                Assert.isTrue(dispatchItemMapper.batchDelete(notLoadedItemIds) > 0, "删除未装车明细失败");
            }
            deleteItems.stream().forEach(item -> {
                dispatch.setTotalPackageQuantity(dispatch.getTotalPackageQuantity() - (item.getPackageQuantity() == null ? 0 : item.getPackageQuantity()));
                dispatch.setTotalVolume(dispatch.getTotalVolume().subtract(item.getVolume() == null ? new BigDecimal(0) : item.getVolume()));
                dispatch.setTotalWeight(dispatch.getTotalWeight().subtract(item.getWeight() == null ? new BigDecimal(0) : item.getWeight()));
            });
            */
            dispatch.setStatus(Dispatch.statusEnum.LOADED);
            Assert.isTrue(dispatchMapper.update(dispatch) > 0, "更新派车单失败");

            DispatchLog log = buildLog(dispatch, operatorId, "完成装车");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "新增派车单日志失败");
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "finishLoad", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> start(String dispatchId, LocalDateTime startTime, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            Assert.notNull(status);
            if (!Dispatch.statusEnum.ASSIGNED.equals(status)
                    && !Dispatch.statusEnum.ACCEPT.equals(status)
                    && !Dispatch.statusEnum.LOADING.equals(status)
                    && !Dispatch.statusEnum.LOADED.equals(status)) {
                return ResponseBuilder.fail("请选择已指派司机且未发车的派车单！");
            }
            List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatchId);
            Assert.notEmpty(items);

            dispatch.setStatus(Dispatch.statusEnum.INTRANSIT);
            dispatch.setStartDate(startTime);
            Assert.isTrue(dispatchMapper.update(dispatch) > 0, "更新派车单失败");

            DispatchLog log = buildLog(dispatch, operatorId, "发车");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "新增派车单日志失败");
            //修改派车箱数信息
            List<String> orderNos = items.stream().map(DispatchItem::getOrderNo).collect(Collectors.toList());
            List<OrderDispatchPackageQty> orderDispatchPackageQties = orderDispatchPackageQtyMapper.findByOrderNos(dispatch.getSiteCode(), orderNos);
            orderDispatchPackageQties.stream().forEach(orderDispatchPackageQty -> {
                Optional<DispatchItem> optional = items.stream().filter(item -> item.getOrderNo().equals(orderDispatchPackageQty.getOrderNo())).findFirst();
                Assert.isTrue(optional.isPresent());
                DispatchItem item = optional.get();
                if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                    orderDispatchPackageQty.setCollectingPackageQty(
                            (orderDispatchPackageQty.getCollectingPackageQty() == null ? 0 : orderDispatchPackageQty.getCollectingPackageQty()) + item.getPackageQuantity());
                } else {
                    orderDispatchPackageQty.setDistributionInPackageQty(
                            (orderDispatchPackageQty.getDistributionInPackageQty() == null ? 0 : orderDispatchPackageQty.getDistributionInPackageQty()) + item.getPackageQuantity());
                }
            });
            orderDispatchPackageQtyMapper.batchUpdate(orderDispatchPackageQties);
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "start", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> finish(String dispatchId, List<String> finishOrderNos, List<String> notFinishOrderNos, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            Assert.notNull(status);
            if (!Dispatch.statusEnum.INTRANSIT.equals(status)) {
                return ResponseBuilder.fail("请选择在途中的派车单！");
            }
            dispatch.setStatus(Dispatch.statusEnum.FINISHED);
            dispatch.setFinishedDate(LocalDateTime.now());
            Assert.isTrue(dispatchMapper.update(dispatch) > 0, "更新派车单失败");

            DispatchLog log = buildLog(dispatch, operatorId, "完成");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "新增派车单日志失败");
            //更新派车箱数
            List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatchId);
            List<String> orderNos = items.stream().map(item -> item.getOrderNo()).collect(Collectors.toList());
            List<OrderDispatchPackageQty> packageQties = orderDispatchPackageQtyMapper.findByOrderNos(dispatch.getSiteCode(), orderNos);

            List<OrderDispatchPackageQty> updatePackageQtyList = new ArrayList<>();
            if (finishOrderNos != null && finishOrderNos.size() > 0) {
                finishOrderNos.stream().forEach(orderNo -> {
                    Optional<OrderDispatchPackageQty> packageQtyOptional = packageQties.stream().filter(packageQty -> packageQty.getOrderNo().equals(orderNo)).findFirst();
                    Assert.isTrue(packageQtyOptional.isPresent());
                    OrderDispatchPackageQty packageQty = packageQtyOptional.get();

                    Optional<DispatchItem> itemOptional = items.stream().filter(item -> item.getOrderNo().equals(orderNo)).findFirst();
                    Assert.isTrue(itemOptional.isPresent());
                    DispatchItem item = itemOptional.get();

                    if (item != null && DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                        packageQty.setCollectedPackageQty((packageQty.getCollectedPackageQty() == null ? 0 : packageQty.getCollectedPackageQty()) + item.getPackageQuantity());
                    }
                    updatePackageQtyList.add(packageQty);
                });
            }
            if (notFinishOrderNos != null && notFinishOrderNos.size() > 0) {
                notFinishOrderNos.stream().forEach(orderNo -> {
                    Optional<OrderDispatchPackageQty> packageQtyOptional = packageQties.stream().filter(packageQty -> packageQty.getOrderNo().equals(orderNo)).findFirst();
                    Assert.isTrue(packageQtyOptional.isPresent());
                    OrderDispatchPackageQty packageQty = packageQtyOptional.get();

                    Optional<DispatchItem> itemOptional = items.stream().filter(item -> item.getOrderNo().equals(orderNo)).findFirst();
                    Assert.isTrue(itemOptional.isPresent());
                    DispatchItem item = itemOptional.get();

                    packageQty.setDispatchingPackageQty(packageQty.getDispatchingPackageQty() - item.getPackageQuantity());
                    if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                        packageQty.setCollectingPackageQty(packageQty.getCollectingPackageQty() - item.getPackageQuantity());
                    } else {
                        packageQty.setDistributionInPackageQty(packageQty.getDistributionInPackageQty() - item.getPackageQuantity());
                    }
                    updatePackageQtyList.add(packageQty);
                });
            }
            if (updatePackageQtyList.size() > 0) {
                orderDispatchPackageQtyMapper.batchUpdate(updatePackageQtyList);
            }
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "finish", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> cancel(String dispatchId, String notes, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            Assert.notNull(status);
            if (Dispatch.statusEnum.FINISHED.equals(status)) {
                return ResponseBuilder.fail("派车单已完成！");
            }
            if (Dispatch.statusEnum.CANCELED.equals(status)) {
                return ResponseBuilder.fail("派车单已取消！");
            }
            dispatch.setStatus(Dispatch.statusEnum.CANCELED);
            Assert.isTrue(dispatchMapper.update(dispatch) > 0, "更新派车单失败");

            List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatchId);

            Assert.isTrue(dispatchItemMapper.deleteByDispatchId(dispatchId) > 0, "删除派车单明细失败");
            Assert.isTrue(dispatchPackageMapper.deleteByDispatchId(dispatchId) >= 0, "删除派车单包明细失败");

            DispatchLog log = buildLog(dispatch, operatorId, "取消：" + notes);
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "新增派车单日志失败");
            //修改派车箱数
            List<String> orderNos = items.stream().map(DispatchItem::getOrderNo).collect(Collectors.toList());
            List<OrderDispatchPackageQty> orderDispatchPackageQtyList = orderDispatchPackageQtyMapper.findByOrderNos(dispatch.getSiteCode(), orderNos);
            orderDispatchPackageQtyList.stream().forEach(orderDispatchPackageQty -> {
                Optional<DispatchItem> optional = items.stream().filter(item ->
                        item.getOrderNo().equals(orderDispatchPackageQty.getOrderNo())
                ).findFirst();
                Assert.isTrue(optional.isPresent());
                DispatchItem item = optional.get();
                orderDispatchPackageQty.setDispatchingPackageQty(orderDispatchPackageQty.getDispatchingPackageQty() - item.getPackageQuantity());
                if (Dispatch.statusEnum.INTRANSIT.equals(status)) {
                    if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(item.getOrderType())) {
                        orderDispatchPackageQty.setCollectingPackageQty(orderDispatchPackageQty.getCollectingPackageQty() - item.getPackageQuantity());
                    } else {
                        orderDispatchPackageQty.setDistributionInPackageQty(orderDispatchPackageQty.getDistributionInPackageQty() - item.getPackageQuantity());
                    }
                }
            });
            orderDispatchPackageQtyMapper.batchUpdate(orderDispatchPackageQtyList);
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "cancel", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<Dispatch> searchDispatch(String branchCode, String siteCode, String condition, String driverId, String assignDate) {
        try {
            return ResponseBuilder.list(dispatchMapper.searchDispatch(branchCode, siteCode, condition, driverId, assignDate));
        } catch (Exception e) {
            log.error(className + "searchDispatch", e);
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<DispatchItem> findItems(String dispatchId) {
        try {
            return ResponseBuilder.list(dispatchItemMapper.findByDispatchId(dispatchId));
        } catch (Exception e) {
            log.error(className + "findItems", e);
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<DispatchItem> findItems(List<String> dispatchIds) {
        try {
            return ResponseBuilder.list(dispatchItemMapper.findByDispatchIds(dispatchIds));
        } catch (Exception e) {
            log.error(className + "findItems", e);
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<OrderDispatchPackageQty> findOrderDispatchPackageQty(String siteCode, List<String> orderNos) {
        try {
            Assert.notEmpty(orderNos);
            Assert.notNull(siteCode);
            return ResponseBuilder.list(orderDispatchPackageQtyMapper.findByOrderNos(siteCode, orderNos));
        } catch (Exception e) {
            log.error(className + "findOrderDispatchPackageQty", e);
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<DispatchMonthDto> searchGroupByMonth(String createdBy) {
        try {
            Assert.hasText(createdBy);
            return ResponseBuilder.list(dispatchMapper.searchGroupByMonth(createdBy));
        } catch (Exception e) {
            log.error(className + "searchGroupByMonth", e);
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public Response getTodayDispatch(String branchCode, String siteCode) {
        try {
            Integer dispatchCount = dispatchMapper.getTodayDispatch(branchCode, siteCode);
            return ResponseBuilder.success(dispatchCount);
        } catch (Exception e) {
            log.error(className + "getTodayDispatch", e);
            return ResponseBuilder.fail(e);
        }
    }

    private void setLogOperator(Dispatch dispatch) {
        List<User> userList = userService.getByOrgCode(dispatch.getBranchCode()).getBody();
        if (userList != null)
            dispatch.getLogs().stream().forEach(log -> {
                if (log.getOperatorId() != null) {
                    User user = ObjectQuery.findOne(userList, "userId", log.getOperatorId());
                    if (user != null) {
                        log.setOperator(user.getFullName());
                    }
                }
            });
    }

    @Override
    public Response<Map<String, Object>> getMap(String dispatchId) {
        try {
            Map<String, Object> map = new HashedMap();
            if (!StringUtils.hasText(dispatchId)) {
                return ResponseBuilder.fail("派车单ID不能为空");
            }
            Response<Dispatch> dispatchResponse = new Response<>();
            dispatchResponse = get(dispatchId);
            if (dispatchResponse.isSuccess() && null == dispatchResponse.getBody()) {
                dispatchResponse = getByNo(dispatchId);
            }
            if (!dispatchResponse.isSuccess()) {
                return ResponseBuilder.fail(dispatchResponse.getMessage());
            }
            Dispatch dispatch = dispatchResponse.getBody();
            setLogOperator(dispatch);
            DispatchDto dispatchDto = BeanHelper.convert(dispatch, DispatchDto.class);
            List<User> users = userService.getByOrgCode(dispatch.getBranchCode()).getBody();
            if (users != null) {
                User user = ObjectQuery.findOne(users, "userId", dispatchDto.getCreatedBy());
                if (user != null) {
                    dispatchDto.setCreateUserName(user.getFullName());
                }
            }
            map.put("dispatch", dispatchDto);
            map.put("dispatchItemDtoList", dispatchItemConvert(dispatch.getItems(), dispatch.getSiteCode()));
            map.put("feeDetailDtos", dispatchFeeDetailConvert(dispatch.getFeeDetails()));
            return ResponseBuilder.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response getOrderCount(String branchCode, String siteCode) {
        try {
            if (!StringUtils.hasText(branchCode)) {
                return ResponseBuilder.fail("分支编码为空！");
            }
            if (!StringUtils.hasText(siteCode)) {
                return ResponseBuilder.fail("站点编码为空！");
            }
            Map<String, Object> result = new HashMap();
            result.put("needConsign", logisticsOrderService.consignQueryCount(branchCode, siteCode).getBody());
            result.put("needDispatch", logisticsOrderService.dispatchQueryCount(branchCode, siteCode).getBody());
            result.put("todayOrder", logisticsOrderService.getTodayOrderCount(branchCode, siteCode).getBody());
            result.put("todayDispatch", getTodayDispatch(branchCode, siteCode).getBody());
            result.put("todayPackage", packageService.getTodayPackage(branchCode, siteCode).getBody());
            result.put("todayConsign", consignOrderService.getTodayConsignCount(branchCode, siteCode).getBody());
            return ResponseBuilder.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public ListResponse<Dispatch> findByDrivers(List<String> vehicleIds, Dispatch.statusEnum status) {
        try {
            Assert.notEmpty(vehicleIds);
            Assert.notNull(status);
            List<Dispatch> dispatches = dispatchMapper.getByVehicleIdsAndStatus(vehicleIds, status);
            return ResponseBuilder.list(dispatches);
        } catch (Exception e) {
            log.error(className + "findByDrivers", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }

    }

    @Override
    public PageResponse<DispatchPayableDto> searchDispatchPay(Integer page, Integer pageSize, Map<String, Object> filterMap) {
        try {
            PageHelper.startPage(page, pageSize);
            List<DispatchDto> list = dispatchMapper.pageList(filterMap);
            PageInfo pageInfo = new PageInfo(list);
            List<DispatchPayableDto> result = convertPayDto(list, (String) filterMap.get("branchCode"));
            return ResponseBuilder.page(result, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    @Override
    public PageResponse<DispatchQueryDto> dispatchQueryPageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            PageResponse<DispatchQueryDto> pageResponse = logisticsOrderService.dispatchQueryPageList(page, pageSize, params);
            List<DispatchQueryDto> dispatchQueryDtos = pageResponse.getBody();
            if (dispatchQueryDtos != null && dispatchQueryDtos.size() > 0) {
                //物流订单如有打包，则将打包信息填充到DTO上
                List<String> orderNos = dispatchQueryDtos.stream().map(dto -> dto.getOrderNo()).collect(Collectors.toList());
                ListResponse<OrderPackageRecordDto> orderPackageRecordDtoListResponse = packageService.findOrderPacking(orderNos);
                if (!orderPackageRecordDtoListResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(orderPackageRecordDtoListResponse.getMessage());
                }
                List<OrderPackageRecordDto> orderPackageRecordDtos = orderPackageRecordDtoListResponse.getBody();
                //客户
                Response<List<Customer>> customerResponse = customerService.getAll();
                if (!customerResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(customerResponse.getMessage());
                }
                List<Customer> customers = customerResponse.getBody();
                //派车箱数
                ListResponse<OrderDispatchPackageQty> orderDispatchPackageQtyListResponse =
                        findOrderDispatchPackageQty((String) params.get("siteCode"), orderNos);
                if (!orderDispatchPackageQtyListResponse.isSuccess()) {
                    return ResponseBuilder.pageFail(orderDispatchPackageQtyListResponse.getMessage());
                }
                List<OrderDispatchPackageQty> orderDispatchPackageQties = orderDispatchPackageQtyListResponse.getBody();

                for (DispatchQueryDto dto : dispatchQueryDtos) {
                    //打包箱数
                    if (orderPackageRecordDtos != null && orderPackageRecordDtos.size() > 0 && !dto.isTransferOrder()) {
                        OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", dto.getOrderNo());
                        if (orderPackageRecordDto != null) {
                            dto.setTotalPackageQty(orderPackageRecordDto.getPackageQty());
                            dto.setTotalWeight(orderPackageRecordDto.getWeight());
                            dto.setTotalVolume(orderPackageRecordDto.getVolume());
                        }
                    }
                    //客户
                    Customer customer = ObjectQuery.findOne(customers, "code", dto.getCustomerCode());
                    if (customer != null) {
                        dto.setCustomerName(customer.getName());
                    }
                    //目的城市
                    if (!StringUtils.isEmpty(dto.getDestinationCode())) {
                        List<District> districts = districtService.getSuperior(dto.getDestinationCode()).getBody();
                        if (districts != null) {
                            List<String> names = districts.stream().map(District::getName).collect(Collectors.toList());
                            dto.setDestinationName(String.join("", names));
                        }
                    }
                    //派车箱数
                    Integer dispatchPackageQty = 0;
                    if (orderDispatchPackageQties != null && orderDispatchPackageQties.size() > 0) {
                        OrderDispatchPackageQty orderDispatchPackageQty = ObjectQuery.findOne(orderDispatchPackageQties, "orderNo", dto.getOrderNo());
                        if (orderDispatchPackageQty != null && orderDispatchPackageQty.getDispatchingPackageQty() != null) {
                            dispatchPackageQty = orderDispatchPackageQty.getDispatchingPackageQty();
                        }
                    }
                    dto.setDispatchPackageQty(dispatchPackageQty);
                }
            }
            return pageResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    public PageResponse searchDispatchItemPay(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            PageHelper.startPage(page, pageSize);
            List<DispatchItemPayableDto> list = dispatchItemMapper.pageList(params);
            PageInfo pageInfo = new PageInfo(list);
            List<DispatchItemPayableDto> result = convertDispatchItemDto(list, (String) params.get("branchCode"));
            return ResponseBuilder.page(result, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    private List<DispatchItemPayableDto> convertDispatchItemDto(List<DispatchItemPayableDto> dispatchDtos, String branchCode) throws Exception {
        List<String> nos = dispatchDtos.stream().map(DispatchItemPayableDto::getOrderNo).collect(Collectors.toList());
        List<OrderDispatchDto> orderList = logisticsOrderService.findOrderDispatch(nos).getBody();
        List<String> orderNos = orderList.stream().map(OrderDispatchDto::getOrderNo).collect(Collectors.toList());
        List<PayableProportion> dispatchProportions = payableService.findByOrderNos(orderNos, Payable.OwnerType.DRIVER.name()).getBody();
        List<DispatchItemPayableDto> list = new ArrayList<>();
        List<Customer> customers = customerService.getAvailable().getBody();
        if (null != orderList) {
            for (OrderDispatchDto orderAllDto : orderList) {
                DispatchItemPayableDto dto = null;
                if (null != orderAllDto.getInstructionNo()) {
                    dto = ObjectQuery.findOne(dispatchDtos, "orderNo", orderAllDto.getInstructionNo());
                } else {
                    dto = ObjectQuery.findOne(dispatchDtos, "orderNo", orderAllDto.getOrderNo());
                }
                DispatchItemPayableDto payableDto = BeanHelper.convert(orderAllDto, DispatchItemPayableDto.class);
                if (null != dto) {
                    BeanUtils.copyProperties(payableDto, dto);
                    payableDto.setOrderCreateTime(orderAllDto.getCreatedDate());
                    Customer c = ObjectQuery.findOne(customers, "code", dto.getCustomerCode());
                    if (null != c) dto.setCustomerName(c.getName());
                    payableDto.setControlType(orderAllDto.getDispatchType().getText());
                    payableDto.setDestinationName(orderAllDto.getDeliveryProvince() + orderAllDto.getDeliveryCity() +
                            org.apache.commons.lang3.StringUtils.defaultString(orderAllDto.getDeliveryDistrict()) +
                            org.apache.commons.lang3.StringUtils.defaultString(orderAllDto.getDeliveryStreet()));
                    PayableProportion proportion = ObjectQuery.findOne(dispatchProportions, "orderNo", payableDto.getOrderNo());
                    if (proportion != null) {
                        payableDto.setTotalPayable(proportion.getAmount());
                        payableDto.setRental(getPayableProportion(proportion.getProportionAccounts(), "120201"));
                        payableDto.setWaiting(getPayableProportion(proportion.getProportionAccounts(), "120202"));
                        payableDto.setUpstairs(getPayableProportion(proportion.getProportionAccounts(), "120205"));
                        payableDto.setParking(getPayableProportion(proportion.getProportionAccounts(), "120203"));
                        payableDto.setOther(getPayableProportion(proportion.getProportionAccounts(), "120206"));
                        payableDto.setBridge(getPayableProportion(proportion.getProportionAccounts(), "120204"));
                    }
                    list.add(payableDto);
                }
            }
        }
        return list;

    }

    private List<DispatchPayableDto> convertPayDto(List dispatchDtos, String branchCode) {
        List<DispatchPayableDto> result = BeanHelper.convertList(dispatchDtos, DispatchPayableDto.class);
        List<String> dispachNos = result.stream().map(DispatchPayableDto::getDispatchNumber).collect(Collectors.toList());
        List<String> dispatchIds = result.stream().map(DispatchPayableDto::getDispatchId).collect(Collectors.toList());
        List<Payable> payableDtos = payableService.findBySourceNos(dispachNos, Payable.OwnerType.DRIVER.name()).getBody();
        List<DispatchFollow> follows = findFollowers(dispatchIds).getBody();
        if (null != result && result.size() > 0) {
            List<User> userList = userService.getByOrgCode(branchCode).getBody();
            result.stream().forEach(dispatchDto -> {
                if (dispatchDto.getCreatedBy() != null) {
                    User user = ObjectQuery.findOne(userList, "userId", dispatchDto.getCreatedBy());
                    if (user != null) {
                        dispatchDto.setCreateUserName(user.getFullName());
                    }
                    List<DispatchFollow> dispatchFollows = ObjectQuery.find(follows, "dispatchId", dispatchDto.getDispatchId());
                    Set<String> userIds = dispatchFollows.stream().map(DispatchFollow::getFollowUserId).collect(Collectors.toSet());
                    List<String> userNames = new ArrayList<String>();
                    if (null != userIds && userIds.size() > 0) {
                        for (String userId : userIds) {
                            User user1 = ObjectQuery.findOne(userList, "userId", userId);
                            if (null != user1) userNames.add(user1.getFullName());
                        }
                    }
                    dispatchDto.setFollowUserName(String.join("/", userNames));
                    Payable payableDto = ObjectQuery.findOne(payableDtos, "sourceNo", dispatchDto.getDispatchNumber());
                    if (payableDto != null) {
                        dispatchDto.setTotalPayable(payableDto.getTotalAmount());
                        dispatchDto.setRental(getPayable(payableDto.getAccounts(), "120201"));
                        dispatchDto.setWaiting(getPayable(payableDto.getAccounts(), "120202"));
                        dispatchDto.setUpstairs(getPayable(payableDto.getAccounts(), "120205"));
                        dispatchDto.setParking(getPayable(payableDto.getAccounts(), "120203"));
                        dispatchDto.setOther(getPayable(payableDto.getAccounts(), "120206"));
                        dispatchDto.setBridge(getPayable(payableDto.getAccounts(), "120204"));
                    }
                }

            });
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

    private BigDecimal getPayableProportion(List<PayableProportionAccount> accounts, String code) {
        if (accounts != null) {
            for (PayableProportionAccount account : accounts) {
                if (code.equals(account.getAccountCode())) {
                    return account.getAmount();
                }
            }
        }
        return new BigDecimal(0);
    }

    private ListResponse<DispatchFollow> findFollowers(List<String> dispatchIds) {
        try {
            Assert.notNull(dispatchIds);
            List<DispatchFollow> follows = new ArrayList<>();
            int count = dispatchIds.size() / 1000;
            for (int i = 1; i <= count; i++) {
                follows.addAll(dispatchFollowMapper.findByDispatchIds(dispatchIds.subList(1000 * (i - 1), 1000 * i)));
            }
            follows.addAll(dispatchFollowMapper.findByDispatchIds(dispatchIds.subList(1000 * count, dispatchIds.size())));
            return ResponseBuilder.list(follows);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.listFail(e);
        }

    }

    private List<DispatchItemDto> dispatchItemConvert(List<DispatchItem> items, String siteCode) {
        try {
            List<DispatchItemDto> itemDtos = new ArrayList<>();
            if (items != null && items.size() > 0) {
                List<String> orderNos = items.stream().map(DispatchItem::getOrderNo).collect(Collectors.toList());
                //打包
                ListResponse<OrderPackageRecordDto> recordDtoListResponse = packageService.findOrderPacking(orderNos);
                if (!recordDtoListResponse.isSuccess()) {
                    return null;
                }
                List<OrderPackageRecordDto> orderPackageRecordDtos = recordDtoListResponse.getBody();
                //客户
                Response<List<Customer>> customerResponse = customerService.getAll();
                if (!customerResponse.isSuccess()) {
                    return null;
                }
                List<Customer> customers = customerResponse.getBody();
                //分公司
                List<Organization> orgs = organizationService.getAllBranches("TMS").getBody();
                //派车箱数
                ListResponse<OrderDispatchPackageQty> orderDispatchPackageQtyListResponse =
                        findOrderDispatchPackageQty(siteCode, orderNos);
                if (!orderDispatchPackageQtyListResponse.isSuccess()) {
                    return null;
                }
                List<OrderDispatchPackageQty> orderDispatchPackageQties = orderDispatchPackageQtyListResponse.getBody();
                //提货指令
                ListResponse<CollectingInstruction> collectingInstructionListResponse = collectingInstructionService.findByNos(orderNos);
                if (!collectingInstructionListResponse.isSuccess()) {
                    return null;
                }
                List<CollectingInstruction> instructions = collectingInstructionListResponse.getBody();
                //订单
                ListResponse<LogisticsOrderAllDto> logisticsOrderAllDtoListResponse = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
                if (!logisticsOrderAllDtoListResponse.isSuccess()) {
                    return null;
                }
                List<LogisticsOrderAllDto> logisticsOrderAllDtos = logisticsOrderAllDtoListResponse.getBody();
                for (DispatchItem item : items) {
                    String orderNo = item.getOrderNo();
                    DispatchItemDto dispatchItemDto = (DispatchItemDto) ReflectUtils.convert(item, DispatchItemDto.class);
                    CollectingInstruction collectingInstruction = ObjectQuery.findOne(instructions, "instructionNo", orderNo);
                    if (collectingInstruction != null) {
                        dispatchItemDto.setTotalPackageQty(collectingInstruction.getConfirmedTotalPackageQty());
                        dispatchItemDto.setTotalWeight(collectingInstruction.getConfirmedTotalWeight());
                        dispatchItemDto.setTotalVolume(collectingInstruction.getConfirmedTotalVolume());
                        Customer customer = ObjectQuery.findOne(customers, "code", collectingInstruction.getCustomerCode());
                        if (customer != null) {
                            dispatchItemDto.setCustomerName(customer.getName());
                        }
                        List<District> districts = districtService.getSuperior(collectingInstruction.getCityCode()).getBody();
                        dispatchItemDto.setDeliveryAddress(collectingInstruction.getAddress());
//                        dispatchItemDto.setDeliveryContacts(collectingInstruction.get());
                        if (districts != null) {
                            dispatchItemDto.setDestinationName(String.join("", districts.stream().map(District::getName).collect(Collectors.toList())));
                        }
                        if (orgs != null) {
                            Organization org = ObjectQuery.findOne(orgs, "code", collectingInstruction.getBranchCode());
                            if (org != null) dispatchItemDto.setBranchName(org.getName());
                        }
                    } else {
                        LogisticsOrderAllDto logisticsOrderAllDto = ObjectQuery.findOne(logisticsOrderAllDtos, "orderNo", orderNo);
                        dispatchItemDto.setCustomerOrderNo(logisticsOrderAllDto.getCustomerOrderNo());
                        Customer customer = ObjectQuery.findOne(customers, "code", logisticsOrderAllDto.getCustomerCode());
                        if (customer != null) {
                            dispatchItemDto.setCustomerName(customer.getName());
                        }
                        dispatchItemDto.setTotalPackageQty(logisticsOrderAllDto.getTotalPackageQty());
                        dispatchItemDto.setTotalVolume(logisticsOrderAllDto.getTotalVolume());
                        dispatchItemDto.setTotalWeight(logisticsOrderAllDto.getTotalWeight());
                        OrderPackageRecordDto orderPackageRecordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", orderNo);
                        if (orderPackageRecordDto != null) {
                            dispatchItemDto.setTotalPackageQty(orderPackageRecordDto.getPackageQty());
                            dispatchItemDto.setTotalVolume(orderPackageRecordDto.getVolume());
                            dispatchItemDto.setTotalWeight(orderPackageRecordDto.getWeight());
                        }
                        List<District> districts = districtService.getSuperior(logisticsOrderAllDto.getDestinationCode()).getBody();
                        dispatchItemDto.setDeliveryAddress(logisticsOrderAllDto.getDeliveryAddress());
                        dispatchItemDto.setDeliveryContacts(logisticsOrderAllDto.getDeliveryContacts());
                        if (districts != null) {
                            dispatchItemDto.setDestinationName(String.join("", districts.stream().map(District::getName).collect(Collectors.toList())));
                        }
                        if (orgs != null) {
                            Organization org = ObjectQuery.findOne(orgs, "code", logisticsOrderAllDto.getBranchCode());
                            if (org != null) dispatchItemDto.setBranchName(org.getName());
                        }
                    }
                    Integer dispatchPackageQty = 0;
                    OrderDispatchPackageQty orderDispatchPackageQty = ObjectQuery.findOne(orderDispatchPackageQties, "orderNo", orderNo);
                    if (orderDispatchPackageQty != null && orderDispatchPackageQty.getDispatchingPackageQty() != null) {
                        dispatchPackageQty = orderDispatchPackageQty.getDispatchingPackageQty();
                    }
                    dispatchItemDto.setDispatchPackageQty(dispatchPackageQty - dispatchItemDto.getPackageQuantity());
                    itemDtos.add(dispatchItemDto);
                }
            }
            return itemDtos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<DispatchFeeDetailDto> dispatchFeeDetailConvert(List<DispatchFeeDetail> feeDetails) {
        List<DispatchFeeDetailDto> feeDetailDtos = new ArrayList<>();
        ListResponse<ExpenseAccount> expenseAccountListResponse = expenseAccountService.findAll();
        if (!expenseAccountListResponse.isSuccess()) {
            return null;
        }
        List<ExpenseAccount> expenseAccounts = expenseAccountListResponse.getBody();
        if (feeDetails != null && feeDetails.size() > 0) {
            for (DispatchFeeDetail feeDetail : feeDetails) {
                DispatchFeeDetailDto feeDetailDto = (DispatchFeeDetailDto) ReflectUtils.convert(feeDetail, DispatchFeeDetailDto.class);
                ExpenseAccount expenseAccount = ObjectQuery.findOne(expenseAccounts, "code", feeDetailDto.getFeeAccountCode());
                if (expenseAccount != null) {
                    feeDetailDto.setFeeAccountName(expenseAccount.getName());
                }
                feeDetailDtos.add(feeDetailDto);
            }
        }
        return feeDetailDtos;
    }

    @Override
    public Response flowers(String dispatchId, List<DispatchFollow> follows, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Assert.notEmpty(follows);

            Dispatch dispatch = dispatchMapper.get(dispatchId);
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            follows.forEach(item -> {
                item.setDispatchId(dispatchId);
                item.setDispatchFollowId(Snowflake.getInstance().next());
            });
            Dispatch.statusEnum status = dispatch.getStatus();
            if (Dispatch.statusEnum.CANCELED.equals(status) || Dispatch.statusEnum.FINISHED.equals(status)) {
                return ResponseBuilder.fail("派车单已完成或已取消，不能指定跟车人！");
            }
            Assert.isTrue(dispatchFollowMapper.batchInsert(follows) == follows.size(), "添加跟车人失败");
            DispatchLog log = this.buildLog(dispatch, operatorId, "指定跟车人");
            Assert.isTrue(dispatchLogMapper.insert(log) > 0);
            return ResponseBuilder.success(dispatch);
        } catch (Exception e) {
            log.error(className + "flowers", e);
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response updateFee(String dispatchId, List<DispatchFeeDetail> feeDetails, String operatorId) {
        try {
            Assert.hasText(dispatchId);
            Assert.notEmpty(feeDetails);
            Dispatch dispatch = dispatchMapper.get(dispatchId);
            BigDecimal oldTotalFee = dispatch.getTotalFee();
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            Dispatch.statusEnum status = dispatch.getStatus();
            Assert.notNull(status);
            if (Dispatch.statusEnum.CANCELED.equals(status)) {
                return ResponseBuilder.fail("派车单已取消！");
            }
            feeDetailMapper.batchUpdate(feeDetails);
            BigDecimal totalFee = new BigDecimal(0);
            for (DispatchFeeDetail feeDetail : feeDetails) {
                totalFee = totalFee.add(feeDetail.getAmount() == null ? new BigDecimal(0) : feeDetail.getAmount());
            }
            dispatch.setTotalFee(totalFee);
            dispatchMapper.update(dispatch);
            DispatchLog log = buildLog(dispatch, operatorId, "修改费用，原费用为" + oldTotalFee + "，修改为" + totalFee);
            Assert.isTrue(dispatchLogMapper.insert(log) > 0, "新增派车单日志失败");
            return ResponseBuilder.success();
        } catch (Exception e) {
            log.error(className + "updateFee", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<Dispatch> getByNo(String dispatchNo) {
        try {
            Assert.notNull(dispatchNo);
            Dispatch dispatch = dispatchMapper.getByNo(dispatchNo);
            if (dispatch != null) {
                List<DispatchItem> items = dispatchItemMapper.findByDispatchId(dispatch.getDispatchId());
                List<DispatchFollow> follows = dispatchFollowMapper.findByDispatchId(dispatch.getDispatchId());
                List<DispatchLog> logs = dispatchLogMapper.findByDispatchId(dispatch.getDispatchId());
                List<DispatchPackage> packages = dispatchPackageMapper.findByDispatchId(dispatch.getDispatchId());
                List<DispatchFeeDetail> feeDetails = feeDetailMapper.findByDispatchId(dispatch.getDispatchId());
                dispatch.setItems(items);
                dispatch.setFollows(follows);
                dispatch.setLogs(logs);
                dispatch.setPackages(packages);
                dispatch.setFeeDetails(feeDetails);
                return ResponseBuilder.success(dispatch);
            }
            return ResponseBuilder.fail("派车单不存在！");
        } catch (Exception e) {
            log.error(className + "getByNo", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    private DispatchLog buildLog(Dispatch dispatch, String operatorId, String operatorContent) {
        DispatchLog log = DispatchLog.builder()
                .dispatchLogId(Snowflake.getInstance().next())
                .dispatchId(dispatch.getDispatchId())
                .status(dispatch.getStatus())
                .operationTime(LocalDateTime.now())
                .operationContent(operatorContent)
                .operatorId(operatorId)
                .build();
        return log;
    }

    private List<DispatchItemDto> dispatchItemPadding(List<DispatchItemDto> itemDtos, List<OrderDispatchPackageQty> orderDispatchPackageQties) {
        try {
            itemDtos.stream().forEach(itemDto -> {
                OrderDispatchPackageQty orderDispatchPackageQty = ObjectQuery.findOne(orderDispatchPackageQties, "orderNo", itemDto.getOrderNo());
                Integer dispatchingPackageQty = 0;
                if (orderDispatchPackageQty != null) {
                    dispatchingPackageQty = orderDispatchPackageQty.getDispatchingPackageQty() == null ? 0 : orderDispatchPackageQty.getDispatchingPackageQty();
                }
                itemDto.setDispatchPackageQty(dispatchingPackageQty);
                itemDto.setPackageQuantity((itemDto.getTotalPackageQty() == null ? 0 : itemDto.getTotalPackageQty()) - itemDto.getDispatchPackageQty());
                itemDto.setVolume(itemDto.getTotalVolume() == null ? new BigDecimal(0) : itemDto.getTotalVolume());
                itemDto.setWeight(itemDto.getTotalWeight() == null ? new BigDecimal(0) : itemDto.getTotalWeight());
                if (DispatchItem.orderTypeEnum.COLLECTING_INSTRUCTION.equals(itemDto.getOrderType())) {
                    itemDto.setOrderDispatchType(DispatchItem.orderDispatchTypeEnum.COLLECTING);
                }
            });
            return itemDtos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ListResponse<Dispatch> findDispatchByOrderNo(String siteCode, String orderNo) {
        try {
            Assert.hasText(siteCode);
            Assert.hasText(orderNo);
            return ResponseBuilder.list(dispatchMapper.findByOrderNoAndSiteCode(siteCode, orderNo));
        } catch (Exception e) {
            log.error(className + "findDispatchByOrderNo", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }
}

/*
package com.lnet.tms.service.consign;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.Snowflake;
import com.lnet.microservices.consign.api.ConsignOrderService;
import com.lnet.microservices.consign.contract.*;
import com.lnet.microservices.consign.dao.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class ConsignOrderServiceImpl_old implements ConsignOrderService {

    private final String className = this.getClass().getSimpleName() + ".";

    @Autowired
    ConsignOrderDao consignOrderDao;
    @Autowired
    ConsignOrderOperationTimeDao operationTimeDao;
    @Autowired
    ConsignOrderLogDao logDao;
    @Autowired
    ConsignOrderItemDao itemDao;
    @Autowired
    OrderConsignPackageQtyDao consignPackageQtyDao;

    @Override
    public Response<ConsignOrder> create(ConsignOrder consignOrder) {
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
            if (consignOrderDao.orderNoIsExists(consignOrder.getCarrierCode(), consignOrder.getConsignOrderNo(), consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            consignOrder.setConsignOrderId(Snowflake.getInstance().next());
            consignOrder.setCreateDate(LocalDateTime.now());
            consignOrder.setStatus(ConsignOrder.consignStatus.NEW);
            consignOrder.getItems().forEach(item -> {
                item.setConsignOrderId(consignOrder.getConsignOrderId());
                item.setItemId(Snowflake.getInstance().next());
            });
            int insertResult = consignOrderDao.insert(consignOrder);
            if (insertResult != 1) {
                return ResponseBuilder.fail("托运单保存失败！");
            }
            Assert.isTrue(itemDao.batchInsert(consignOrder.getItems()) == consignOrder.getItems().size());
            ConsignOrderOperationTime operationTime = ConsignOrderOperationTime.builder()
                    .consignOrderId(consignOrder.getConsignOrderId())
                    .build();
            Assert.isTrue(operationTimeDao.insert(operationTime) == 1);
            ConsignOrderLog log = createLog(consignOrder, "创建托运单", consignOrder.getCreatedBy(), false);
            Assert.isTrue(logDao.insert(log) == 1);
            consignOrder.setOperationTime(operationTime);
            //region托运单明细开单箱数
            List<ConsignOrderItem> items = consignOrder.getItems();
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQtyList = consignPackageQtyDao.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
            Map<String, Object> map = this.getCreateOrReplace(items, orderConsignPackageQtyList, consignOrder.getSiteCode(), consignOrder.getBranchCode());
            List<OrderConsignPackageQty> addList = (List<OrderConsignPackageQty>) map.get("addList");
            List<OrderConsignPackageQty> updateList = (List<OrderConsignPackageQty>) map.get("updateList");

            if (addList.size() > 0) {
                Assert.isTrue(consignPackageQtyDao.batchInsert(addList) == addList.size());
            }
            if (updateList.size() > 0) {
                consignPackageQtyDao.batchUpdateConsigningQty(updateList);
            }
            //endregion
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "create", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> batchCreate(List<ConsignOrder> consignOrders) {
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
            List<ConsignOrder> consignOrders1 = consignOrderDao.findByOrderNos(consignOrderNos);
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
            Assert.isTrue(consignOrderDao.batchInsert(consignOrders) == consignOrders.size());
            Assert.isTrue(itemDao.batchInsert(items) == items.size());
            Assert.isTrue(operationTimeDao.batchInsert(operationTimes) == operationTimes.size());
            Assert.isTrue(logDao.batchInsert(logs) == logs.size());
            //endregion
            //region托运单明细开单箱数
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQtyList = consignPackageQtyDao.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
            Map<String, Object> map = this.getCreateOrReplace(items, orderConsignPackageQtyList, consignOrders.get(0).getSiteCode(), consignOrders.get(0).getBranchCode());
            List<OrderConsignPackageQty> addList = (List<OrderConsignPackageQty>) map.get("addList");
            List<OrderConsignPackageQty> updateList = (List<OrderConsignPackageQty>) map.get("updateList");
            if (addList.size() > 0) {
                Assert.isTrue(consignPackageQtyDao.batchInsert(addList) == addList.size());
            }
            if (updateList.size() > 0) {
                consignPackageQtyDao.batchUpdateConsigningQty(updateList);
            }
            //endregion
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchCreate", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public Response<ConsignOrder> update(ConsignOrderModifyDto consignOrderModifyDto) {
        try {
            Assert.notNull(consignOrderModifyDto);
            ConsignOrder consignOrder = consignOrderModifyDto.getConsignOrder();
            Assert.notNull(consignOrder);
            Assert.notNull(consignOrder.getConsignOrderId());
            Assert.notEmpty(consignOrder.getItems());
            ConsignOrder oldConsignOrder = consignOrderDao.get(consignOrder.getConsignOrderId());
            if (!ConsignOrder.consignStatus.NEW.equals(oldConsignOrder.getStatus())) {
                return ResponseBuilder.fail("托运单已发运或已取消，不能操作修改！");
            }
            if (consignOrderDao.orderNoIsExists(consignOrder.getCarrierCode(), consignOrder.getConsignOrderNo(), consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            consignOrder.setModifyDate(LocalDateTime.now());
            Assert.isTrue(consignOrderDao.update(consignOrder) == 1);
            //删除原有的订单明细
            Assert.isTrue(itemDao.deleteByConsignOrderId(consignOrder.getConsignOrderId()) > 0);
            //新增新的订单明细
            consignOrder.getItems().forEach(item -> {
                item.setItemId(Snowflake.getInstance().next());
                item.setConsignOrderId(consignOrder.getConsignOrderId());
            });
            Assert.isTrue(itemDao.batchInsert(consignOrder.getItems()) == consignOrder.getItems().size());
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
            Assert.isTrue(logDao.insert(log) == 1);

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
            List<OrderConsignPackageQty> orderConsignPackageQtyList = consignPackageQtyDao.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
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
                Assert.isTrue(consignPackageQtyDao.batchInsert(addListAll) == addListAll.size());
            }
            if (updateListAll.size() > 0) {
                consignPackageQtyDao.batchUpdateConsigningQty(updateListAll);
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

    @Override
    public Response<ConsignOrder> consign(ConsignOrder consignOrder, String operatorId) {
        try {
            Assert.notNull(consignOrder);
            Assert.notNull(consignOrder.getConsignOrderId());
            Assert.notNull(consignOrder.getOperationTime());
            ConsignOrder oldConsignOrder = consignOrderDao.get(consignOrder.getConsignOrderId());
            if (!ConsignOrder.consignStatus.NEW.equals(oldConsignOrder.getStatus())) {
                return ResponseBuilder.fail("托运单已发运或已取消，不能操作发运！");
            }
            if (consignOrderDao.orderNoIsExists(consignOrder.getCarrierCode(), consignOrder.getConsignOrderNo(), consignOrder.getConsignOrderId())) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            consignOrder.setStatus(ConsignOrder.consignStatus.CONSIGNED);
            Assert.isTrue(consignOrderDao.update(consignOrder) == 1);
            ConsignOrderOperationTime operationTime = operationTimeDao.get(consignOrder.getConsignOrderId());
            operationTime.setConsignTime(consignOrder.getOperationTime().getConsignTime());
            operationTime.setFeedbackConsignTime(consignOrder.getOperationTime().getFeedbackConsignTime());
            operationTime.setPredictArriveTime(consignOrder.getOperationTime().getPredictArriveTime());
            consignOrder.setOperationTime(operationTime);
            Assert.isTrue(operationTimeDao.update(operationTime) == 1);
            //日志
            ConsignOrderLog log = createLog(consignOrder, "托运单发运", operatorId, false);
            Assert.isTrue(logDao.insert(log) == 1);
            //发运箱数
            List<String> orderNos = consignOrder.getItems().stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyDao.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
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
                consignPackageQtyDao.batchUpdateConsignedQty(updateConsignQtyList);
            }
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "consign", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> batchConsign(List<ConsignOrder> consignOrders, String operatorId) {
        try {
            Assert.notEmpty(consignOrders);
            consignOrders.stream().forEach(consignOrder -> {
                Assert.notNull(consignOrder);
                Assert.notNull(consignOrder.getConsignOrderId());
                Assert.notNull(consignOrder.getOperationTime());
            });
            List<String> consignOrderIds = consignOrders.stream().map(ConsignOrder::getConsignOrderId).collect(Collectors.toList());
            List<ConsignOrder> oldConsignOrders = consignOrderDao.findByIds(consignOrderIds);
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
            Assert.isTrue(consignOrderDao.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.CONSIGNED) == consignOrderIds.size());
            List<ConsignOrderOperationTime> operationTimes = consignOrders.stream().map(ConsignOrder::getOperationTime).collect(Collectors.toList());
            operationTimeDao.batchUpdateConsignTime(operationTimes);
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.CONSIGNED);
                ConsignOrderLog log = createLog(consignOrder, "托运单发运", operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logDao.batchInsert(logs) == logs.size());
            //发运箱数
            List<ConsignOrderItem> items = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> items.addAll(consignOrder.getItems()));
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyDao.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
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
                consignPackageQtyDao.batchUpdateConsignedQty(updateConsignQtyList);
            }
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchConsign", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> batchStartUp(List<String> consignOrderIds, LocalDateTime startUpTime, String operatorId) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderDao.findByIds(consignOrderIds);
            List<String> statusExceptionOrderNos = new ArrayList<>();
            for (ConsignOrder consignOrder : consignOrders) {
                if (!ConsignOrder.consignStatus.CONSIGNED.equals(consignOrder.getStatus())) {
                    statusExceptionOrderNos.add(consignOrder.getConsignOrderNo());
                }
            }
            if (statusExceptionOrderNos.size() > 0) {
                return ResponseBuilder.listFail(String.join(",", statusExceptionOrderNos) + "不能操作发运，请选择已发运的托运单！");
            }
            Assert.isTrue(consignOrderDao.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.IN_TRANSIT) == consignOrderIds.size());
            Assert.isTrue(operationTimeDao.batchUpdateStartUpTime(consignOrderIds, startUpTime) == consignOrderIds.size());
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.IN_TRANSIT);
                ConsignOrderLog log = createLog(consignOrder, "托运单启运，启运时间：" + startUpTime, operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logDao.batchInsert(logs) == logs.size());
            //启运箱数
            List<ConsignOrderItem> items = itemDao.findByConsignOrderIds(consignOrderIds);
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyDao.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
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
                consignPackageQtyDao.batchUpdateStartUpQty(updateStartUpQtyList);
            }
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchStartUp", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> batchArrived(List<String> consignOrderIds, LocalDateTime arriveTime, String operatorId) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderDao.findByIds(consignOrderIds);
            List<String> statusExceptionOrderNos = new ArrayList<>();
            for (ConsignOrder consignOrder : consignOrders) {
                if (!ConsignOrder.consignStatus.IN_TRANSIT.equals(consignOrder.getStatus())) {
                    statusExceptionOrderNos.add(consignOrder.getConsignOrderNo());
                }
            }
            if (statusExceptionOrderNos.size() > 0) {
                return ResponseBuilder.listFail(String.join(",", statusExceptionOrderNos) + "不能操作到达，请选择在途中的托运单！");
            }
            Assert.isTrue(consignOrderDao.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.ARRIVED) == consignOrderIds.size());
            Assert.isTrue(operationTimeDao.batchUpdateArriveTime(consignOrderIds, arriveTime) == consignOrderIds.size());
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.ARRIVED);
                ConsignOrderLog log = createLog(consignOrder, "托运单到达,到达时间：" + arriveTime, operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logDao.batchInsert(logs) == logs.size());
            //启运箱数
            List<ConsignOrderItem> items = itemDao.findByConsignOrderIds(consignOrderIds);
            List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
            List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyDao.findBySiteAndOrderNo(consignOrders.get(0).getSiteCode(), orderNos);
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
                consignPackageQtyDao.batchUpdateArriveQty(updateStartUpQtyList);
            }
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchArrived", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> batchFinished(List<String> consignOrderIds, LocalDateTime finishTime, String operatorId) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderDao.findByIds(consignOrderIds);
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
            Assert.isTrue(consignOrderDao.batchUpdateStatus(consignOrderIds, ConsignOrder.consignStatus.FINISHED) == consignOrderIds.size());
            Assert.isTrue(operationTimeDao.batchUpdateFinishTime(consignOrderIds, finishTime) == consignOrderIds.size());
            List<ConsignOrderLog> logs = new ArrayList<>();
            consignOrders.stream().forEach(consignOrder -> {
                consignOrder.setStatus(ConsignOrder.consignStatus.FINISHED);
                ConsignOrderLog log = createLog(consignOrder, "托运单完成,完成时间：" + finishTime, operatorId, false);
                logs.add(log);
            });
            Assert.isTrue(logDao.batchInsert(logs) == logs.size());
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "batchFinished", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public Response<ConsignOrder> cancel(String consignOrderId, String operatorId, String notes) {
        try {
            Assert.notNull(consignOrderId);
            ConsignOrder consignOrder = consignOrderDao.get(consignOrderId);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            if (!ConsignOrder.consignStatus.NEW.equals(consignOrder.getStatus())) {
                return ResponseBuilder.fail("托运单不能操作取消，请选择开单中的托运单！");
            }
            List<ConsignOrderItem> items = itemDao.findByConsignOrderId(consignOrderId);
            Assert.isTrue(consignOrderDao.updateStatus(consignOrderId, ConsignOrder.consignStatus.CANCELED) == 1);
            Assert.isTrue(itemDao.deleteByConsignOrderId(consignOrderId) > 0);
            consignOrder.setStatus(ConsignOrder.consignStatus.CANCELED);
            ConsignOrderLog log = createLog(consignOrder, "托运单取消,取消原因：" + notes, operatorId, false);
            Assert.isTrue(logDao.insert(log) == 1);
            //发运箱数
            List<OrderConsignPackageQty> updateList = new ArrayList<>();
            if (items != null && items.size() > 0) {
                for (ConsignOrderItem item : items) {
                    OrderConsignPackageQty orderConsignPackageQty = null;
                    List<String> orderNos = items.stream().map(ConsignOrderItem::getOrderNo).collect(Collectors.toList());
                    List<OrderConsignPackageQty> orderConsignPackageQties = consignPackageQtyDao.findBySiteAndOrderNo(consignOrder.getSiteCode(), orderNos);
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
                consignPackageQtyDao.batchUpdateConsigningQty(updateList);
            }
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "cancel", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<ConsignOrder> get(String consignOrderId) {
        try {
            Assert.notNull(consignOrderId);
            ConsignOrder consignOrder = consignOrderDao.get(consignOrderId);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            List<ConsignOrderItem> items = itemDao.findByConsignOrderId(consignOrderId);
            consignOrder.setItems(items);
            ConsignOrderOperationTime operationTime = operationTimeDao.get(consignOrderId);
            consignOrder.setOperationTime(operationTime);
            List<ConsignOrderLog> logs = logDao.findByConsignOrderId(consignOrderId);
            consignOrder.setLogs(logs);
            return ResponseBuilder.success(consignOrder);
        } catch (Exception e) {
            log.error(className + "get", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public PageResponse<ConsignOrderPageDto> pageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<ConsignOrderPageDto> list = consignOrderDao.pageList(params);
            PageInfo pageInfo = new PageInfo<>(list);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(className + "pageList", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.pageFail(e);
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

    @Override
    public Response<ConsignOrder> updateOrderNo(String consignOrderId, String consignOrderNo, String operatorId) {
        try {
            Assert.notNull(consignOrderId);
            Assert.notNull(consignOrderNo);
            ConsignOrder consignOrder = consignOrderDao.get(consignOrderId);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            if (consignOrder.getIsTemporaryNo() == null || !consignOrder.getIsTemporaryNo()) {
                return ResponseBuilder.fail("托运单单号非临时单号，不能替换！");
            }
            if (consignOrderDao.orderNoIsExists(consignOrder.getCarrierCode(), consignOrderNo, consignOrderId)) {
                return ResponseBuilder.fail("托运单单号已存在！");
            }
            Assert.isTrue(consignOrderDao.updateOrderNo(consignOrderId, consignOrderNo) == 1);
            ConsignOrderLog log = createLog(consignOrder, "托运单单号更新，原临时单号：" + consignOrder.getConsignOrderNo(), operatorId, false);
            Assert.isTrue(logDao.insert(log) == 1);
            return ResponseBuilder.success();
        } catch (Exception e) {
            log.error(className + "updateOrderNo", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrder> findByIds(List<String> consignOrderIds) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrder> consignOrders = consignOrderDao.findByIds(consignOrderIds);
            return ResponseBuilder.list(consignOrders);
        } catch (Exception e) {
            log.error(className + "findByIds", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<ConsignOrderItem> findItemsByIds(List<String> consignOrderIds) {
        try {
            Assert.notEmpty(consignOrderIds);
            List<ConsignOrderItem> items = itemDao.findByConsignOrderIds(consignOrderIds);
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
            return ResponseBuilder.list(consignPackageQtyDao.findBySiteAndOrderNo(siteCode, orderNos));
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
            return ResponseBuilder.list(consignOrderDao.findByOrderNos(consignOrderNos));
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
            ConsignOrder consignOrder = consignOrderDao.getByNo(carrierCode, consignOrderNo);
            if (consignOrder == null) {
                return ResponseBuilder.fail("托运单不存在！");
            }
            List<ConsignOrderItem> items = itemDao.findByConsignOrderId(consignOrder.getConsignOrderId());
            consignOrder.setItems(items);
            ConsignOrderOperationTime operationTime = operationTimeDao.get(consignOrder.getConsignOrderId());
            consignOrder.setOperationTime(operationTime);
            List<ConsignOrderLog> logs = logDao.findByConsignOrderId(consignOrder.getConsignOrderId());
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

            return ResponseBuilder.list(consignOrderDao.findByOrderNo(orderNo));
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public ListResponse<OrderMonthDto> findOrderByMonth(String createdBy) {
        try {
            Assert.hasText(createdBy);
            return ResponseBuilder.list(consignOrderDao.findOrderByMonth(createdBy));
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public Response getTodayConsignCount(String branchCode, String siteCode) {

        return ResponseBuilder.success(consignOrderDao.getTodayConsignCount(branchCode, siteCode));
    }

    @Override
    public ListResponse<ConsignOrder> payableModifyQuery(String carrierCode) {
        try {
            Assert.hasText(carrierCode);
            List<String> statusList = new ArrayList<>();
            statusList.add(ConsignOrder.consignStatus.CONSIGNED.name());
            statusList.add(ConsignOrder.consignStatus.IN_TRANSIT.name());
            statusList.add(ConsignOrder.consignStatus.ARRIVED.name());
            statusList.add(ConsignOrder.consignStatus.FINISHED.name());
            return ResponseBuilder.list(consignOrderDao.findByCarrierCode(carrierCode, statusList));
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public ListResponse<ConsignDetailDto> findDetailsByOrderNos(List<String> orderNos) {
        try {
            Assert.notEmpty(orderNos);
            List<ConsignDetailDto> result = new ArrayList<>();
            int count = orderNos.size() / 1000;
            for (int i = 1; i <= count; i++) {
                result.addAll(consignOrderDao.findItemsByOrderNos(orderNos.subList(1000 * (i - 1), 1000 * i)));
            }
            result.addAll(consignOrderDao.findItemsByOrderNos(orderNos.subList(1000 * count, orderNos.size())));
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

            List<ConsignOrder> consignOrders = consignOrderDao.findByOrderNo(orderNo);
            consignOrders.forEach(e -> {
                List<ConsignOrderLog> logs = logDao.findByConsignOrderId(e.getConsignOrderId());
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
}
*/

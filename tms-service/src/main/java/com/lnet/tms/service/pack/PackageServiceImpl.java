package com.lnet.tms.service.pack;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.ObjectQuery;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.tms.pack.packDto.*;
import com.lnet.model.tms.pack.packEntity.OrderPackage;
import com.lnet.model.tms.pack.packEntity.OrderPackingInfo;
import com.lnet.model.tms.pack.packEntity.PackageRecord;
import com.lnet.model.tms.pack.packEntity.Package;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.model.ums.transprotation.transprotationDto.LogisticsOrderAllDto;
import com.lnet.tms.contract.spi.pack.PackageService;
import com.lnet.tms.mapper.OrderPackageMapper;
import com.lnet.tms.mapper.OrderPackingInfoMapper;
import com.lnet.tms.mapper.PackageMapper;
import com.lnet.tms.mapper.PackageRecordMapper;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/8/22.
 */
@Service
@Transactional
@Slf4j
public class PackageServiceImpl implements PackageService {

    @Resource
    PackageMapper packageMapper;

    @Resource
    PackageRecordMapper packageRecordMapper;

    @Resource
    OrderPackageMapper orderPackageMapper;


    @Autowired
    CustomerService customerService;
    
    @Resource
    LogisticsOrderService logisticsOrderService;
    @Resource
    OrderPackingInfoMapper orderPackingInfoMapper;

    private Response<PackageDto> merge(PackageDto packDto) {
        try {
            Assert.notNull(packDto);
            Assert.notEmpty(packDto.getOrderNos(), "请选择订单");
            Assert.notEmpty(packDto.getPackers(), "请添加打包记录");
            List<OrderPackage> orderPackages = new ArrayList<>();
            List<PackageRecord> packageRecords = new ArrayList<>();
            List<OrderPackingInfo> orderPackingInfos = new ArrayList<>();

            OrderPackingInfo updatePackingInfo = null;
            Package updatePackage = null;//二次打包需要修改的

            List<Package> newPackages = new ArrayList<>();//新加的包
            //1、（一单多包）删除原来的包，重新打包
            //2、（多单一包）原来包变无效，
            // 二次打包选择的有已经打包成多包的单； 抛出异常。重新加一个包。改包体积重量....
            if (packDto.getOrderNos().size() == 1) {//一单打成多包
                List<Package> oldPackages = packageMapper.findPackByOrderNos(packDto.getOrderNos());
                if (oldPackages != null && oldPackages.size() == 1 && oldPackages.get(0).getStatus().equals(Package.Status.MERGE)) {
                    throw new Exception("选择的订单已合单打包，不能拆单打包");
                }
                String orderNo = packDto.getOrderNos().get(0);
                BigDecimal volume = new BigDecimal(0);
                BigDecimal weight = new BigDecimal(0);
                for (Package pack : packDto.getPackages()) {
                    pack.setPackageNo(getPackageNo());
                    pack.setPackageId(Snowflake.getInstance().next());
                    pack.setStatus(Package.Status.SPLIT);
                    newPackages.add(pack);
                    volume = volume.add(pack.getVolume());
                    weight = weight.add(pack.getWeight());
                    //region 打包记录
                    packDto.getPackers().forEach(packer -> {
                        PackageRecord record = new PackageRecord();
                        record.setPackageId(pack.getPackageId());
                        record.setPackageNo(pack.getPackageNo());
                        record.setPackingTime(LocalDateTime.now());
                        record.setRecordId(Snowflake.getInstance().next());
                        record.setPackingUser(packer);
                        record.setBranchCode(packDto.getBranchCode());
                        record.setSiteCode(packDto.getSiteCode());
                        packageRecords.add(record);
                    });
                    //endregion
                    orderPackages.add(OrderPackage.builder()
                            .orderNo(orderNo)
                            .itemId(Snowflake.getInstance().next())
                            .packageId(pack.getPackageId())
                            .build());
                }
                OrderPackingInfo packingInfo = orderPackingInfoMapper.selectById(orderNo);
                if (null != packingInfo) {//已打过包的单
                    //删除原来的打包数据
                    packageMapper.deleteByOrderNo(orderNo);
                    orderPackageMapper.deleteByNo(packDto.getOrderNos());
                    orderPackingInfoMapper.deleteById(orderNo);
                    packageRecordMapper.deleteByNos(packDto.getOrderNos());
                }
                //创建打包后的订单数据
                orderPackingInfos.add(OrderPackingInfo.builder()
                        .orderNo(orderNo)
                        .volume(volume)
                        .weight(weight)
                        .packageQty(packDto.getPackages().size())
                        .build());
            } else {//多单一包
                //如果包含有一单打成多包的单直接抛出异常
                Assert.isTrue(packDto.getPackages().size() == 1, "多个订单只能打成一个包");
                List<Package> oldPackages = packageMapper.findPackByOrderNos(packDto.getOrderNos());
                if (oldPackages != null && oldPackages.size() > 0) {
                    if (oldPackages.size() > 1) {
                        throw new Exception("选择的订单已打成多包，不能合单打包");
                    } else {//已打成一包
                        //删除原来的打包数据
                        packageMapper.deleteByOrderNos(packDto.getOrderNos());
                        orderPackageMapper.deleteByNo(packDto.getOrderNos());
                        orderPackingInfoMapper.deleteByIds(packDto.getOrderNos());
                        packageRecordMapper.deleteByNos(packDto.getOrderNos());
                    }
                }
                Package  pack = packDto.getPackages().get(0);
                pack.setPackageNo(getPackageNo());
                pack.setPackageId(Snowflake.getInstance().next());
                pack.setStatus(Package.Status.MERGE);
                newPackages.add(pack);
                packDto.getPackers().forEach(packer -> {
                    PackageRecord record = new PackageRecord();
                    record.setPackageId(pack.getPackageId());
                    record.setPackageNo(pack.getPackageNo());
                    record.setPackingTime(LocalDateTime.now());
                    record.setRecordId(Snowflake.getInstance().next());
                    record.setPackingUser(packer);
                    record.setBranchCode(packDto.getBranchCode());
                    record.setSiteCode(packDto.getSiteCode());
                    packageRecords.add(record);
                });
                for (int i = 0; i < packDto.getOrderNos().size(); i++) {
                    String orderNo = packDto.getOrderNos().get(i);
                    orderPackages.add(OrderPackage.builder()
                            .orderNo(orderNo)
                            .itemId(Snowflake.getInstance().next())
                            .packageId(pack.getPackageId())
                            .build());
                    if (i == 0) {//合单多单打成一包时，只有一单有打包值
                        orderPackingInfos.add(OrderPackingInfo.builder()
                                .orderNo(orderNo)
                                .packageId(pack.getPackageId())
                                .volume(pack.getVolume())
                                .weight(pack.getWeight())
                                .packageQty(1)
                                .packageQty(packDto.getPackages().size())
                                .build());
                    } else {
                        orderPackingInfos.add(OrderPackingInfo.builder()
                                .orderNo(orderNo)
                                .packageId(pack.getPackageId())
                                .volume(new BigDecimal(0))
                                .weight(new BigDecimal(0))
                                .packageQty(0)
                                .packageQty(packDto.getPackages().size())
                                .build());
                    }
                }
            }
            if (newPackages.size() > 0)
                Assert.isTrue(packageMapper.batchInsert(newPackages) == newPackages.size(), "新增包失败");
            if (packageRecords.size() > 0)
                Assert.isTrue(packageRecordMapper.batchInsert(packageRecords) == packageRecords.size(), "新增打包记录失败");
            if (orderPackages.size() > 0)
                Assert.isTrue(orderPackageMapper.batchInsert(orderPackages) == orderPackages.size(), "新增包单关联失败");
            if (orderPackingInfos.size() > 0)
                Assert.isTrue(orderPackingInfoMapper.batchInsert(orderPackingInfos) == orderPackingInfos.size(), "新增打包信息失败");
            return ResponseBuilder.success(packDto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    public Response<PackageDto> mergePackage(PackageDto packageDto) {
        try {
            //region  参数验证
            Assert.notNull(packageDto);
            Assert.notEmpty(packageDto.getOrderNos(), "请选择订单");
            Assert.notEmpty(packageDto.getPackers(), "请添加打包记录");
            //endregion
            //region  验证订单是否为“已审单”状态
            List<LogisticsOrder.Status> statuses = new ArrayList<>();
            statuses.add(LogisticsOrder.Status.ACCEPTED);
            Response response = logisticsOrderService.validatePackage(packageDto.getOrderNos(), statuses);
            if (!response.isSuccess()) {
                return response;
            }
            //endregion   
            // TODO: 2017/1/6 费用
            /*//region生成应收
            List<ReceivableOrderDto> receivableOrderDtos = new ArrayList<>();
            List<Package> packages = packageDto.getPackages();
            Integer itemQty = packages.stream().filter(p -> p.getItemQty() != null).collect(Collectors.summingInt(Package::getItemQty));
            Double weight = packages.stream().filter(p -> p.getWeight() != null).collect(Collectors.summingDouble(pack -> pack.getWeight().doubleValue()));
            Double volume = packages.stream().filter(p -> p.getVolume() != null).collect(Collectors.summingDouble(pack -> pack.getVolume().doubleValue()));
            if (packageDto.getOrderNos().size() == 1) {
                LogisticsOrderAllDto orderAllDto = logisticsOrderService.findDtoByOrderNo(packageDto.getOrderNos().get(0)).getBody();
                if (orderAllDto != null) {
                    orderAllDto.setTotalWeight(new BigDecimal(weight));
                    orderAllDto.setTotalVolume(new BigDecimal(volume));
                    orderAllDto.setTotalPackageQty(packages.size());
                    orderAllDto.setTotalItemQty(itemQty);
                }
                receivableOrderDtos.add(priceCalc.calculateReceivable(orderAllDto, Project.ReceivableDataSourceEnum.PACK_DATA).getBody());
            } else {
                List<LogisticsOrderAllDto> orderAllDtos = logisticsOrderService.findAllDtoListByOrderNos(packageDto.getOrderNos()).getBody();
                if (orderAllDtos != null && orderAllDtos.size() > 0) {
                    for (int i = 0; i < orderAllDtos.size(); i++) {
                        if (i == 0) {
                            orderAllDtos.get(i).setTotalWeight(new BigDecimal(weight));
                            orderAllDtos.get(i).setTotalVolume(new BigDecimal(volume));
                            orderAllDtos.get(i).setTotalPackageQty(packages.size());
                            orderAllDtos.get(i).setTotalItemQty(itemQty);
                        } else {

                            orderAllDtos.get(i).setTotalWeight(new BigDecimal(0));
                            orderAllDtos.get(i).setTotalVolume(new BigDecimal(0));
                            orderAllDtos.get(i).setTotalPackageQty(0);
                            orderAllDtos.get(i).setTotalItemQty(0);
                        }
                    }
                }
                receivableOrderDtos.addAll(priceCalc.calculateReceivable(orderAllDtos, Project.ReceivableDataSourceEnum.PACK_DATA).getBody());
            }
            //endregion
            receivableOrderDtos.removeIf(receivableOrderDto -> null == receivableOrderDto);
            List<String> orderNos = receivableOrderDtos.stream().map(ReceivableOrderDto::getOrderNo).collect(Collectors.toList());
            //region 判断是否是二次打包，二次打包如果有应付，则删除
            if (orderNos != null && orderNos.size() > 0) {
                ListResponse<Receivable> response1 = receivableService.findByOrderNos(orderNos);
                Assert.isTrue(response1.isSuccess(), response1.getMessage());
                if (response1.getBody() != null && response1.getBody().size() > 0) {
                    List<String> receivableIds = response1.getBody().stream().map(Receivable::getReceivableId).collect(Collectors.toList());
                    Response r = receivableService.deleteReceivable(receivableIds);
                    Assert.isTrue(r.isSuccess(), r.getMessage());
                }
                Response receivableOrderDtoResponse = receivableService.batchCreate(receivableOrderDtos);
                Assert.isTrue(receivableOrderDtoResponse.isSuccess(), receivableOrderDtoResponse.getMessage());
            }
            //endregion*/
            return merge(packageDto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }
    @Override
    public Response<List<OrderPackingInfo>> batchConfirm(List<OrderPackingInfo> orderPacks) {
        try {
            Assert.notEmpty(orderPacks);
            List<String> orderNos = orderPacks.stream().map(OrderPackingInfo::getOrderNo).collect(Collectors.toList());
            List<OrderPackingInfo> oldPackingInfos = orderPackingInfoMapper.findByOrderNo(orderNos);
            List<String> hasPackages = oldPackingInfos.stream().filter(info -> StringUtils.isEmpty(info.getPackageId()))
                    .map(OrderPackingInfo::getOrderNo).collect(Collectors.toList());
            if (hasPackages != null && hasPackages.size() > 0) {
                throw new Exception("订单" + String.join(",", hasPackages) + "已打包，不能批量确认");
            }
            Assert.isTrue(orderPackingInfoMapper.batchInsert(orderPacks) == orderPacks.size(), "批量合并失败");
            return ResponseBuilder.success(orderPacks);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<OrderPackingInfo> getPackageInfo(String orderNo) {
        try {
            return ResponseBuilder.success(orderPackingInfoMapper.selectById(orderNo));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public PageResponse<Package> pagePackage(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<Package> listPackages = packageMapper.pageList(params);
            PageInfo pageInfo = new PageInfo<>(listPackages);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public PageResponse<PackageRecordDto> pagePackageRecord(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<PackageRecordDto> listPackages = packageMapper.pagePackageRecord(params);
            PageInfo pageInfo = new PageInfo<>(listPackages);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public ListResponse<PackageMonthDto> findGroupByMonth(String packageUser) {
        try {
            Assert.notNull(packageUser);
            List<PackageMonthDto> list = packageRecordMapper.searchGroupByMonth(packageUser);
            return ResponseBuilder.list(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public PageResponse<OrderPackageDto> pageOrderPacking(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<OrderPackageDto> list = orderPackingInfoMapper.pageList(params);
            PageInfo pageInfo = new PageInfo<>(list);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public ListResponse<OrderPackageRecordDto> findOrderPacking(String orderNo) {
        try {
            List<OrderPackageRecordDto> list = orderPackageMapper.findByOrderNo(orderNo);
            for (OrderPackageRecordDto dto : list) {
                dto.setPackageList(packageMapper.findPackByOrderNo(dto.getOrderNo()));
            }
            return ResponseBuilder.list(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<OrderPackageRecordDto> findOrderPacking(List<String> orderNos) {
        try {
            List<OrderPackageRecordDto> list = orderPackageMapper.findRecordByOrderNos(orderNos);
            if (list != null && list.size() > 0) {
                for (OrderPackageRecordDto dto : list) {
                    dto.setPackageList(packageMapper.findPackByOrderNo(dto.getOrderNo()));
                }
            }
            return ResponseBuilder.list(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public PageResponse<PackageRecord> pageRecord(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<PackageRecord> list = packageRecordMapper.pageList(params);
            PageInfo pageInfo = new PageInfo<>(list);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public Response<PackageDto> getByPackageId(String packageId) {
        try {
            PackageDto result = new PackageDto();
            List<Package> packages = packageMapper.getByPackageId(packageId);
            List<PackageRecord> records = packageRecordMapper.findByPackageId(packageId);
            List<String> packers = records.stream().map(PackageRecord::getPackingUser).collect(Collectors.toList());
            List<OrderPackage> orderPackageList = orderPackageMapper.findByPackageId(packageId);
            List<String> orderNos = orderPackageList.stream().map(OrderPackage::getOrderNo).collect(Collectors.toList());
            result.setPackages(packages);
            result.setPackers(packers);
            result.setOrderNos(orderNos);
            return ResponseBuilder.success(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response getTodayPackage(String branchCode, String siteCode) {
        return ResponseBuilder.success(packageRecordMapper.getTodayPackageCount(branchCode, siteCode));
    }

    @Override
    public Response<PackageDto> getPackageById(String packageId) {
        try {
            PackageDto result = new PackageDto();
            List<Package> packages = packageMapper.getByPackageId(packageId);
            List<PackageRecord> records = packageRecordMapper.findByPackageId(packageId);
            List<String> packers = records.stream().map(PackageRecord::getPackingUser).collect(Collectors.toList());
            List<OrderPackage> orderPackageList = orderPackageMapper.findByPackageId(packageId);
            List<String> orderNos = orderPackageList.stream().map(OrderPackage::getOrderNo).collect(Collectors.toList());
            result.setPackages(packages);
            result.setPackers(packers);
            result.setOrderNos(orderNos);
            return ResponseBuilder.success(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public ListResponse<ConfirmPackageDto> findOrderSummary(List<String> orderNos) {
        try{
            List<Customer> customers = customerService.getAvailable().getBody();
            Response<List<LogisticsOrderAllDto>> summaryList = logisticsOrderService.findAllDtoListByOrderNos(orderNos);
            if (summaryList.isSuccess()) {
                List<ConfirmPackageDto> list = summaryList.getBody().stream().map(summary -> {
                    ConfirmPackageDto dto = new ConfirmPackageDto();
                    if (summary.getCustomerCode() != null) {
                        Customer c = ObjectQuery.findOne(customers, "code", summary.getCustomerCode());
                        if (null != c) dto.setCustomerName(c.getName());
                    }
                    dto.setPackageQty(summary.getTotalPackageQty());
                    dto.setOrderNo(summary.getOrderNo());
                    dto.setVolume(summary.getTotalVolume());
                    dto.setWeight(summary.getTotalWeight());
                    dto.setCustomerCode(summary.getCustomerCode());
                    dto.setCustomerOrderNo(summary.getCustomerOrderNo());
                    return dto;
                }).collect(Collectors.toList());
                return ResponseBuilder.list(list);
            }
            return ResponseBuilder.listFail("");
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseBuilder.listFail(e);
        }

    }

    @Override
    public ListResponse<String> judgeOrdersIsHavePacked(List<String> orderNos) {
        try {
            Assert.notEmpty(orderNos);
            ListResponse<LogisticsOrder> orderListResponse = logisticsOrderService.findByNos(orderNos);
            Assert.isTrue(orderListResponse.isSuccess());
            List<LogisticsOrder> orders = orderListResponse.getBody();
            //判断是订单，再判断是否打包，提货指令跳过判断
            if (orders != null && orders.size() > 0) {
                orderNos = orders.stream().map(LogisticsOrder::getOrderNo).collect(Collectors.toList());
                ListResponse<OrderPackageRecordDto> recordDtoListResponse = findOrderPacking(orderNos);
                Assert.isTrue(recordDtoListResponse.isSuccess());
                List<OrderPackageRecordDto> orderPackageRecordDtos = recordDtoListResponse.getBody();
                if (orderPackageRecordDtos != null && orderPackageRecordDtos.size() > 0) {
                    List<String> notPacked = new ArrayList<>();
                    orderNos.stream().forEach(orderNo -> {
                        OrderPackageRecordDto recordDto = ObjectQuery.findOne(orderPackageRecordDtos, "orderNo", orderNo);
                        if (recordDto == null) {
                            notPacked.add(orderNo);
                        }
                    });
                    return ResponseBuilder.list(notPacked);
                }
                return ResponseBuilder.list(orderNos);
            } else {
                return ResponseBuilder.list(new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    private String getPackageNo() {
        LocalDate date = LocalDate.now();
        String[] months = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M"};
        String prefix = "P" + String.valueOf(date.getYear()).substring(2);
        prefix += months[date.getMonth().getValue() - 1];
        return prefix + String.format("%012d", Integer.parseInt(packageMapper.getPackageNo()));
    }
}

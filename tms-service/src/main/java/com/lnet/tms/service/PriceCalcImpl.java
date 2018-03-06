package com.lnet.tms.service;

import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.model.cnaps.*;
import com.lnet.model.cnaps.payDto.ReceivableOrderDto;
import com.lnet.model.cnaps.payEntity.*;
import com.lnet.model.ums.customer.customerDto.PriceCalcDto;
import com.lnet.model.ums.customer.customerDto.PriceDto;
import com.lnet.model.ums.customer.customerDto.PriceRangeDto;
import com.lnet.model.ums.customer.customerEntity.Project;
import com.lnet.model.ums.expense.ExpenseAccount;
import com.lnet.model.ums.transprotation.transprotationDto.LogisticsOrderAllDto;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import com.lnet.model.tms.consign.consignEntity.ConsignOrderItem;
import com.lnet.tms.contract.spi.consgin.PriceCalc;
import com.lnet.ums.contract.api.ExpenseAccountService;
import com.lnet.ums.contract.api.PriceService;
import com.lnet.ums.contract.api.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/11/7.
 */
@Service
@Slf4j
public class PriceCalcImpl implements PriceCalc {

    public static final String RECEIVABLE = "110000";
    public static final String PAYABLE = "120100";
    @Resource
    private PriceService priceService;

    @Resource
    ExpenseAccountService expenseAccountService;

    @Resource
    ProjectService projectService;

    public Response<ReceivableOrderDto> calculateReceivable(LogisticsOrderAllDto order, Project.ReceivableDataSourceEnum receivableSource) {
        try {
            ReceivableOrderDto receivable = null;
            Response<Project> projectResponse = projectService.getProject(order.getBranchCode(), order.getCustomerCode());
            Assert.isTrue(null != projectResponse.getBody(), "项目不存在");
            if (null != projectResponse.getBody() && null != projectResponse.getBody().getReceivableDataSource()
                    && projectResponse.getBody().getReceivableDataSource().equals(receivableSource)) {
                Project project = projectResponse.getBody();
                receivable = new ReceivableOrderDto();
                ReceivableCalc calc = ReceivableCalc.builder()
                        .deliveryCompany(order.getDeliveryCompany())
                        .deliveryContacts(order.getDeliveryContacts())
                        .destinationCode(order.getDestinationCode())
                        .orginCode(order.getOrginCode())
                        .orderDate(order.getOrderDate())
                        .orderNo(order.getOrderNo())
                        .totalItemQty(order.getTotalItemQty())
                        .totalPackageQty(order.getTotalPackageQty())
                        .totalWeight(order.getTotalWeight())
                        .totalVolume(order.getTotalVolume())
                        .transportType(order.getTransportType().name())
                        .build();
                receivable.setReceivableCalc(calc);
                receivable.setBillingCycle(project.getSettleCycle().name());
                receivable.setBranchCode(order.getBranchCode());
                receivable.setCustomerCode(order.getCustomerCode());
                receivable.setCreateUserId(order.getCreatedBy());
                receivable.setCalcAttr(order.getCalculateType().name());
                receivable.setSiteCode(order.getSiteCode());
                receivable.setPaymentType(project.getPaymentType().name());
                receivable.setOrderNo(order.getOrderNo());
                List<ExpenseAccount> expenseAccounts = expenseAccountService.findChildren(RECEIVABLE).getBody();
                Map<String, BigDecimal> account = null;
                BigDecimal totalAmount = new BigDecimal(0);
                switch (order.getCalculateType()) {
                    case WEIGHT:
                        account = priceService.calculate(new PriceCalcDto(order.getCustomerCode(), order.getOrginCode(), order.getDestinationCode(),
                                order.getCalculateType().name(), order.getTransportType().name(), order.getTotalVolume())).getBody();
                        break;
                    case VOLUME:
                        account = priceService.calculate(new PriceCalcDto(order.getCustomerCode(), order.getOrginCode(), order.getDestinationCode(),
                                order.getCalculateType().name(), order.getTransportType().name(), order.getTotalVolume())).getBody();
                        break;
                    case FREQUENCY://车次
//                    account = priceService.calculate(order.getCustomerCode(),order.getOrginCode(),order.getDestinationCode(),
//                        order.getCalculateType().name(),order.getTransportType().name(),null,null,order.getTotalVolume()).getBody();
                        break;
                    case AMOUNT:
                        account = priceService.calculate(new PriceCalcDto(order.getCustomerCode(), order.getOrginCode(), order.getDestinationCode(),
                                PriceDto.CalcAttr.PRODUCT_QTY.name(), order.getTransportType().name(), new BigDecimal(order.getTotalItemQty()))).getBody();
                    case VOLUME_OR_WEIGHT:
                        Map<String, BigDecimal> accByWeight = priceService.calculate(new PriceCalcDto(order.getCustomerCode(), order.getOrginCode(), order.getDestinationCode(),
                                PriceDto.CalcAttr.WEIGHT.name(), order.getTransportType().name(), order.getTotalWeight())).getBody();
                        Map<String, BigDecimal> accByVolume = priceService.calculate(new PriceCalcDto(order.getCustomerCode(), order.getOrginCode(), order.getDestinationCode(),
                                PriceDto.CalcAttr.VOLUME.name(), order.getTransportType().name(), order.getTotalVolume())).getBody();
                        if (null != accByVolume && null != accByWeight) {
                            BigDecimal totalByWeight = new BigDecimal(0);
                            BigDecimal totalByVolume = new BigDecimal(0);
                            for (Map.Entry<String, BigDecimal> decimalEntry : accByWeight.entrySet()) {
                                totalByWeight = totalByWeight.add(decimalEntry.getValue());
                            }
                            for (Map.Entry<String, BigDecimal> decimalEntry : accByVolume.entrySet()) {
                                totalByVolume = totalByVolume.add(decimalEntry.getValue());
                            }
                            if (totalByVolume.compareTo(totalByWeight) > 0) {//取较低的
                                receivable.setCalcAttr(PriceDto.CalcAttr.WEIGHT.name());
                                account = accByWeight;
                            } else {
                                receivable.setCalcAttr(PriceDto.CalcAttr.VOLUME.name());
                                account = accByVolume;
                            }
                        } else if (null == accByVolume) {
                            receivable.setCalcAttr(PriceDto.CalcAttr.WEIGHT.name());
                            account = accByWeight;
                        } else {
                            receivable.setCalcAttr(PriceDto.CalcAttr.VOLUME.name());
                            account = accByVolume;
                        }
                    case CARGO_TYPE_AND_VOLUME://// TODO: 2016/11/8
                        receivable.setCalcAttr(PriceDto.CalcAttr.VOLUME.name());
                        account = priceService.calculate(new PriceCalcDto(order.getCustomerCode(), order.getOrginCode(), order.getDestinationCode(),
                                PriceDto.CalcAttr.VOLUME.name(), order.getTransportType().name(), order.getItems().get(0).getGoodsName(), null, order.getTotalWeight())).getBody();
                        break;
                    default:
                        account = priceService.calculate(new PriceCalcDto(order.getCustomerCode(), order.getOrginCode(), order.getDestinationCode(),
                                PriceDto.CalcAttr.PRODUCT_QTY.name(), order.getTransportType().name(), new BigDecimal(order.getTotalItemQty()))).getBody();


                }
                List<ReceivableAccount> accounts = new ArrayList<>();
                if (null == account) account = new HashMap<>();
                for (Map.Entry<String, BigDecimal> decimalEntry : account.entrySet()) {
                    totalAmount = totalAmount.add(decimalEntry.getValue());
                }
                if (null != expenseAccounts) {
                    for (ExpenseAccount expenseAccount : expenseAccounts) {
                        BigDecimal amount = account.get(expenseAccount.getCode()) == null ? new BigDecimal(0) : account.get(expenseAccount.getCode());
                        accounts.add(ReceivableAccount.builder()
                                .calculateAmount(amount.setScale(2, RoundingMode.HALF_EVEN))
                                .accountCode(expenseAccount.getCode())
                                .amount(amount.setScale(2, RoundingMode.HALF_EVEN))
                                .build());
                    }
                }
                receivable.setCalculateAmount(totalAmount);
                receivable.setTotalAmount(totalAmount);
                receivable.setAccounts(accounts);
            }
            return ResponseBuilder.success(receivable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e);
        }
    }

    public Response<List<ReceivableOrderDto>> calculateReceivable(List<LogisticsOrderAllDto> orders, Project.ReceivableDataSourceEnum receivableSource) {
        try {
            Assert.notEmpty(orders);
            List<ReceivableOrderDto> result = new ArrayList<>();
            orders.forEach(consignOrder -> result.add(calculateReceivable(consignOrder, receivableSource).getBody()));
            return ResponseBuilder.list(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.listFail(e);
        }
    }

    public Response<Payable> calculatePayable(ConsignOrder consignOrder) {
        try {
            Assert.hasText(consignOrder.getCalculateType(), "计费类型不能为空");
            Assert.hasText(consignOrder.getCarrierCode(), "承运商不能为空");
            Assert.hasText(consignOrder.getStartCityCode(), "起始地不能为空");
            Assert.hasText(consignOrder.getDestCityCode(), "目的地不能为空");
            Payable payable = Payable.builder()
                    .billingCycle(consignOrder.getSettlementCycle())
                    .paymentType(consignOrder.getPaymentType())
                    .branchCode(consignOrder.getBranchCode())
                    .createUserId(consignOrder.getModifiedBy())
                    .ownerCode(consignOrder.getCarrierCode())
                    .ownerType(Payable.OwnerType.CARRIER)
                    .billingCycle(consignOrder.getSettlementCycle())
                    .siteCode(consignOrder.getSiteCode())
                    .calcAttr(consignOrder.getCalculateType())
                    .sourceNo(consignOrder.getConsignOrderNo())
                    .build();
            Map<String, BigDecimal> account = null;
            BigDecimal totalAmount = new BigDecimal(0);
            //region 计算应付
            switch (consignOrder.getCalculateType()) {
                case "WEIGHT":
                    payable.setCalcAttr(PriceDto.CalcAttr.WEIGHT.name());
                    account = priceService.calculate(new PriceCalcDto(consignOrder.getCarrierCode(), consignOrder.getStartCityCode(), consignOrder.getDestCityCode(),
                            PriceDto.CalcAttr.WEIGHT.name(), consignOrder.getTransportType(), consignOrder.getTotalVolume())).getBody();
                    break;
                case "CARGO_TYPE_AND_VOLUME"://// TODO: 2016/11/8
                case "VOLUME":
                    payable.setCalcAttr(PriceDto.CalcAttr.VOLUME.name());
                    account = priceService.calculate(new PriceCalcDto(consignOrder.getCarrierCode(), consignOrder.getStartCityCode(), consignOrder.getDestCityCode(),
                            PriceDto.CalcAttr.VOLUME.name(), consignOrder.getTransportType(), consignOrder.getTotalVolume())).getBody();
                    break;
                case "FREQUENCY"://车次
//                    account = priceService.calculate(consignOrder.getCustomerCode(),consignOrder.getStartCityCode(),consignOrder.getDestCityCode(),
//                        consignOrder.getCalculateType().name(),consignOrder.getTransportType().name(),null,null,consignOrder.getTotalVolume()).getBody();
                    break;
                case "AMOUNT":
                    payable.setCalcAttr(PriceDto.CalcAttr.PACKAGE_QTY.name());
                    account = priceService.calculate(new PriceCalcDto(consignOrder.getCarrierCode(), consignOrder.getStartCityCode(), consignOrder.getDestCityCode(),
                            PriceDto.CalcAttr.PACKAGE_QTY.name(), consignOrder.getTransportType(), new BigDecimal(consignOrder.getTotalPackageQuantity()))).getBody();
                    break;
                case "VOLUME_OR_WEIGHT":
                    Map<String, BigDecimal> accByWeight = priceService.calculate(new PriceCalcDto(consignOrder.getCarrierCode(), consignOrder.getStartCityCode(), consignOrder.getDestCityCode(),
                            PriceDto.CalcAttr.WEIGHT.name(), consignOrder.getTransportType(), consignOrder.getTotalWeight())).getBody();
                    Map<String, BigDecimal> accByVolume = priceService.calculate(new PriceCalcDto(consignOrder.getCarrierCode(), consignOrder.getStartCityCode(), consignOrder.getDestCityCode(),
                            PriceDto.CalcAttr.VOLUME.name(), consignOrder.getTransportType(), consignOrder.getTotalVolume())).getBody();
                    if (accByVolume != null && accByWeight != null) {
                        BigDecimal totalByWeight = new BigDecimal(0);
                        BigDecimal totalByVolume = new BigDecimal(0);
                        for (Map.Entry<String, BigDecimal> decimalEntry : accByWeight.entrySet()) {
                            totalByWeight = totalByWeight.add(decimalEntry.getValue());
                        }
                        for (Map.Entry<String, BigDecimal> decimalEntry : accByVolume.entrySet()) {
                            totalByVolume = totalByVolume.add(decimalEntry.getValue());
                        }
                        if (totalByVolume.compareTo(totalByWeight) < 0) {//取较高的
                            account = accByWeight;
                            payable.setCalcAttr(PriceDto.CalcAttr.WEIGHT.name());
                        } else {
                            account = accByVolume;
                            payable.setCalcAttr(PriceDto.CalcAttr.VOLUME.name());
                        }
                    } else if (accByVolume == null) {
                        payable.setCalcAttr(PriceDto.CalcAttr.WEIGHT.name());
                        account = accByWeight;
                    } else {
                        account = accByVolume;
                        payable.setCalcAttr(PriceDto.CalcAttr.VOLUME.name());
                    }
                    break;

                default:
                    payable.setCalcAttr(PriceDto.CalcAttr.VOLUME.name());
                    account = priceService.calculate(new PriceCalcDto(consignOrder.getCarrierCode(), consignOrder.getStartCityCode(), consignOrder.getDestCityCode(),
                            PriceDto.CalcAttr.VOLUME.name(), consignOrder.getTransportType(), consignOrder.getTotalVolume())).getBody();
            }
            //endregion
            if (account == null) account = new HashMap<>();
            for (Map.Entry<String, BigDecimal> decimalEntry : account.entrySet()) {
                totalAmount = totalAmount.add(decimalEntry.getValue());
            }
            List<PayableAccount> accounts = new ArrayList<>();
            List<ExpenseAccount> expenseAccounts = expenseAccountService.findChildren(PAYABLE).getBody();
            List<PayableProportion> proportions = new ArrayList<>();

            if (null != expenseAccounts) {
                //region 应付明细
                for (ExpenseAccount expenseAccount : expenseAccounts) {
                    BigDecimal amount = account.get(expenseAccount.getCode()) == null ? new BigDecimal(0) : account.get(expenseAccount.getCode());
                    accounts.add(PayableAccount.builder()
                            .accountCode(expenseAccount.getCode())
                            .amount(amount.setScale(2, RoundingMode.HALF_EVEN))
                            .calculateAmount(amount.setScale(2, RoundingMode.HALF_EVEN))
                            .build());
                }
                //endregion

                //region 创建分摊
                List<ConsignOrderItem> items = consignOrder.getItems();
                BigDecimal totalScale = new BigDecimal(0);
                for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
                    ConsignOrderItem consignOrderItem = items.get(i);
                    BigDecimal scale = new BigDecimal(0);
                    switch (payable.getCalcAttr()) {
                        case "WEIGHT":
                            scale = consignOrderItem.getWeight().divide(consignOrder.getTotalWeight(), 4, RoundingMode.HALF_EVEN);
                            break;
                        case "VOLUME":
                            scale = consignOrderItem.getVolume().divide(consignOrder.getTotalVolume(), 4, RoundingMode.HALF_EVEN);
                            break;
                        case "PACKAGE_QTY":
                            scale = new BigDecimal(consignOrderItem.getPackageQuantity().doubleValue() / consignOrder.getTotalPackageQuantity());
                            break;
                        default:
                            scale = BigDecimal.valueOf(1.0 / consignOrder.getItems().size());
                            break;
                    }
                    if (i == items.size() - 1) {
                        scale = new BigDecimal(1).subtract(totalScale);
                    }
                    Assert.isTrue(scale.compareTo(new BigDecimal(0)) >= 0, "分摊比例不能小于0");
                    totalScale = totalScale.add(scale);
                    BigDecimal finalScale = scale;
                    List<PayableProportionAccount> proportionAccounts = accounts.stream()
                            .map(payableAccount ->
                                    PayableProportionAccount.builder()
                                            .accountCode(payableAccount.getAccountCode())
                                            .amount(payableAccount.getCalculateAmount().multiply(finalScale).setScale(2, RoundingMode.HALF_EVEN))
                                            .build())
                            .collect(Collectors.toList());
                    proportions.add(PayableProportion.builder()
                            .amount(totalAmount.multiply(scale).setScale(2, RoundingMode.HALF_EVEN))
                            .orderNo(consignOrderItem.getOrderNo())
                            .proportionAccounts(proportionAccounts)
                            .proportionType(payable.getCalcAttr())
                            .scale(scale)
                            .build());
                }
                Assert.isTrue(totalScale.compareTo(new BigDecimal(1)) == 0, "未完全分摊");
                //endregion
            }
            payable.setCalculateAmount(totalAmount.setScale(2, RoundingMode.HALF_EVEN));
            payable.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_EVEN));
            payable.setAccounts(accounts);

            payable.setProportions(proportions);

            return ResponseBuilder.success(payable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e);
        }
    }

    public Response<List<Payable>> calculatePayable(List<ConsignOrder> consignOrders) {
        try {
            Assert.notEmpty(consignOrders);
            List<Payable> result = new ArrayList<>();
            consignOrders.forEach(consignOrder -> result.add(calculatePayable(consignOrder).getBody()));
            return ResponseBuilder.list(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.listFail(e);
        }
    }



    private BigDecimal getAmount(PriceDto priceDto, BigDecimal value) {
        PriceRangeDto rangeDto = priceService.findByPriceId(priceDto.getPriceId(), value).getBody();
        BigDecimal amount = new BigDecimal(0);
        if (rangeDto != null) {
            /*add 加  subtract 减    multiply 乘 divide除
        RANGED("区间"),
        INCREASED("续增累加"),
        UNITED("乘积");*/
            BigDecimal unit = rangeDto.getUnitPrice() == null ? new BigDecimal(0) : rangeDto.getUnitPrice();
            switch (priceDto.getCalcFormula()) {
                case INCREASED://续增
                    if (value.compareTo(rangeDto.getRangeStart()) > 0) {
                        amount = rangeDto.getMinAmount().add(value.subtract(rangeDto.getRangeStart()).multiply(unit));
                    } else {
                        amount = rangeDto.getMinAmount();
                    }
                    break;
                case RANGED:
                    amount = value.multiply(unit);
                    amount = amount.compareTo(rangeDto.getMinAmount()) > 0 ? amount : rangeDto.getMinAmount();
                    break;
                case UNITED:
                    amount = value.multiply(unit);
                    break;
            }
        }
        return amount;
    }

    /**
     * 根据起始地目的地过滤,详细地址查询不到时默认查询上级地址
     *
     * @param priceDtos
     * @param orgin
     * @param dest
     * @return
     */
    private PriceDto filterByDistrict(List<PriceDto> priceDtos, String orgin, String dest) {
        Integer[] length = new Integer[]{2, 4, 6, 9};
        for (int i = 0; i < length.length; i++) {
            final int finalI = length[i];
            if (finalI <= orgin.length()) {
                for (int j = 0; j < length.length; j++) {
                    final int finalJ = length[j];
                    if (finalJ <= dest.length()) {
                        List<PriceDto> result = new ArrayList<>();
                        result = priceDtos.stream().filter(priceDto ->
                                orgin.substring(0, finalI).equals(priceDto.getOrgin().substring(0, finalI))
                                        && dest.substring(0, finalJ).equals(priceDto.getDestination().substring(0, finalJ))
                        )
                                .collect(Collectors.toList());

                        if (result.size() == 1) {
                            return result.get(0);
                        }
                    }
                }
            }
        }
        return null;
    }

}

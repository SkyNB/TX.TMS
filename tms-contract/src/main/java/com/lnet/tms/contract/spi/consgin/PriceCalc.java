package com.lnet.tms.contract.spi.consgin;

import com.lnet.framework.core.Response;
import com.lnet.model.cnaps.payDto.ReceivableOrderDto;
import com.lnet.model.cnaps.payEntity.Payable;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import com.lnet.model.ums.customer.customerEntity.Project;
import com.lnet.model.ums.transprotation.transprotationDto.LogisticsOrderAllDto;

import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */
public interface PriceCalc {

    Response<ReceivableOrderDto> calculateReceivable(LogisticsOrderAllDto order, Project.ReceivableDataSourceEnum receivableSource);

    Response<List<ReceivableOrderDto>> calculateReceivable(List<LogisticsOrderAllDto> orders, Project.ReceivableDataSourceEnum receivableSource);

    Response<Payable> calculatePayable(ConsignOrder consignOrder);

    Response<List<Payable>> calculatePayable(List<ConsignOrder> consignOrders);
}

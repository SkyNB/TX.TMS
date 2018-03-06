package com.lnet.tms.mapper;

import com.lnet.model.tms.consign.consignEntity.OrderConsignPackageQty;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderConsignPackageQtyMapper {

    int insert(OrderConsignPackageQty record);

    int batchInsert(List<OrderConsignPackageQty> records);

    int batchUpdateConsigningQty(List<OrderConsignPackageQty> records);

    int batchUpdateConsignedQty(List<OrderConsignPackageQty> records);

    int batchUpdateStartUpQty(List<OrderConsignPackageQty> records);

    int batchUpdateArriveQty(List<OrderConsignPackageQty> records);

    OrderConsignPackageQty getBySiteAndOrderNo(@Param("siteCode") String siteCode, @Param("orderNo") String orderNo);

    List<OrderConsignPackageQty> findBySiteAndOrderNo(@Param("siteCode") String siteCode, @Param("orderNos") List<String> orderNos);
}
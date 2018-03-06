package com.lnet.tms.mapper;


import com.lnet.model.tms.dispatch.dispatchEntity.OrderDispatchPackageQty;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDispatchPackageQtyMapper {

    int insert(OrderDispatchPackageQty record);

    int batchInsert(List<OrderDispatchPackageQty> records);

    int batchUpdate(List<OrderDispatchPackageQty> records);

    int batchUpdateDispatchingQty(List<OrderDispatchPackageQty> records);

    int batchUpdateDistributionInQty(List<OrderDispatchPackageQty> records);

    int batchUpdateCollectingQty(List<OrderDispatchPackageQty> records);

    int batchUpdateCollectedQty(List<OrderDispatchPackageQty> records);

    List<OrderDispatchPackageQty> findByOrderNos(@Param("siteCode") String siteCode, @Param("orderNos") List<String> orderNos);
}
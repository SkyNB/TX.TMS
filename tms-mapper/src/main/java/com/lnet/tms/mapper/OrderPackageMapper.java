package com.lnet.tms.mapper;


import com.lnet.model.tms.pack.packDto.OrderPackageRecordDto;
import com.lnet.model.tms.pack.packEntity.OrderPackage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderPackageMapper {
    int deleteById(String itemId);

    int insert(OrderPackage record);

    OrderPackage selectById(String itemId);

    int update(OrderPackage record);

    int batchInsert(List<OrderPackage> orderPackages);

    List<OrderPackage> findByOrderNos(List<String> orderNos);

    /**
     * 查询和该订单打成一包的单
     *
     * @param orderNo
     * @return
     */
    List<OrderPackageRecordDto> findByOrderNo(String orderNo);

    List<OrderPackageRecordDto> findRecordByOrderNos(List<String> orderNos);

    List<OrderPackage> findByPackageId(String packageId);

    int deleteByNo(List<String> orderNos);
}
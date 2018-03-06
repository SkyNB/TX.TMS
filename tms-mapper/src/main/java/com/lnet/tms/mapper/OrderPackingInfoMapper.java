package com.lnet.tms.mapper;

import com.lnet.model.tms.pack.packDto.OrderPackageDto;
import com.lnet.model.tms.pack.packEntity.OrderPackingInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderPackingInfoMapper {
    int deleteById(String orderNo);

    int insert(OrderPackingInfo record);

    OrderPackingInfo selectById(String orderNo);

    int update(OrderPackingInfo record);

    int batchInsert(List<OrderPackingInfo> orderPackingInfos);


    List<OrderPackingInfo> findByOrderNo(List<String> orderNos);

    List<OrderPackageDto> pageList(Map<String, Object> params);

    List<OrderPackageDto> findPackageByOrderNo(String orderNo);

    void deleteByIds(List<String> orderNos);
}
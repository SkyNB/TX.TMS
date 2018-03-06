package com.lnet.tms.mapper;


import com.lnet.model.tms.dispatch.dispatchEntity.DispatchItem;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchItemPayableDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DispatchItemMapper {

    int insert(DispatchItem record);

    int batchInsert(List<DispatchItem> records);

    List<DispatchItem> findByDispatchId(String dispatchId);

    List<DispatchItem> findByDispatchIds(List<String> dispatchIds);

    List<DispatchItem> findByDispatchNumber(String dispatchNumber);

    int delete(String dispatchItemId);

    int batchDelete(List<String> dispatchItemIds);

    int deleteByDispatchId(String dispatchId);

    int update(DispatchItem record);

    int batchUpdate(List<DispatchItem> records);

    List<DispatchItem> findByOrderNosAndSiteCode(@Param("siteCode") String siteCode, @Param("orderNos") List<String> orderNos);

    List<DispatchItemPayableDto> pageList(Map<String, Object> params);
}
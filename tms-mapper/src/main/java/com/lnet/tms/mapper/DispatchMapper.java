package com.lnet.tms.mapper;


import com.lnet.model.tms.dispatch.dispatchDto.DispatchDto;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchMonthDto;
import com.lnet.model.tms.dispatch.dispatchEntity.Dispatch;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DispatchMapper {

    int insert(Dispatch record);

    Dispatch get(String dispatchId);

    Dispatch getByNo(String dispatchNo);

    int update(Dispatch record);

    List<DispatchDto> pageList(Map<String, Object> params);

    Long getDispatchSequenceNo();

    int updateStatus(@Param("dispatchId") String dispatchId, @Param("status") Dispatch.statusEnum status);

    List<Dispatch> getByVehicleIdAndStatus(@Param("vehicleId") String vehicleId, @Param("status") Dispatch.statusEnum status);

    int batchUpdateStatus(@Param("dispatchIds") List<String> dispatchIds, @Param("status") Dispatch.statusEnum status);

    List<Dispatch> searchDispatch(@Param("branchCode") String branchCode, @Param("siteCode") String siteCode,
                                  @Param("condition") String condition, @Param("vehicleId") String vehicleId,
                                  @Param("assignDate") String assignDate);

    List<DispatchMonthDto> searchGroupByMonth(String createdBy);

    List<Dispatch> findByOrderNoAndSiteCode(@Param("siteCode") String siteCode, @Param("orderNo") String orderNo);

    Integer getTodayDispatch(@Param("branchCode") String branchCode, @Param("siteCode") String siteCode);

    List<Dispatch> getByVehicleIdsAndStatus(@Param("vehicleIds") List<String> vehicleIds, @Param("status") Dispatch.statusEnum status);

}
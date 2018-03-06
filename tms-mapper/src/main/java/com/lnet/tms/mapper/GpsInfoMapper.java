package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.GpsInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpsInfoMapper {

    int insert(GpsInfo record);

    int batchCreate(List<GpsInfo> infos);

    GpsInfo getLastByUser(String userId);

    List<GpsInfo> findByUser(String userId);
    List<GpsInfo> findAllByUser(String userId);

    List<GpsInfo> findByDispatchNo(String dispatchNumber);

    GpsInfo get(String gpsId);

    List<GpsInfo> findLastByUsers(List<String> userIds);

    GpsInfo getLastByName(String name);

    List<GpsInfo> findLastByDispatchNos(List<String> dispatchNos);
}
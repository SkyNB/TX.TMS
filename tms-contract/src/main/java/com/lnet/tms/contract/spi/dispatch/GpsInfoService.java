package com.lnet.tms.contract.spi.dispatch;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.dispatch.dispatchEntity.GpsInfo;

import java.util.List;

/**
 * Created by Administrator on 2016/10/29.
 */
public interface GpsInfoService {
    /**
     * 添加GPS信息
     * @param gpsInfo
     * @return
     */
    Response<GpsInfo> create(GpsInfo gpsInfo);

    Response<List<GpsInfo>> batchCreate(List<GpsInfo> gpsInfos);

    /**
     * 查询用户最后一条位置信息
     * @param userId
     * @return
     */
    Response<GpsInfo> getLastByUserId(String userId);
    /**
     * 查询用户最后一条位置信息
     * @param name
     * @return
     */
    Response<GpsInfo> getLastByName(String name);

    /**
     * 查询多人最后一条位置信息
     * @param userIds
     * @return
     */
    ListResponse<GpsInfo> findLastByUserIds(List<String> userIds);
    /**
     * 查询多个派车单最后一条位置信息
     * @param dispatchNos
     * @return
     */
    ListResponse<GpsInfo> findLastByDispatchNos(List<String> dispatchNos);

    /**
     * 查讯派单路径信息
     * @param dispatchNo
     * @return
     */
    ListResponse<GpsInfo> findByDispatchNo(String dispatchNo);

}

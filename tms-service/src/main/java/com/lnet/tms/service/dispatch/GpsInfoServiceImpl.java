package com.lnet.tms.service.dispatch;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.tms.dispatch.dispatchEntity.GpsInfo;
import com.lnet.tms.contract.spi.dispatch.GpsInfoService;
import com.lnet.tms.mapper.GpsInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/10/29.
 */
@Service
@Slf4j
@Transactional
public class GpsInfoServiceImpl implements GpsInfoService {
    @Resource
    GpsInfoMapper gpsInfoMapper;

    @Override
    public Response<GpsInfo> create(GpsInfo gpsInfo) {
        try {
            Assert.notNull(gpsInfo);
            gpsInfo.setGpsId(Snowflake.getInstance().next());
            Assert.isTrue(gpsInfoMapper.insert(gpsInfo) > 0, "添加GPS信息失败");
            return ResponseBuilder.success(gpsInfo);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<List<GpsInfo>> batchCreate(List<GpsInfo> gpsInfos) {
        try {
            Assert.notEmpty(gpsInfos);
            gpsInfos.forEach(gpsInfo -> gpsInfo.setGpsId(Snowflake.getInstance().next()));
            Assert.isTrue(gpsInfoMapper.batchCreate(gpsInfos) > 0, "添加GPS信息失败");
            return ResponseBuilder.success(gpsInfos);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<GpsInfo> getLastByUserId(String userId) {
        try {
            Assert.hasText(userId);
            return ResponseBuilder.success(gpsInfoMapper.getLastByUser(userId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<GpsInfo> getLastByName(String name) {
        try {
            Assert.hasText(name);
            return ResponseBuilder.success(gpsInfoMapper.getLastByName(name));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<GpsInfo> findLastByUserIds(List<String> userIds) {
        try {
            Assert.notEmpty(userIds);
            Set<String> ids = new HashSet<>(userIds);
            userIds = new ArrayList<>(ids);
            return ResponseBuilder.list(gpsInfoMapper.findLastByUsers(userIds));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<GpsInfo> findLastByDispatchNos(List<String> dispatchNos) {
        try {
            Assert.notEmpty(dispatchNos);
            Set<String> ids = new HashSet<>(dispatchNos);
            dispatchNos = new ArrayList<>(ids);
            return ResponseBuilder.list(gpsInfoMapper.findLastByDispatchNos(dispatchNos));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public ListResponse<GpsInfo> findByDispatchNo(String dispatchNo) {
        try {
            return ResponseBuilder.list(gpsInfoMapper.findByDispatchNo(dispatchNo));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBuilder.listFail(e);
        }
    }
}

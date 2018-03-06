package com.lnet.tms.contract.spi.pack;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.pack.packDto.*;
import com.lnet.model.tms.pack.packEntity.OrderPackingInfo;
import com.lnet.model.tms.pack.packEntity.PackageRecord;
import com.lnet.model.tms.pack.packEntity.Package;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/22.
 */

public interface PackageService {

    /**
     * 合单打包
     *
     * @param pack
     * @return
     */
    Response<PackageDto> mergePackage(PackageDto pack);

    /**
     * 批量确认
     *
     * @param OrderPacks
     * @return
     */
    Response<List<OrderPackingInfo>> batchConfirm(List<OrderPackingInfo> OrderPacks);

    /**
     * 通过单号查询打包详情
     *
     * @param orderNo
     * @return
     */
    Response<OrderPackingInfo> getPackageInfo(String orderNo);

    /**
     * 分页查询包
     *
     * @param page
     * @param pageSize
     * @param params
     * @return
     */
    PageResponse<Package> pagePackage(Integer page, Integer pageSize, Map<String, Object> params);

    PageResponse<PackageRecordDto> pagePackageRecord(Integer page, Integer pageSize, Map<String, Object> params);

    /**
     * 按月统计每人打包数量
     * @param packageUser
     * @return
     */
    ListResponse<PackageMonthDto> findGroupByMonth(String packageUser);

    /**
     * 分页查询已打包的单  的信息
     *
     * @param page
     * @param pageSize
     * @param params
     * @return
     */
    PageResponse<OrderPackageDto> pageOrderPacking(Integer page, Integer pageSize, Map<String, Object> params);

    /**
     * 根据单号查询打包情况
     *
     * @param orderNo
     * @return
     */
    ListResponse<OrderPackageRecordDto> findOrderPacking(String orderNo);

    /**
     * 根据单号批量查询打包情况
     *
     * @param orderNos
     * @return
     */
    ListResponse<OrderPackageRecordDto> findOrderPacking(List<String> orderNos);

    /**
     * 分页查询打包记录
     *
     * @param page
     * @param pageSize
     * @param params
     * @return
     */
    PageResponse<PackageRecord> pageRecord(Integer page, Integer pageSize, Map<String, Object> params);

    /**
     * 查询打包详细信息
     *
     * @param packageId
     * @return
     */
    Response<PackageDto> getByPackageId(String packageId);

    /**
     * 查询当天打包数量
     * @param branchCode
     * @param siteCode
     * @return
     */
    Response getTodayPackage(String branchCode, String siteCode);

    Response<PackageDto> getPackageById(String packageId);

    ListResponse<ConfirmPackageDto> findOrderSummary(List<String> orderNos);


    ListResponse<String> judgeOrdersIsHavePacked(List<String> orderNos);
}

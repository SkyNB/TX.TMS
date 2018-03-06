package com.lnet.tms.mapper;

import com.lnet.model.tms.pack.packDto.PackageMonthDto;
import com.lnet.model.tms.pack.packEntity.PackageRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PackageRecordMapper {
    int deleteById(String recordId);

    int deleteByNos(List<String> orderNos);
    int insert(PackageRecord record);
    PackageRecord selectById(String recordId);
    List<PackageRecord> findByPackageId(String packageId);
    int update(PackageRecord record);

    int batchInsert(List<PackageRecord> records);

    List<PackageRecord> pageList(Map<String, Object> params);

    List<PackageMonthDto> searchGroupByMonth(String packageUser);


    Integer getTodayPackageCount(@Param("branchCode") String branchCode, @Param("siteCode") String siteCode);
}
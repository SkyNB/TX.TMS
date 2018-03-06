package com.lnet.tms.mapper;

import com.lnet.model.tms.pack.packDto.PackageRecordDto;
import org.springframework.stereotype.Repository;
import com.lnet.model.tms.pack.packEntity.Package;
import java.util.List;
import java.util.Map;


@Repository
public interface PackageMapper {
    int deleteById(String packageId);
    int insert(Package record);
    Package selectById(String packageId);
    List<Package> getByPackageId(String packageId);
    int update(Package record);
    String getPackageNo();

    int batchInsert(List<Package> packages);
    List<Package> findPackByOrderNos(List<String> orderNos);
    List<Package> findPackByOrderNo(String orderNo);

    List<Package> pageList(Map<String, Object> params);


    List<PackageRecordDto> pagePackageRecord(Map<String, Object> params);

    int deleteByOrderNo(String orderNo);

    int deleteByOrderNos(List<String> orderNos);
}
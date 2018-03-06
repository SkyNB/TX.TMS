package com.lnet.tms.mapper;

import com.lnet.model.tms.dispatch.dispatchEntity.DispatchPackage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchPackageMapper {

    int insert(DispatchPackage dispatchPackage);

    int update(DispatchPackage dispatchPackage);

    int batchInsert(List<DispatchPackage> dispatchPackages);

    List<DispatchPackage> findByDispatchId(String dispatchId);

    int delete(String dispatchPackageId);

    int batchDelete(List<String> dispatchPackageIds);

    int deleteByDispatchId(String dispatchId);

    int batchUpdate(List<DispatchPackage> dispatchPackages);
}
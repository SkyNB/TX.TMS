package com.lnet.tms.mapper;

import com.lnet.model.tms.exception.exceptionEntity.OperationException;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ExceptionMapper {
    int insert(OperationException operationException);

    int updateStatusByCode(@Param("code") String code, @Param("status") OperationException.Status status, @Param("processor") String processor);

    int updateStatusByOrderNo(@Param("orderNo") String orderNo, @Param("status") OperationException.Status status, @Param("processor") String processor);

    OperationException get(String id);

    OperationException getByCode(String code);

    List<OperationException> findByOrderNo(String orderNo);

    List<OperationException> pageList(Map<String, Object> params);

    String getExceptionCode();

    int close(OperationException operationException);
}

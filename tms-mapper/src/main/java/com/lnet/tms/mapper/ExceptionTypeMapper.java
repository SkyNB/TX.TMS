package com.lnet.tms.mapper;

import com.lnet.model.tms.exception.exceptionEntity.ExceptionType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ExceptionTypeMapper {
    int insert(ExceptionType exceptionType);

    int update(ExceptionType exceptionType);

    boolean exists(String code);

    ExceptionType get(String id);

    ExceptionType getByCode(String code);

    List<ExceptionType> getAll();

    List<ExceptionType> search(Map<String, Object> params);
}

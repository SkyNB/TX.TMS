package com.lnet.tms.contract.spi;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.exception.exceptionDto.ExceptionTypeDto;

import java.util.Map;

public interface ExceptionTypeService {
    Response<String> create(ExceptionTypeDto dto);

    Response<String> update(ExceptionTypeDto dto);

    Response<ExceptionTypeDto> get(String id);

    Response<ExceptionTypeDto> getByCode(String code);

    ListResponse<ExceptionTypeDto> getAll();

    ListResponse<ExceptionTypeDto> search(Map<String, Object> params);
}

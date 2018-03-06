package com.lnet.tms.contract.spi;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.exception.exceptionDto.ExceptionCloseDto;
import com.lnet.model.tms.exception.exceptionDto.ExceptionDto;
import com.lnet.model.tms.exception.exceptionDto.ExceptionListDto;

import java.util.Map;

public interface ExceptionService {
    /**
     * 记录异常信息
     *
     * @param exceptionDto
     * @return
     */
    Response<String> create(ExceptionDto exceptionDto);

    /**
     * 根据编码修改异常状态
     *
     * @param code
     * @param status
     * @param processor 异常处理人
     * @return
     */
    Response handleExceptionByCode(String code, ExceptionDto.Status status, String processor);

    /**
     * 根据新易泰单号修改异常状态
     *
     * @param orderNo
     * @param status
     * @param processor 异常处理人
     * @return
     */
    Response handleExceptionByOrderNo(String orderNo, ExceptionDto.Status status, String processor);

    /**
     * 关闭异常
     *
     * @param exceptionCloseDto
     * @return
     */
    Response closeException(ExceptionCloseDto exceptionCloseDto);

    /**
     * @param id
     * @return
     */
    Response<ExceptionListDto> get(String id);

    /**
     * @param code
     * @return
     */
    Response<ExceptionListDto> getByCode(String code);

    /**
     * @param orderNo
     * @return
     */
    ListResponse<ExceptionListDto> findByOrderNo(String orderNo);

    /**
     * @param pageNumber
     * @param pageSize
     * @param params
     * @return
     */
    PageResponse<ExceptionListDto> pageList(int pageNumber, int pageSize, Map<String, Object> params);
}

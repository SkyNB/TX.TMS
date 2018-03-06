package com.lnet.tms.service.exception;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lnet.framework.core.*;
import com.lnet.framework.util.BeanHelper;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.tms.exception.exceptionDto.ExceptionCloseDto;
import com.lnet.model.tms.exception.exceptionDto.ExceptionDto;
import com.lnet.model.tms.exception.exceptionDto.ExceptionListDto;
import com.lnet.model.tms.exception.exceptionEntity.ExceptionType;
import com.lnet.model.tms.exception.exceptionEntity.ExceptionTypeEnum;
import com.lnet.model.tms.exception.exceptionEntity.OperationException;
import com.lnet.model.tms.exception.exceptionEntity.OperationExceptionLog;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.tms.contract.spi.ExceptionService;
import com.lnet.tms.mapper.ExceptionLogMapper;
import com.lnet.tms.mapper.ExceptionMapper;
import com.lnet.tms.mapper.ExceptionTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ExceptionServiceImpl implements ExceptionService {
    @Autowired
    private ExceptionMapper exceptionMapper;
    @Autowired
    private ExceptionLogMapper exceptionLogMapper;
    @Autowired
    private ExceptionTypeMapper exceptionTypeMapper;
    @Autowired
    LogisticsOrderService logisticsOrderService;

    public PageResponse<ExceptionListDto> pageList(KendoGridRequest request) {
        try {
            PageHelper.startPage(request.getPage(), request.getPageSize());
            List<ExceptionType> allExceptionTypes = new ArrayList<>();

            //从数据库加载异常类型
            List<ExceptionType> dataBaseTypes = exceptionTypeMapper.getAll();
            allExceptionTypes.addAll(dataBaseTypes);

            //从系统预设中加载异常类型
            List<ExceptionType> sysTypes = new ArrayList<>();
            for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                ExceptionType exceptionType = ExceptionType.builder()
                        .code(typeEnum.getCode())
                        .name(typeEnum.getName())
                        .remark(typeEnum.getRemark())
                        .build();
                sysTypes.add(exceptionType);
            }
            allExceptionTypes.addAll(sysTypes);

            List<OperationException> exceptions = exceptionMapper.pageList(request.getParams());
            List<ExceptionListDto> dtos = exceptions.stream().map(m -> toExceptionListDto(m)).collect(Collectors.toList());
            //填充异常类型
            dtos.forEach(e -> {
                Optional<ExceptionType> typeOptional = allExceptionTypes.stream().filter(f -> f.getCode().equals(e.getTypeCode())).findFirst();
                if (typeOptional.isPresent()) {
                    ExceptionType type = typeOptional.get();
                    e.setTypeName(type.getName());
                }
            });

            PageInfo<ExceptionListDto> pageInfo = new PageInfo<>(dtos);

            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.pageFail(e.getMessage());
        }
    }

    public Response close(ExceptionCloseDto exceptionCloseDto) {
        try {
            Assert.notNull(exceptionCloseDto);
            Assert.hasText(exceptionCloseDto.getCode());

            OperationException operationException = BeanHelper.convert(exceptionCloseDto, OperationException.class);

            boolean isSuccess = exceptionMapper.close(operationException) > 0;
            if (!isSuccess)
                return ResponseBuilder.fail("关闭异常失败！");
            return ResponseBuilder.success("", "成功关闭异常！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<String> create(ExceptionDto exceptionDto) {
        try {
            Response<LogisticsOrder> logisticsOrderResponse = logisticsOrderService.getByOrderNo(exceptionDto.getOrderNo());
            if(logisticsOrderResponse.isSuccess() && logisticsOrderResponse.getBody() != null) {
                OperationException operationException = toOperationException(exceptionDto);
                //记录异常
                boolean isSuccess = exceptionMapper.insert(operationException) > 0;
                //给异常添加日志
                if (isSuccess) {
                    OperationExceptionLog log = buildLog(operationException.getCode(), operationException.getStatus(), operationException.getProcessor(), "记录异常");
                    isSuccess = exceptionLogMapper.insert(log) > 0;
                }
                if (isSuccess)
                    return ResponseBuilder.success(operationException.getCode(), "记录异常成功！");

                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseBuilder.fail("记录异常失败！");
            }else{
                return ResponseBuilder.fail("订单不存在，无法添加异常");
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response handleExceptionByCode(String code, ExceptionDto.Status status, String processor) {
        try {
            Assert.hasText(code);
            Assert.notNull(status);
            Assert.hasText(processor);

            //修改异常记录的状态
            boolean isSuccess = exceptionMapper.updateStatusByCode(code, OperationException.Status.valueOf(status.toString()), processor) > 0;

            //记录日志
            if (isSuccess) {
                OperationExceptionLog log = buildLog(code, OperationException.Status.valueOf(status.toString()), processor, "解决异常");
                isSuccess = exceptionLogMapper.insert(log) > 0;
            }

            if (isSuccess)
                return ResponseBuilder.success("", "解决异常成功！");

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail("解决异常失败！");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response handleExceptionByOrderNo(String orderNo, ExceptionDto.Status status, String processor) {
        try {
            Assert.hasText(orderNo);
            Assert.notNull(status);
            Assert.hasText(processor);

            //修改异常记录的状态
            boolean isSuccess = exceptionMapper.updateStatusByOrderNo(orderNo, OperationException.Status.valueOf(status.toString()), processor) > 0;

            //记录日志
            if (isSuccess) {
                List<OperationExceptionLog> logs = buildLogList(orderNo, OperationException.Status.valueOf(status.toString()), processor, "解决异常");
                isSuccess = exceptionLogMapper.batchInsert(logs) == logs.size();
            }

            if (isSuccess)
                return ResponseBuilder.success("", "解决异常成功！");

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail("解决异常失败！");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response closeException(ExceptionCloseDto exceptionCloseDto) {
        return null;
    }

    @Override
    public Response<ExceptionListDto> get(String exceptionId) {
        try {
            Assert.hasText(exceptionId);
            OperationException operationException = exceptionMapper.get(exceptionId);
            Assert.notNull(operationException, "获取数据失败！");
            ExceptionListDto dto = toExceptionListDto(operationException);

            //填充异常类型
            {
                boolean flag = false;

                //从系统预设中查找异常类型
                for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                    if (typeEnum.getCode().equals(operationException.getTypeCode())) {
                        flag = true;
                        dto.setTypeName(typeEnum.getName());
                    }
                }

                //系统预设中查找不到则从数据库中查找
                if (!flag) {
                    ExceptionType type = exceptionTypeMapper.getByCode(operationException.getTypeCode());
                    if (null != type)
                        dto.setTypeName(type.getName());
                }

            }

            return ResponseBuilder.success(dto, "获取数据成功！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<ExceptionListDto> getByCode(String code) {
        try {
            Assert.hasText(code);
            OperationException operationException = exceptionMapper.getByCode(code);
            Assert.notNull(operationException, "获取数据失败！");
            ExceptionListDto dto = toExceptionListDto(operationException);

            //填充异常类型
            {
                boolean flag = false;

                //从系统预设中查找异常类型
                for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                    if (typeEnum.getCode().equals(operationException.getTypeCode())) {
                        flag = true;
                        dto.setTypeName(typeEnum.getName());
                    }
                }

                //系统预设中查找不到则从数据库中查找
                if (!flag) {
                    ExceptionType type = exceptionTypeMapper.getByCode(operationException.getTypeCode());
                    if (null != type)
                        dto.setTypeName(type.getName());
                }
            }

            return ResponseBuilder.success(dto, "获取数据成功！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public ListResponse<ExceptionListDto> findByOrderNo(String orderNo) {
        try {
            Assert.hasText(orderNo);
            List<OperationException> exceptions = exceptionMapper.findByOrderNo(orderNo);
            Assert.notEmpty(exceptions);

            return ResponseBuilder.list(exceptions.stream().map(f -> toExceptionListDto(f)).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public PageResponse<ExceptionListDto> pageList(int pageNumber, int pageSize, Map<String, Object> params) {
        return null;
    }

    private OperationExceptionLog buildLog(String exceptionCode, OperationException.Status status, String operateUserName, String remark) {
        return OperationExceptionLog.builder()
                .id(Snowflake.getInstance().next())
                .exceptionCode(exceptionCode)
                .status(status)
                .remark(remark)
                .operateDate(LocalDateTime.now())
                .operateUserName(operateUserName)
                .build();
    }

    private List<OperationExceptionLog> buildLogList(String orderNo, OperationException.Status status, String operateUserName, String remark) {
        List<OperationException> operationExceptions = exceptionMapper.findByOrderNo(orderNo);

        return operationExceptions.stream().map(m -> {
            return OperationExceptionLog.builder()
                    .id(Snowflake.getInstance().next())
                    .exceptionCode(m.getCode())
                    .status(status)
                    .remark(remark)
                    .operateDate(LocalDateTime.now())
                    .operateUserName(operateUserName)
                    .build();
        }).collect(Collectors.toList());
    }

    private OperationException toOperationException(ExceptionDto exceptionDto) {
        return OperationException.builder()
                .id(Snowflake.getInstance().next())
                .code("OE" + exceptionMapper.getExceptionCode())
                .occurTime(exceptionDto.getOccurTime())
                .address(exceptionDto.getAddress())
                .branchCode(exceptionDto.getBranchCode())
                .siteCode(exceptionDto.getSiteCode())
                .classification(OperationException.Classification.valueOf(exceptionDto.getClassification().toString()))
                .typeCode(exceptionDto.getTypeCode())
                .personsResponsible(exceptionDto.getPersonsResponsible())
                .status(OperationException.Status.valueOf(exceptionDto.getStatus().toString()))
                .remark(exceptionDto.getRemark())
                .orderNo(exceptionDto.getOrderNo())
                .processor(exceptionDto.getProcessor())
                .build();
    }

    private ExceptionListDto toExceptionListDto(OperationException operationException) {
        return ExceptionListDto.builder()
                .id(operationException.getId())
                .code(operationException.getCode())
                .occurTime(operationException.getOccurTime())
                .address(operationException.getAddress())
                .branchCode(operationException.getBranchCode())
                .siteCode(operationException.getSiteCode())
                .classification(ExceptionDto.Classification.valueOf(operationException.getClassification().toString()))
                .typeCode(operationException.getTypeCode())
                .personsResponsible(operationException.getPersonsResponsible())
                .status(ExceptionDto.Status.valueOf(operationException.getStatus().toString()))
                .remark(operationException.getRemark())
                .orderNo(operationException.getOrderNo())
                .processor(operationException.getProcessor())
                .goodsValue(operationException.getGoodsValue())
                .compensationToCustomer(operationException.getCompensationToCustomer())
                .insurance(operationException.getInsurance())
                .damage(operationException.getDamage())
                .build();
    }
}

package com.lnet.tms.service.exception;

import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.BeanHelper;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.tms.exception.exceptionEntity.ExceptionType;
import com.lnet.model.tms.exception.exceptionDto.ExceptionTypeDto;
import com.lnet.model.tms.exception.exceptionEntity.ExceptionTypeEnum;
import com.lnet.tms.contract.spi.ExceptionTypeService;
import com.lnet.tms.mapper.ExceptionTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ExceptionTypeServiceImpl implements ExceptionTypeService {
    @Autowired
    private ExceptionTypeMapper exceptionTypeMapper;

    @Override
    public Response<String> create(ExceptionTypeDto exceptionTypeDto) {
        try {
            Assert.hasText(exceptionTypeDto.getCode());
            Assert.hasText(exceptionTypeDto.getName());

            //判断编码是否存在于系统预设类型中
            for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                if (typeEnum.getCode().equals(exceptionTypeDto.getCode()))
                    return ResponseBuilder.fail("编码已存在！");
            }

            //判断编码是否存在于数据库中
            boolean isExists = exceptionTypeMapper.exists(exceptionTypeDto.getCode());
            if (isExists)
                return ResponseBuilder.fail("编码已存在！");

            ExceptionType type = BeanHelper.convert(exceptionTypeDto, ExceptionType.class);
            type.setId(Snowflake.getInstance().next());

            Assert.isTrue(exceptionTypeMapper.insert(type) > 0, "创建失败！");
            return ResponseBuilder.success(type.getCode(), "创建成功！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<String> update(ExceptionTypeDto dto) {
        try {
            //系统预设里面是否存在
            for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                if (typeEnum.getCode().equals(dto.getCode())) {
                    return ResponseBuilder.fail("系统预设异常类型，不能修改！");
                }
            }

            ExceptionType type = BeanHelper.convert(dto, ExceptionType.class);

            Assert.isTrue(exceptionTypeMapper.update(type) > 0, "修改失败！");
            return ResponseBuilder.success(type.getCode(), "修改成功！");
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<ExceptionTypeDto> get(String id) {
        try {
            Assert.hasText(id);

            ExceptionTypeDto dto = BeanHelper.convert(exceptionTypeMapper.get(id), ExceptionTypeDto.class);
            return ResponseBuilder.success(dto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public Response<ExceptionTypeDto> getByCode(String code) {
        try {
            Assert.hasText(code);
            ExceptionType type = null;

            boolean flag = false;

            //从系统预设里面查找
            for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                if (typeEnum.getCode().equals(code)) {
                    flag = true;
                    type = ExceptionType.builder()
                            .code(typeEnum.getCode())
                            .name(typeEnum.getName())
                            .remark(typeEnum.getRemark())
                            .build();
                    break;
                }
            }

            //系统预设里面没有找到则从数据库里面查找
            if (!flag)
                type = exceptionTypeMapper.getByCode(code);

            Assert.notNull(type, "获取数据失败！");

            ExceptionTypeDto dto = BeanHelper.convert(type, ExceptionTypeDto.class);
            return ResponseBuilder.success(dto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @Override
    public ListResponse<ExceptionTypeDto> getAll() {
        try {
            List<ExceptionType> allExceptionTypes = new ArrayList<>();

            //来源于数据库
            List<ExceptionType> typesFromDataBase = exceptionTypeMapper.getAll();
            if (null != typesFromDataBase && 0 < typesFromDataBase.size())
                allExceptionTypes.addAll(typesFromDataBase);

            //来源于系统预设
            List<ExceptionType> typesFromEnum = new ArrayList<>();
            for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                ExceptionType type = ExceptionType.builder()
                        .code(typeEnum.getCode())
                        .name(typeEnum.getName())
                        .remark(typeEnum.getRemark())
                        .build();
                typesFromEnum.add(type);
            }
            allExceptionTypes.addAll(typesFromEnum);

            List<ExceptionTypeDto> dtos = allExceptionTypes.stream().map(m -> BeanHelper.convert(m, ExceptionTypeDto.class)).collect(Collectors.toList());

            return ResponseBuilder.list(dtos);
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @Override
    public ListResponse<ExceptionTypeDto> search(Map<String, Object> params) {
        try {
            List<ExceptionType> allMatchTypes = new ArrayList<>();

            //数据库中符合条件的数据
            List<ExceptionType> types = exceptionTypeMapper.search(params);
            if (types != null && 0 < types.size())
                allMatchTypes.addAll(types);

            //系统预设中符合条件的数据
            {
                String code = (String) params.get("code");
                String name = (String) params.get("name");

                List<ExceptionType> matchTypes = new ArrayList<>();

                for (ExceptionTypeEnum typeEnum : ExceptionTypeEnum.values()) {
                    boolean flag = true;
                    if (!StringUtils.isBlank(code))
                        flag = typeEnum.getCode().contains(code);

                    if (!StringUtils.isBlank(name))
                        flag = typeEnum.getName().contains(name);

                    if (flag) {
                        ExceptionType type = ExceptionType.builder()
                                .code(typeEnum.getCode())
                                .name(typeEnum.getName())
                                .remark(typeEnum.getRemark())
                                .build();
                        matchTypes.add(type);
                    }
                }

                allMatchTypes.addAll(matchTypes);
            }

            List<ExceptionTypeDto> dtos = allMatchTypes.stream().map(m -> BeanHelper.convert(m, ExceptionTypeDto.class)).collect(Collectors.toList());

            return ResponseBuilder.list(dtos);
        } catch (Exception e) {
            log.error("", e);
            return ResponseBuilder.listFail(e.getMessage());
        }
    }
}

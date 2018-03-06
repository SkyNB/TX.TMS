package com.lnet.tms.web.exception;

import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.exception.exceptionDto.ExceptionTypeDto;
import com.lnet.tms.contract.spi.ExceptionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/exceptionType")
public class ExceptionTypeController {
    @Autowired
    private ExceptionTypeService exceptionTypeService;

    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return "exception/type/index";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ListResponse<ExceptionTypeDto> search(@RequestBody KendoGridRequest params) {
        return exceptionTypeService.search(params.getParams());
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String create() {
        return "exception/type/create";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public Response create(@RequestBody ExceptionTypeDto dto) {
        return exceptionTypeService.create(dto);
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public String update() {
        return "/exception/type/update";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response update(@RequestBody ExceptionTypeDto dto) {
        return exceptionTypeService.update(dto);
    }

    @RequestMapping(value = "/getByCode/{code}", method = RequestMethod.GET)
    @ResponseBody
    public Response getByCode(@PathVariable String code) {
        return exceptionTypeService.getByCode(code);
    }
}

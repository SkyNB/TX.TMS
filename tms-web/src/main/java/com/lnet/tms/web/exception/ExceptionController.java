package com.lnet.tms.web.exception;

import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.SelectObject;
import com.lnet.framework.security.UserPrincipal;
import com.lnet.model.tms.exception.exceptionDto.ExceptionCloseDto;
import com.lnet.model.tms.exception.exceptionDto.ExceptionDto;
import com.lnet.model.tms.exception.exceptionDto.ExceptionTypeDto;
import com.lnet.tms.contract.spi.ExceptionService;
import com.lnet.tms.contract.spi.ExceptionTypeService;
import com.lnet.ums.contract.api.OrganizationService;
import com.lnet.model.ums.organization.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lnet.tms.web.util.SystemUtil.SYSTEM_CODE;

@RequestMapping("/exception")
@Controller
public class ExceptionController {
    @Autowired
    private ExceptionService exceptionService;
    // TODO: 2017/1/4
    @Autowired
    private UserPrincipal userPrincipal;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private ExceptionTypeService exceptionTypeService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
        map.addAttribute("statusList", ExceptionDto.Status.values());
        List<SelectObject> exceptionTypes = new ArrayList<>();
        Response<List<ExceptionTypeDto>> typeDtoResponse = exceptionTypeService.getAll();
        if (typeDtoResponse.isSuccess()) {
            exceptionTypes = typeDtoResponse.getBody().stream().map(m -> new SelectObject(m.getCode(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("types", exceptionTypes);
        return "exception/index";
    }

    @RequestMapping("/search")
    @ResponseBody
    public Object search(@RequestBody KendoGridRequest request) {
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        return exceptionService.pageList(request.getPage(),request.getPageSize(),request.getParams());
    }

    @RequestMapping("/detail")
    public String detail() {
        return "exception/detail";
    }

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Response get(@PathVariable String id) {
        return exceptionService.get(id);
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String create(ModelMap map) {
        List<SelectObject> branches = new ArrayList<>();
        List<SelectObject> exceptionTypes = new ArrayList<>();
        // TODO: 2017/1/4
        Response<List<Organization>> branchResponse = organizationService.getAllBranches(SYSTEM_CODE);
        if (branchResponse.isSuccess()) {
            branches = branchResponse.getBody().stream().map(ele -> new SelectObject(ele.getCode(), ele.getName())).collect(Collectors.toList());
        }

        Response<List<ExceptionTypeDto>> typeDtoResponse = exceptionTypeService.getAll();
        if (typeDtoResponse.isSuccess()) {
            exceptionTypes = typeDtoResponse.getBody().stream().map(m -> new SelectObject(m.getCode(), m.getName())).collect(Collectors.toList());
        }

        map.addAttribute("types", exceptionTypes);
        map.addAttribute("statusList", ExceptionDto.Status.values());
        map.addAttribute("classifications", ExceptionDto.Classification.values());
        map.addAttribute("branches", branches);
        return "exception/create";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public Response create(@RequestBody ExceptionDto dto) {
        return exceptionService.create(dto);
    }

    @RequestMapping(value = "/close", method = RequestMethod.GET)
    public String close() {
        return "exception/close";
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    @ResponseBody
    public Response close(@RequestBody ExceptionCloseDto closeDto) {
        return exceptionService.closeException(closeDto);
    }
}

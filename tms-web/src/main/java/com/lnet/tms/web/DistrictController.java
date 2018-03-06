package com.lnet.tms.web;

import com.lnet.model.base.District;
import com.lnet.base.contract.spi.DistrictService;
import com.lnet.framework.core.KendoGridRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "district")
public class DistrictController {
    @Resource
    DistrictService districtService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
        map.put("districtTypes", District.DistrictType.values());
        return "district/index";
    }

    @RequestMapping(value = "search", method = RequestMethod.POST)
    @ResponseBody
    public Object search(@RequestBody KendoGridRequest params) {
        return districtService.pageList(params.getPage(), params.getPageSize(), params.getParams());
    }

    @RequestMapping(value = "getChildren", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object getChildren(@RequestBody KendoGridRequest params) {
        String superCode = null;
        if (params.getData() != null && params.getData().get("id") != null) {
            superCode = params.getData().get("id").toString();
        }
        return districtService.getChildren(superCode, params.getParams());
    }

    @RequestMapping(value = "getHierarchical", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object getHierarchical() {
        return districtService.getHierarchicalToArea();
    }

    @RequestMapping(value = "getChildren/{code}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object getChildren(@PathVariable String code) {
        return districtService.getChildren(code);
    }
}

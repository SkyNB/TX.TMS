package com.lnet.tms.web.dispatch;

import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.core.SelectObject;
import com.lnet.framework.security.UserPrincipal;
import com.lnet.model.tms.dispatch.dispatchEntity.Dispatch;
import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeApply;
import com.lnet.model.tms.dispatch.dispatchEntity.DispatchItem;
import com.lnet.tms.contract.spi.dispatch.DispatchFeeApplyService;
import com.lnet.tms.contract.spi.dispatch.DispatchService;
import com.lnet.ums.contract.api.VehicleTypeService;
import com.lnet.model.ums.vehicle.vehicleDto.VehicleTypeDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dispatchFeeApply")
public class DispatchFeeApplyController {

    @Resource
    DispatchFeeApplyService feeApplyService;
    @Resource
    UserPrincipal userPrincipal;
    @Resource
    DispatchService dispatchService;
    @Resource
    VehicleTypeService vehicleTypeService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
        return "dispatchFeeApply/index";
    }

    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    @ResponseBody
    public Object pageList(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        return feeApplyService.pageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/notApprove", method = RequestMethod.GET)
    public String notApprove(ModelMap map) {
        return "dispatchFeeApply/notApprove";
    }

    @RequestMapping(value = "/notApprovePageList", method = RequestMethod.POST)
    @ResponseBody
    public Object notApprovePageList(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        filterMap.put("isApprove", false);
        return feeApplyService.pageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/approve", method = RequestMethod.GET)
    public String approve(ModelMap map) {
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> vehicleTypes = new ArrayList<>();
        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        map.addAttribute("vehicleTypes", vehicleTypes);
        return "dispatchFeeApply/approve";
    }

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public
    @ResponseBody
    Response get(@RequestBody String feeApplyId) {
        try {//// TODO: 2017/1/5  
            Response<Map<String, Object>> feeApplyResponse = feeApplyService.getMap(feeApplyId);
            Assert.isTrue(feeApplyResponse.isSuccess());
            Map<String, Object> map = feeApplyResponse.getBody();
            DispatchFeeApply feeApply = (DispatchFeeApply) map.get("dispatchFeeApply");
            Assert.notNull(feeApply);
            Response<Dispatch> dispatchResponse = dispatchService.getByNo(feeApply.getDispatchNumber());
            Assert.isTrue(dispatchResponse.isSuccess());
            Assert.notNull(dispatchResponse.getBody());
            Response<Map<String, Object>> dispatchGetResponse = dispatchService.getMap(dispatchResponse.getBody().getDispatchId());
            Assert.isTrue(dispatchGetResponse.isSuccess());
            Assert.notNull(dispatchGetResponse.getBody());
            map.putAll(dispatchGetResponse.getBody());
            return ResponseBuilder.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    public
    @ResponseBody
    Response approve(@RequestBody DispatchFeeApply dispatchFeeApply) {
        dispatchFeeApply.setApproveUserId(userPrincipal.getUserId());
        return feeApplyService.approve(dispatchFeeApply);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String detail(ModelMap map) {
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> vehicleTypes = new ArrayList<>();
        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        map.addAttribute("vehicleTypes", vehicleTypes);
        return "dispatchFeeApply/detail";
    }
}

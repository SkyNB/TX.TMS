package com.lnet.tms.web.pack;


import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.Response;
import com.lnet.model.tms.pack.packDto.ConfirmPackageDto;
import com.lnet.model.tms.pack.packDto.PackageDto;
import com.lnet.model.tms.pack.packEntity.OrderPackingInfo;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.tms.contract.spi.pack.PackageService;
import com.lnet.tms.web.util.SystemUtil;
import com.lnet.tms.web.util.UserPrincipalImpl;
import com.lnet.ums.contract.api.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import com.lnet.model.tms.pack.packEntity.Package;

@Controller
@RequestMapping("package")
public class PackageController {
    @Resource
    PackageService packageService;

    @Resource
    UserService userService;

    @Resource
    UserPrincipalImpl userPrincipal;

    @Resource
    LogisticsOrderService logisticsOrderService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
        return "package/index";
    }

    @RequestMapping(value = "records", method = RequestMethod.GET)
    public String records(ModelMap map) {
        return "package/records";
    }

    @RequestMapping(value = "mergePackage", method = RequestMethod.GET)
    public String mergePacking(ModelMap map) {
        map.put("packageSizes", Package.PackageSize.values());
        map.put("wrapMaterials", Package.wrapMaterial.values());
        map.put("users", userService.getAvailable(userPrincipal.getBinding(SystemUtil.SYSTEM)).getBody());
        return "package/mergePackage";
    }

    @RequestMapping(value = "details", method = RequestMethod.GET)
    public String details(ModelMap map) {
        map.put("users", userService.getAvailable("TMS").getBody());
        return "package/details";
    }

    @RequestMapping(value = "getByPackageId/{packageId}", method = RequestMethod.POST)
    @ResponseBody
    public Response<PackageDto> getByPackageId(@PathVariable String packageId) {
        return packageService.getPackageById(packageId);
    }


    @RequestMapping(value = "pagePackage", method = RequestMethod.POST)
    @ResponseBody
    public Object pagePackage(@RequestBody KendoGridRequest request) {
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        return packageService.pagePackageRecord(request.getPage(), request.getPageSize(), request.getParams());
    }

    @RequestMapping(value = "pageOrderPacking", method = RequestMethod.POST)
    @ResponseBody
    public Object pageOrderPacking(@RequestBody KendoGridRequest request) {
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        return packageService.pageOrderPacking(request.getPage(),request.getPageSize(),request.getParams());
    }

    @RequestMapping(value = "mergePackage", method = RequestMethod.POST)
    public
    @ResponseBody
    Object mergePacking(@RequestBody PackageDto packageDto) {
        packageDto.setBranchCode(userPrincipal.getCurrentBranchCode());
        packageDto.setSiteCode(userPrincipal.getCurrentSiteCode());
        return packageService.mergePackage(packageDto);
    }


    @RequestMapping(value = "batchConfirm", method = RequestMethod.GET)
    public String batchConfirm(ModelMap map) {

        return "package/batchConfirm";
    }

    @RequestMapping(value = "findOrderSummary", method = RequestMethod.POST)
    public
    @ResponseBody
    List<ConfirmPackageDto> findOrderSummary(@RequestBody List<String> orderNos) {
        return packageService.findOrderSummary(orderNos).getBody();
    }

    @RequestMapping(value = "batchConfirm", method = RequestMethod.POST)
    public
    @ResponseBody
    Object batchConfirm(@RequestBody List<OrderPackingInfo> packingInfos) {
        return packageService.batchConfirm(packingInfos);
    }

    @RequestMapping(value = "judgeOrdersIsHavePacked", method = RequestMethod.POST)
    public
    @ResponseBody
    Object judgeOrdersIsHavePacked(@RequestBody List<String> orderNos) {
        return packageService.judgeOrdersIsHavePacked(orderNos);
    }

    @RequestMapping(value = "searchForPackage", method = RequestMethod.POST)
    @ResponseBody
    public Object searchForPackage(@RequestBody KendoGridRequest request) {
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        return logisticsOrderService.searchForPackage(request);
    }
}

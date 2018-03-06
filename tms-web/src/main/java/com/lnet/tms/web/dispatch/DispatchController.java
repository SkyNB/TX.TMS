package com.lnet.tms.web.dispatch;


import com.lnet.framework.core.*;
import com.lnet.framework.security.UserPrincipal;
import com.lnet.model.oms.order.orderDto.DispatchQueryDto;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchFeeDetailDto;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchPayableDto;
import com.lnet.model.tms.dispatch.dispatchEntity.Dispatch;
import com.lnet.model.tms.dispatch.dispatchEntity.DispatchFeeDetail;
import com.lnet.model.tms.dispatch.dispatchEntity.DispatchItem;
import com.lnet.model.tms.dispatch.dispatchEntity.DispatchOperateModel;
import com.lnet.model.ums.site.Site;
import com.lnet.model.ums.vehicle.vehicleDto.VehicleTypeDto;
import com.lnet.oms.contract.api.CollectingInstructionService;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.tms.contract.spi.dispatch.DispatchService;
import com.lnet.tms.contract.spi.dispatch.GpsInfoService;
import com.lnet.ums.contract.api.*;
import com.lnet.model.ums.carrier.carrierDto.CarrierListDto;
import com.lnet.model.ums.customer.customerEntity.Customer;
import com.lnet.model.ums.expense.ExpenseAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dispatch")
public class DispatchController {

    @Resource
    DispatchService dispatchService;
    @Resource
    VehicleTypeService vehicleTypeService;
    @Resource
    LogisticsOrderService logisticsOrderService;
    @Resource
    CustomerService customerService;
    @Resource
    CollectingInstructionService collectingInstructionService;
    @Resource
    UserService userService;
    @Resource
    UserPrincipal userPrincipal;
    @Resource
    SiteService siteService;
    @Resource
    GpsInfoService gpsInfoService;
    @Resource
    ExpenseAccountService expenseAccountService;
    @Autowired
    CarrierService carrierService;

    private static final String DISPATCH_PAYABLE_PARENT_CODE = "120200";


    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
        map.addAttribute("statusEnum", Dispatch.statusEnum.values());
        return "dispatch/index";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public Object pageList(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        return dispatchService.pageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String create(ModelMap map) {
        String siteCode = userPrincipal.getCurrentSiteCode();
        Response<Site> siteResponse = siteService.getByCode(siteCode);
        if (siteResponse.isSuccess()) {
            Site site = siteResponse.getBody();
            map.put("startAddress", site.getName());
        }
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> vehicleTypes = new ArrayList<>();
        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        List<SelectObject> carrierDtos = new ArrayList<>();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        if (carrierResponse.isSuccess())
            carrierDtos = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        map.addAttribute("carriers", carrierDtos);
        map.addAttribute("vehicleTypes", vehicleTypes);
        map.put("users", userService.findBySiteCode(siteCode).getBody());
        //派车单费用科目
        ListResponse<ExpenseAccount> expenseAccountListResponse = expenseAccountService.findChildren(DISPATCH_PAYABLE_PARENT_CODE);
        Assert.isTrue(expenseAccountListResponse.isSuccess());
        List<ExpenseAccount> expenseAccounts = expenseAccountListResponse.getBody();
        if (expenseAccounts != null && expenseAccounts.size() > 0) {
            List<DispatchFeeDetailDto> feeDetailDtos = new ArrayList<>();
            expenseAccounts.stream().forEach(expenseAccount -> {
                DispatchFeeDetailDto feeDetailDto = new DispatchFeeDetailDto();
                feeDetailDto.setFeeAccountName(expenseAccount.getName());
                feeDetailDto.setFeeAccountCode(expenseAccount.getCode());
                feeDetailDto.setAmount(new BigDecimal(0));
                feeDetailDtos.add(feeDetailDto);
            });
            map.put("feeDetailDtos", feeDetailDtos);
        }
        return "dispatch/create";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public Response create(@RequestBody Dispatch dispatch) {
        dispatch.setBranchCode(userPrincipal.getCurrentBranchCode());
        dispatch.setSiteCode(userPrincipal.getCurrentSiteCode());
        dispatch.setCreatedBy(userPrincipal.getUserId());
        return dispatchService.create(dispatch);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String detail(ModelMap map) {
        String siteCode = userPrincipal.getCurrentSiteCode();
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> vehicleTypes = new ArrayList<>();
        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        map.addAttribute("vehicleTypes", vehicleTypes);
        List<SelectObject> carrierDtos = new ArrayList<>();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        if (carrierResponse.isSuccess())
            carrierDtos = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        map.addAttribute("carriers", carrierDtos);
        map.put("users", userService.findBySiteCode(siteCode).getBody());
        return "dispatch/detail";
    }

    @RequestMapping(value = "/removeOrder", method = RequestMethod.GET)
    public String removeOrder(ModelMap map) {
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> vehicleTypes = new ArrayList<>();
        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        map.addAttribute("vehicleTypes", vehicleTypes);
        List<SelectObject> carrierDtos = new ArrayList<>();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        if (carrierResponse.isSuccess())
            carrierDtos = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        map.addAttribute("carriers", carrierDtos);
        return "dispatch/removeOrder";
    }

    @RequestMapping(value = "get/{dispatchId}", method = RequestMethod.POST)
    public
    @ResponseBody
    Response<Map<String, Object>> get(@PathVariable String dispatchId) {
        try {
            return dispatchService.getMap(dispatchId);
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    /*
    @RequestMapping(value = "/findByOrderNos", method = RequestMethod.POST)
    public
    @ResponseBody
    List<DispatchItemDto> findByOrderNos(@RequestBody List<String> orderNos) {
        return dispatchService.findItemsByOrderNos(userPrincipal.getCurrentSiteCode(),orderNos).getBody();
    }
    */

    @RequestMapping(value = "/getPrintTemplate", method = RequestMethod.POST)
    public String getPrintTemplate(@RequestBody List<String> dispatchIds, ModelMap modelMap) {
        modelMap.putAll(dispatchService.getMap(dispatchIds.get(0)).getBody());
        return "dispatch/printTable";
    }

    @RequestMapping(value = "/dispatchQuery", method = RequestMethod.GET)
    public String dispatchQuery(ModelMap map) {
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        map.addAttribute("orderTypes", DispatchQueryDto.OrderTypeEnum.values());
        return "dispatch/dispatchQuery";
    }

    @RequestMapping(value = "/dispatchQuerySearch", method = RequestMethod.POST)
    @ResponseBody
    public Object dispatchQuerySearch(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        filterMap.put("orderBy", "CREATED_DATE desc");
        return dispatchService.dispatchQueryPageList(request.getPage(), request.getPageSize(), filterMap);
    }


    @RequestMapping(value = "/dispatchPay", method = RequestMethod.GET)
    public String searchDispatchPay(ModelMap map) {
        map.addAttribute("statusEnum", Dispatch.statusEnum.FINISHED);
        return "dispatch/dispatchPay";
    }

    @RequestMapping(value = "/searchDispatchPay", method = RequestMethod.POST)
    @ResponseBody
    public PageResponse<DispatchPayableDto> searchDispatchPay(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        return dispatchService.searchDispatchPay(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/dispatchItemPay", method = RequestMethod.GET)
    public String dispatchItemPay(ModelMap map) {
        map.addAttribute("statusEnum", Dispatch.statusEnum.FINISHED);
        return "dispatch/dispatchItemPay";
    }

    @RequestMapping(value = "/searchDispatchItemPay", method = RequestMethod.POST)
    @ResponseBody
    public PageResponse searchDispatchItemPay(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        return dispatchService.searchDispatchItemPay(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/addOrder", method = RequestMethod.GET)
    public String addOrder(ModelMap map) {
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> vehicleTypes = new ArrayList<>();

        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        map.addAttribute("vehicleTypes", vehicleTypes);

        List<SelectObject> carrierDtos = new ArrayList<>();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        if (carrierResponse.isSuccess())
            carrierDtos = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        map.addAttribute("carriers", carrierDtos);
        return "dispatch/addOrder";
    }

    @RequestMapping(value = "/addOrder", method = RequestMethod.POST)
    @ResponseBody
    public Response addOrder(@RequestBody DispatchOperateModel model) {
        model.setOperatorId(userPrincipal.getUserId());
        model.setSiteCode(userPrincipal.getCurrentSiteCode());
        return dispatchService.addOrders(model);
    }

    @RequestMapping(value = "/removeOrders", method = RequestMethod.POST)
    @ResponseBody
    public Response removeOrders(@RequestBody final Map map) {
        String dispatchId = (String) map.get("dispatchId");
        List<String> orderNos = (List<String>) map.get("orderNos");
        return dispatchService.removeOrders(dispatchId, orderNos, userPrincipal.getUserId());
    }

    @RequestMapping(value = "/assign", method = RequestMethod.GET)
    public String assign(ModelMap map) {
        List<SelectObject> vehicleTypes = new ArrayList<>();
        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        map.addAttribute("vehicleTypes", vehicleTypes);
        return "dispatch/assign";
    }

    @RequestMapping(value = "/assign", method = RequestMethod.POST)
    @ResponseBody
    public Response assign(@RequestBody Map map) {
        String dispatchId = (String) map.get("dispatchId");
        Response<Map<String, Object>> dispatchResponse = dispatchService.getMap(dispatchId);
        Assert.isTrue(dispatchResponse.isSuccess());
        Dispatch dispatch = (Dispatch) dispatchResponse.getBody().get("dispatch");
        dispatch.setVehicleNumber((String) map.get("vehicleNumber"));
        dispatch.setDriver((String) map.get("driver"));
        dispatch.setDriverPhone((String) map.get("driverPhone"));
        dispatch.setVehicleTypeId((String) map.get("vehicleTypeId"));
        dispatch.setVehicleId(StringUtils.isEmpty(map.get("vehicleId")) ? null : (String) map.get("vehicleId"));
        return dispatchService.assign(dispatch);
    }

    @RequestMapping(value = "/loading", method = RequestMethod.GET)
    public String loading(ModelMap map) {
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> carrierDtos = new ArrayList<>();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        if (carrierResponse.isSuccess())
            carrierDtos = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        map.addAttribute("carriers", carrierDtos);
        return "dispatch/loading";
    }

    @RequestMapping(value = "/loading", method = RequestMethod.POST)
    @ResponseBody
    public Response loading(@RequestBody Map map) {
        String dispatchId = (String) map.get("dispatchId");
        List<String> orderNos = (List<String>) map.get("orderNos");
        return dispatchService.loadOrders(dispatchId, orderNos, userPrincipal.getUserId());
    }

    @RequestMapping(value = "/finishLoading/{dispatchId}", method = RequestMethod.POST)
    @ResponseBody
    public Response finishLoading(@PathVariable String dispatchId) {
        return dispatchService.finishLoading(dispatchId, userPrincipal.getUserId());
    }

    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public String arrive() {
        return "dispatch/start";
    }

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ResponseBody
    public Response start(@RequestBody Map map) {
        String dispatchId = (String) map.get("dispatchId");
        LocalDateTime startTime = LocalDateTime.parse(map.get("startTime").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return dispatchService.start(dispatchId, startTime, userPrincipal.getUserId());
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.GET)
    public String cancel() {
        return "dispatch/cancel";
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public Response cancel(@RequestBody Map map) {
        String dispatchId = (String) map.get("dispatchId");
        String notes = (String) map.get("notes");
        return dispatchService.cancel(dispatchId, notes, userPrincipal.getUserId());
    }

    @RequestMapping(value = "/finish", method = RequestMethod.GET)
    public String finish(ModelMap map) {
        map.put("orderDispatchTypes", DispatchItem.orderDispatchTypeEnum.values());
        map.put("orderTypes", DispatchItem.orderTypeEnum.values());
        List<SelectObject> carrierDtos = new ArrayList<>();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        if (carrierResponse.isSuccess())
            carrierDtos = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        map.addAttribute("carriers", carrierDtos);
        return "dispatch/finish";
    }

    @RequestMapping(value = "/finish", method = RequestMethod.POST)
    @ResponseBody
    public Response finish(@RequestBody Map map) {
        String dispatchId = (String) map.get("dispatchId");
        List<String> finishOrderNos = (List<String>) map.get("finishOrderNos");
        List<String> notFinishOrderNos = (List<String>) map.get("notFinishOrderNos");
        return dispatchService.finish(dispatchId, finishOrderNos, notFinishOrderNos, userPrincipal.getUserId());
    }

    @RequestMapping(value = "/updateFee", method = RequestMethod.GET)
    public String updateFee(ModelMap map) {
        List<SelectObject> vehicleTypes = new ArrayList<>();
        Response<List<VehicleTypeDto>> response = vehicleTypeService.findAll();
        if (response.isSuccess())
            vehicleTypes = response.getBody().stream().map(ele -> new SelectObject(ele.getVehicleTypeId(), ele.getName())).collect(Collectors.toList());
        map.addAttribute("vehicleTypes", vehicleTypes);
        return "dispatch/updateFee";
    }

    @RequestMapping(value = "/updateFee", method = RequestMethod.POST)
    public
    @ResponseBody
    Response updateFee(@RequestBody List<DispatchFeeDetail> feeDetails) {
        return dispatchService.updateFee(feeDetails.get(0).getDispatchId(), feeDetails, userPrincipal.getUserId());
    }

    @RequestMapping(value = "/gps", method = RequestMethod.GET)
    public String gps(ModelMap map) {
        return "dispatch/gps";
    }

    /*
    @RequestMapping(value = "/getGpsInfos", method = RequestMethod.GET)
    public
    @ResponseBody
    Response getGpsInfos() {
        return gpsInfoService.findBySite(userPrincipal.getCurrentBranchCode(),
                userPrincipal.getCurrentSiteCode());
    }
    */

    @RequestMapping(value = "/getOrderCount", method = RequestMethod.GET)
    public
    @ResponseBody
    Response getOrderCount() {
        return dispatchService.getOrderCount(userPrincipal.getCurrentBranchCode(),
                userPrincipal.getCurrentSiteCode());
    }

}

package com.lnet.tms.web.consign;


import com.lnet.framework.core.*;
import com.lnet.model.tms.consign.consignDto.ConsignOrderItemDto;
import com.lnet.model.tms.consign.consignEntity.BatchConsignModel;
import com.lnet.model.ums.vehicle.vehicleDto.VehicleListDto;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import com.lnet.tms.contract.spi.consgin.ConsignOrderService;
import com.lnet.tms.web.util.SystemUtil;
import com.lnet.tms.web.util.UserPrincipalImpl;
import com.lnet.ums.contract.api.CarrierService;
import com.lnet.ums.contract.api.CustomerService;
import com.lnet.ums.contract.api.OrganizationService;
import com.lnet.ums.contract.api.VehicleService;
import com.lnet.model.ums.carrier.carrierDto.CarrierDto;
import com.lnet.model.ums.carrier.carrierDto.CarrierListDto;
import com.lnet.model.ums.customer.customerEntity.Customer;
import com.lnet.model.ums.customer.customerEntity.Project;
import com.lnet.model.ums.organization.Organization;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/consign")
public class ConsignController {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    ConsignOrderService consignService;
    @Resource
    UserPrincipalImpl userPrincipal;
    @Resource
    CarrierService carrierService;
    @Resource
    CustomerService customerService;
    @Resource
    OrganizationService organizationService;
    @Resource
    VehicleService vehicleService;


    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        map.addAttribute("statusList", ConsignOrder.consignStatus.values());
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        return "consign/index";
    }
    @RequestMapping(value = "payable",method = RequestMethod.GET)
    public String payable(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        map.addAttribute("statusList", ConsignOrder.consignStatus.values());
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        return "consign/payable";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public Object pageList(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        Map<String, String> map = userPrincipal.getBindings();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        return consignService.pageList(request.getPage(), request.getPageSize(), filterMap);
    }
    @RequestMapping(value = "/searchPayable", method = RequestMethod.POST)
    @ResponseBody
    public Object searchPayable(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        Map<String, String> map = userPrincipal.getBindings();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        return consignService.searchConsignPayable(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/selectOrder", method = RequestMethod.GET)
    public String selectOrder(ModelMap map) {
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        return "consign/select";
    }

    @RequestMapping(value = "/selectOrder", method = RequestMethod.POST)
    @ResponseBody
    public Object selectOrder(@RequestBody KendoGridRequest request) {
        Map<String, String> map = userPrincipal.getBindings();
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("branchCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("siteCode", userPrincipal.getCurrentSiteCode());
        filterMap.put("orderBy", "CREATED_DATE desc");
        return consignService.selectOrder(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String create(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> selectObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            selectObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }

        List<SelectObject> organizations = new ArrayList<>();
        Response<List<Organization>> listResponse = organizationService.getAllBranches(SystemUtil.SYSTEM_CODE);
        if (listResponse.isSuccess()) {
            organizations = listResponse.getBody().stream().map(ele -> new SelectObject(ele.getCode(), ele.getName())).collect(Collectors.toList());
        }
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        map.addAttribute("carriers", selectObject);
        map.addAttribute("settleCycles", CarrierDto.SettleCycle.values());
        map.addAttribute("paymentTypes", CarrierDto.PaymentType.values());
        map.addAttribute("calculateTypes", CarrierDto.CalculateType.values());
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        map.addAttribute("handoverTypes", Project.HandoverTypeEnum.values());
        map.addAttribute("organizations", organizations);
        return "consign/create";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public Response create(@RequestBody ConsignOrder consignOrder) {
        try {
            Assert.notNull(consignOrder);
            consignOrder.setBranchCode(userPrincipal.getCurrentBranchCode());
            String siteCode = userPrincipal.getCurrentSiteCode();
            consignOrder.setSiteCode(siteCode);
            consignOrder.setCreatedBy(userPrincipal.getUserId());
            return consignService.create(consignOrder);
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String detail(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        map.addAttribute("settleCycles", CarrierDto.SettleCycle.values());
        map.addAttribute("paymentTypes", CarrierDto.PaymentType.values());
        map.addAttribute("calculateTypes", CarrierDto.CalculateType.values());
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        map.addAttribute("handoverTypes", Project.HandoverTypeEnum.values());

        List<SelectObject> organizations = new ArrayList<>();
        Response<List<Organization>> listResponse = organizationService.getAllBranches(SystemUtil.SYSTEM_CODE);
        if (listResponse.isSuccess()) {
            organizations = listResponse.getBody().stream().map(ele -> new SelectObject(ele.getCode(), ele.getName())).collect(Collectors.toList());
        }
        map.addAttribute("organizations", organizations);
        return "consign/detail";
    }

    @RequestMapping(value = "get/{consignOrderId}", method = RequestMethod.POST)
    public
    @ResponseBody
    Response<Map<String, Object>> get(@PathVariable String consignOrderId) {
        try {
            return consignService.get(consignOrderId);
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/consign", method = RequestMethod.GET)
    public String consign(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        map.addAttribute("settleCycles", CarrierDto.SettleCycle.values());
        map.addAttribute("paymentTypes", CarrierDto.PaymentType.values());
        map.addAttribute("calculateTypes", CarrierDto.CalculateType.values());
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        map.addAttribute("handoverTypes", Project.HandoverTypeEnum.values());
        List<SelectObject> organizations = new ArrayList<>();
        Response<List<Organization>> listResponse = organizationService.getAllBranches(SystemUtil.SYSTEM_CODE);
        if (listResponse.isSuccess()) {
            organizations = listResponse.getBody().stream().map(ele -> new SelectObject(ele.getCode(), ele.getName())).collect(Collectors.toList());
        }
        map.addAttribute("organizations", organizations);
        return "consign/consign";
    }

    @RequestMapping(value = "/consign", method = RequestMethod.POST)
    @ResponseBody
    public Response consign(@RequestBody ConsignOrder consignOrder) {
        try {
            if (consignOrder.getConsignOrderId() == null) {
                consignOrder.setBranchCode(userPrincipal.getCurrentBranchCode());
                consignOrder.setSiteCode(userPrincipal.getCurrentSiteCode());
                consignOrder.setCreatedBy(userPrincipal.getUserId());
            }
            return consignService.consign(consignOrder, userPrincipal.getUserId());
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public String update(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        map.addAttribute("settleCycles", CarrierDto.SettleCycle.values());
        map.addAttribute("paymentTypes", CarrierDto.PaymentType.values());
        map.addAttribute("calculateTypes", CarrierDto.CalculateType.values());
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        map.addAttribute("handoverTypes", Project.HandoverTypeEnum.values());
        List<SelectObject> organizations = new ArrayList<>();
        Response<List<Organization>> listResponse = organizationService.getAllBranches(SystemUtil.SYSTEM_CODE);
        if (listResponse.isSuccess()) {
            organizations = listResponse.getBody().stream().map(ele -> new SelectObject(ele.getCode(), ele.getName())).collect(Collectors.toList());
        }
        map.addAttribute("organizations", organizations);
        return "consign/update";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response update(@RequestBody ConsignOrder consignOrder) {
        try {
            Assert.notNull(consignOrder);
            return consignService.update(consignOrder, userPrincipal.getUserId());
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/startUp", method = RequestMethod.GET)
    public String startUp() {
        return "consign/startUp";
    }

    @RequestMapping(value = "/startUp", method = RequestMethod.POST)
    @ResponseBody
    public Response startUp(@RequestBody Map map) {
        try {
            List<String> consignOrderIds = (List<String>) map.get("consignOrderIds");
            LocalDateTime startUpTime = LocalDateTime.parse(map.get("startUpTime").toString(), formatter);
            return consignService.startUp(consignOrderIds, startUpTime, userPrincipal.getUserId(), userPrincipal.getCurrentSiteCode(), userPrincipal.getName());
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/arrive", method = RequestMethod.GET)
    public String arrive() {
        return "consign/arrive";
    }

    @RequestMapping(value = "/arrive", method = RequestMethod.POST)
    @ResponseBody
    public Response arrive(@RequestBody Map map) {
        try {
            List<String> consignOrderIds = (List<String>) map.get("consignOrderIds");
            LocalDateTime arriveTime = LocalDateTime.parse(map.get("arriveTime").toString(), formatter);
            return consignService.arrive(consignOrderIds, arriveTime, userPrincipal.getUserId(), userPrincipal.getCurrentSiteCode());
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/finish", method = RequestMethod.GET)
    public String finish() {
        return "consign/finish";
    }

    @RequestMapping(value = "/finish", method = RequestMethod.POST)
    @ResponseBody
    public Response finish(@RequestBody Map map) {
        try {
            List<String> consignOrderIds = (List<String>) map.get("consignOrderIds");
            LocalDateTime finishTime = LocalDateTime.parse(map.get("finishTime").toString(), formatter);
            return consignService.finish(consignOrderIds, finishTime, userPrincipal.getUserId());
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.GET)
    public String cancel() {
        return "consign/cancel";
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public Response cancel(@RequestBody Map map) {
        try {
            String consignOrderId = (String) map.get("consignOrderId");
            String notes = (String) map.get("notes");
            return consignService.cancel(consignOrderId, userPrincipal.getUserId(), notes, userPrincipal.getCurrentSiteCode());
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/updateOrderNo", method = RequestMethod.GET)
    public String updateOrderNo() {
        return "consign/updateOrderNo";
    }

    @RequestMapping(value = "/updateOrderNo", method = RequestMethod.POST)
    @ResponseBody
    public Response updateOrderNo(@RequestBody Map map) {
        try {
            String consignOrderId = (String) map.get("consignOrderId");
            String consignOrderNo = (String) map.get("consignOrderNo");
            return consignService.updateOrderNo(consignOrderId, consignOrderNo, userPrincipal.getUserId());
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/findItemDtoListByOrderNos", method = RequestMethod.POST)
    @ResponseBody
    public ListResponse<ConsignOrderItemDto> findItemDtoListByOrderNos(@RequestBody List<String> orderNos) {
        try {
            return consignService.findItemDtoListByOrderNos(orderNos, userPrincipal.getCurrentSiteCode());
        } catch (Exception e) {
            return ResponseBuilder.listFail(e.getMessage());
        }
    }

    @RequestMapping(value = "/getByConsignOrderNo/{carrierCode}/{consignOrderNo}", method = RequestMethod.GET)
    @ResponseBody
    public Response getByConsignOrderNo(@PathVariable String carrierCode, @PathVariable String consignOrderNo) {
        return consignService.getByNo(carrierCode, consignOrderNo);
    }

    @RequestMapping(value = "/findItemByIds", method = RequestMethod.POST)
    @ResponseBody
    public ListResponse findItemByIds(@RequestBody String[] consignOrderIds) {
        return consignService.findItemByIds(Arrays.asList(consignOrderIds));
    }

    @RequestMapping(value = "/batchConsign", method = RequestMethod.GET)
    public String batchConsign(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> selectObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            selectObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", selectObject);
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        return "consign/batchConsign";
    }

    @RequestMapping(value = "/batchConsign", method = RequestMethod.POST)
    @ResponseBody
    public Response batchConsign(@RequestBody BatchConsignModel model) {
        model.setBranchCode(userPrincipal.getCurrentBranchCode());
        model.setSiteCode(userPrincipal.getCurrentSiteCode());
        model.setOperatorId(userPrincipal.getUserId());
        return consignService.batchConsign(model, false);
    }

    @RequestMapping(value = "/mergeConsign", method = RequestMethod.POST)
    @ResponseBody
    public Response mergeConsign(@RequestBody BatchConsignModel model) {
        model.setBranchCode(userPrincipal.getCurrentBranchCode());
        model.setSiteCode(userPrincipal.getCurrentSiteCode());
        model.setOperatorId(userPrincipal.getUserId());
        return consignService.batchConsign(model, true);
    }

    @RequestMapping(value = "/createByDriver", method = RequestMethod.GET)
    public String createByDriver(ModelMap map) {
        Response<List<VehicleListDto>> resp = vehicleService.getSiteAvailable(userPrincipal.getCurrentBranchCode(), userPrincipal.getCurrentSiteCode());
        Assert.isTrue(resp.isSuccess());
        List<SelectObject> vehicleSelectList = resp.getBody().stream()
                .map(m -> new SelectObject(m.getVehicleId(), m.getVehicleId(), m.getDriver()))
                .collect(Collectors.toList());
        map.put("vehicles", vehicleSelectList);
        return "consign/createByDriver";
    }

    @RequestMapping(value = "/createByDriverQuery", method = RequestMethod.POST)
    @ResponseBody
    public Object createByDriverQuery(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        String vehicleId = (String) filterMap.get("vehicleId");
        return consignService.createByDriverQuery(vehicleId,userPrincipal.getCurrentBranchCode() ,userPrincipal.getCurrentSiteCode());
    }
}

package com.lnet.tms.web.transfer;


import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.core.SelectObject;
import com.lnet.framework.security.UserPrincipal;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticOptions;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.model.tms.order.orderEntity.OrderTransfer;
import com.lnet.model.tms.order.orderDto.OrderTransferListDto;
import com.lnet.model.tms.transfer.TransferReceiptDto;
import com.lnet.tms.contract.spi.TransferService;
import com.lnet.ums.contract.api.CustomerService;
import com.lnet.model.ums.customer.customerEntity.Customer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transfer")
public class TransferController {

    @Resource
    UserPrincipal userPrincipal;

    @Resource
    TransferService transferService;

    @Resource
    CustomerService customerService;

    @RequestMapping(value = "/consignIndex", method = RequestMethod.GET)
    public String consignIndex(ModelMap map) {
        map.addAttribute("statusEnum", OrderTransfer.statusEnum.values());
        map.addAttribute("dispatchTypeEnum", LogisticsOrder.dispatchTypeEnum.values());
        map.addAttribute("transportTypeEnum", LogisticOptions.TransportType.values());
        map.addAttribute("handoverTypeEnum", LogisticOptions.HandoverType.values());
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        return "transfer/consignIndex";
    }

    @RequestMapping(value = "/consignSearch", method = RequestMethod.POST)
    @ResponseBody
    public Object consignSearch(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("orderOrgCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("orderSiteCode", userPrincipal.getCurrentSiteCode());
        return transferService.pageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/arriveIndex", method = RequestMethod.GET)
    public String arriveIndex(ModelMap map) {
        map.addAttribute("statusEnum", OrderTransfer.statusEnum.values());
        map.addAttribute("dispatchTypeEnum", LogisticsOrder.dispatchTypeEnum.values());
        map.addAttribute("transportTypeEnum", LogisticOptions.TransportType.values());
        map.addAttribute("handoverTypeEnum", LogisticOptions.HandoverType.values());
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        return "transfer/arriveIndex";
    }

    @RequestMapping(value = "/arriveSearch", method = RequestMethod.POST)
    @ResponseBody
    public Object arriveSearch(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("transferOrganizationCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("transferSiteCode", userPrincipal.getCurrentSiteCode());
        return transferService.pageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/arrive", method = RequestMethod.GET)
    public String arrive(ModelMap map) {
        return "transfer/arrive";
    }

    @RequestMapping(value = "/arrive", method = RequestMethod.POST)
    @ResponseBody
    public Response arrive(@RequestBody Map map) {
        try {
            LocalDateTime arriveTime = LocalDateTime.parse(map.get("arriveTime").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<String> orderNos = (List<String>) map.get("orderNos");
            List<String> arriveRemarkList = (List<String>) map.get("arriveRemarkList");
            return transferService.batchArrive(userPrincipal.getCurrentSiteCode(), orderNos, arriveRemarkList, arriveTime);
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/enterReceiptInfo", method = RequestMethod.GET)
    public String enterReceiptInfo(ModelMap map) {
        return "transfer/enterReceiptInfo";
    }

    @RequestMapping(value = "/enterReceiptInfo", method = RequestMethod.POST)
    @ResponseBody
    public Response enterReceiptInfo(@RequestBody Map map) {
        try {
            List<String> orderNos = (List<String>) map.get("orderNos");
            String receiptInfo = (String) map.get("receiptInfo");
            LocalDateTime receiptPostTime = LocalDateTime.parse(map.get("receiptPostTime").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            TransferReceiptDto transferReceiptDto = TransferReceiptDto.builder()
                    .orderNos(orderNos)
                    .siteCode(userPrincipal.getCurrentSiteCode())
                    .receiptInfo(receiptInfo)
                    .receiptPostTime(receiptPostTime)
                    .build();
            return transferService.saveReceiptInfo(transferReceiptDto);
        } catch (Exception e) {
            return ResponseBuilder.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/findTransferByOrderNo", method = RequestMethod.POST)
    public
    @ResponseBody
    Response<OrderTransferListDto> findTransferByOrderNo(@RequestBody String orderNo) {
        try {
            return transferService.findTransferByOrderNo(orderNo, userPrincipal.getCurrentSiteCode());
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "/reportConsignInCity", method = RequestMethod.GET)
    public String reportConsignInCity(ModelMap map) {
        map.addAttribute("dispatchTypeEnum", LogisticsOrder.dispatchTypeEnum.values());
        map.addAttribute("transportTypeEnum", LogisticOptions.TransportType.values());
        map.addAttribute("handoverTypeEnum", LogisticOptions.HandoverType.values());
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        return "report/transferConsignInCity";
    }

    @RequestMapping(value = "/consignInCitySearch", method = RequestMethod.POST)
    @ResponseBody
    public Object consignInCitySearch(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("orderOrgCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("orderSiteCode", userPrincipal.getCurrentSiteCode());
        filterMap.put("dispatchType", LogisticsOrder.dispatchTypeEnum.IN_CITY.name());
        return transferService.reportPageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/reportConsignOutCity", method = RequestMethod.GET)
    public String reportConsignOutCity(ModelMap map) {
        map.addAttribute("dispatchTypeEnum", LogisticsOrder.dispatchTypeEnum.values());
        map.addAttribute("transportTypeEnum", LogisticOptions.TransportType.values());
        map.addAttribute("handoverTypeEnum", LogisticOptions.HandoverType.values());
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        return "report/transferConsignOutCity";
    }

    @RequestMapping(value = "/consignOutCitySearch", method = RequestMethod.POST)
    @ResponseBody
    public Object consignOutCitySearch(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("orderOrgCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("orderSiteCode", userPrincipal.getCurrentSiteCode());
        filterMap.put("dispatchType", LogisticsOrder.dispatchTypeEnum.OUT_CITY.name());
        return transferService.reportPageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/reportArriveInCity", method = RequestMethod.GET)
    public String reportArriveInCity(ModelMap map) {
        map.addAttribute("dispatchTypeEnum", LogisticsOrder.dispatchTypeEnum.values());
        map.addAttribute("transportTypeEnum", LogisticOptions.TransportType.values());
        map.addAttribute("handoverTypeEnum", LogisticOptions.HandoverType.values());
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        return "report/transferArriveInCity";
    }

    @RequestMapping(value = "/arriveInCitySearch", method = RequestMethod.POST)
    @ResponseBody
    public Object arriveInCitySearch(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("transferOrganizationCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("transferSiteCode", userPrincipal.getCurrentSiteCode());
        filterMap.put("dispatchType", LogisticsOrder.dispatchTypeEnum.IN_CITY.name());
        return transferService.reportPageList(request.getPage(), request.getPageSize(), filterMap);
    }

    @RequestMapping(value = "/reportArriveOutCity", method = RequestMethod.GET)
    public String reportArriveOutCity(ModelMap map) {
        map.addAttribute("dispatchTypeEnum", LogisticsOrder.dispatchTypeEnum.values());
        map.addAttribute("transportTypeEnum", LogisticOptions.TransportType.values());
        map.addAttribute("handoverTypeEnum", LogisticOptions.HandoverType.values());
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        List<SelectObject> cusObject = new ArrayList<>();
        if (customers.isSuccess()) {
            cusObject = customers.getBody().stream().map(m -> new SelectObject(m.getCustomerId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("customers", cusObject);
        return "report/transferArriveOutCity";
    }

    @RequestMapping(value = "/arriveOutCitySearch", method = RequestMethod.POST)
    @ResponseBody
    public Object arriveOutCitySearch(@RequestBody KendoGridRequest request) {
        Map<String, Object> filterMap = request.getParams();
        filterMap.put("transferOrganizationCode", userPrincipal.getCurrentBranchCode());
        filterMap.put("transferSiteCode", userPrincipal.getCurrentSiteCode());
        filterMap.put("dispatchType", LogisticsOrder.dispatchTypeEnum.OUT_CITY.name());
        return transferService.reportPageList(request.getPage(), request.getPageSize(), filterMap);
    }
}

package com.lnet.tms.web.progressTrace;


import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.SelectObject;
import com.lnet.framework.excel.reader.ExcelReader;
import com.lnet.framework.excel.util.ExcelFormat;
import com.lnet.framework.security.UserPrincipal;
import com.lnet.model.tms.order.orderEntity.OrderTraceModel;
import com.lnet.model.ums.carrier.carrierDto.CarrierDto;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.model.tms.delivery.DeliveryOrderTraceCreateModel;
import com.lnet.model.tms.order.orderEntity.OrderTrace;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import com.lnet.tms.contract.spi.ProgressTraceService;
import com.lnet.ums.contract.api.CarrierService;
import com.lnet.ums.contract.api.CustomerService;
//import com.lnet.model.ums.carrier.carrierDto.CarrierDto;
import com.lnet.model.ums.carrier.carrierDto.CarrierListDto;
import com.lnet.model.ums.customer.customerEntity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/progressTrace")
public class ProgressTraceController {
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private ProgressTraceService progressTraceService;
    @Autowired
    private CarrierService carrierService;
    // TODO: 2017/1/4
    @Autowired
    private UserPrincipal userPrincipal;
    @Autowired
    private CustomerService customerService;
    @Resource
    LogisticsOrderService logisticsOrderService;

    @RequestMapping(value = "/consignTrace", method = RequestMethod.GET)
    public String consignTraceIndex(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        map.addAttribute("transportTypes", CarrierDto.TransportType.values());
        map.addAttribute("statusList", ConsignOrder.consignStatus.values());
        return "progressTrace/consignIndex";
    }

    @RequestMapping(value = "/importConsignTrace", method = RequestMethod.GET)
    public String importConsignTrace() {
        return "progressTrace/importConsignTrace";
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(@RequestParam("fileName") String fileName, HttpServletResponse response) throws IOException {
        InputStream input = new FileInputStream(servletContext.getRealPath("/downloads/" + fileName));

        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        OutputStream output = response.getOutputStream();

        byte[] bytes = new byte[1024];
        int length = 0;

        while ((length = input.read(bytes)) > 0) {
            output.write(bytes, 0, length);
        }

        input.close();
        output.close();
    }

    @RequestMapping(value = "/importConsignTrace", method = RequestMethod.POST)
    @ResponseBody
    public Response importConsignTrace(@RequestParam("progressTraceFile") MultipartFile file) throws IOException {
        //读取excel的数据
        List<DeliveryOrderTraceCreateModel> createModels = ExcelReader.readByColumnName(file.getInputStream(), ExcelFormat.OFFICE2007, 0, 0, row -> {
            String deliveryNo = (String) row.getColumnValue("托运单号");
            String shipper = (String) row.getColumnValue("承运商名称");
            LocalDateTime operateTime = LocalDateTime.parse((String) row.getColumnValue("跟踪时间"), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            String operateAddress = (String) row.getColumnValue("跟踪地点");
            String description = (String) row.getColumnValue("跟踪内容");

            return DeliveryOrderTraceCreateModel.builder()
                    .deliveryNo(deliveryNo)
                    .shipper(shipper)
                    .operateTime(operateTime)
                    .operateAddress(operateAddress)
                    .description(description)
                    .operator(userPrincipal.getName())
                    .build();
        });
        return progressTraceService.batchAddDeliveryOrderTrace(createModels);
    }

    @RequestMapping(value = "/addConsignTrace", method = RequestMethod.GET)
    public String addConsignTrace() {
        return "progressTrace/addConsignTrace";
    }

    @RequestMapping(value = "/deliveryOrderProgressQuery/{orderNo}/{carrierCode}", method = RequestMethod.GET)
    @ResponseBody
    public Response deliveryOrderProgressQuery(@PathVariable("orderNo") String consignOrderNo, @PathVariable("carrierCode") String carrierCode) {
        return progressTraceService.deliveryOrderProgressQuery(consignOrderNo, carrierCode);
    }

    @RequestMapping(value = "/addConsignTrace", method = RequestMethod.POST)
    @ResponseBody
    public Response addConsignTrace(@RequestBody DeliveryOrderTraceCreateModel createModel) {
        createModel.setOperator(userPrincipal.getName());
        return progressTraceService.addDeliverOrderTrace(createModel);
    }

    @RequestMapping(value = "/orderTrace", method = RequestMethod.GET)
    public String orderTraceIndex(ModelMap map) {
        map.put("orderTypes", LogisticsOrder.OrderType.values());
        map.put("statuses", LogisticsOrder.Status.values());
        return "progressTrace/orderTraceIndex";
    }

    @RequestMapping(value = "/addOrderTrace", method = RequestMethod.GET)
    public String addOrderTrace() {
        return "progressTrace/addOrderTrace";
    }

    @RequestMapping(value = "/addOrderTrace", method = RequestMethod.POST)
    @ResponseBody
    public Response addOrderTrace(@RequestBody OrderTraceModel model) {
        model.setOperator(userPrincipal.getName());
        return progressTraceService.addOrderTrace(model);
    }

    @RequestMapping(value = "/orderTraceQuery/{orderNo}", method = RequestMethod.GET)
    @ResponseBody
    public Response<OrderTrace> orderTraceQuery(@PathVariable String orderNo) {
        return progressTraceService.orderTraceQuery(orderNo);
    }

    @RequestMapping(value = "/startup", method = RequestMethod.GET)
    public String startupIndex(ModelMap map) {
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        return "progressTrace/consignStartupIndex";
    }

    @RequestMapping(value = "/consignStartup", method = RequestMethod.GET)
    public String consignStartup() {
        return "progressTrace/consignStartup";
    }

    @RequestMapping(value = "/sign", method = RequestMethod.GET)
    public String signIndex(ModelMap map) {
        List<SelectObject> res = new ArrayList<>();
//        List<Customer> customers = customerService.getAvailable().getBody();
//        res = customers.stream().map(m -> new SelectObject(m.getCode(), m.getName())).collect(Collectors.toList());

        map.put("orderTypes", LogisticsOrder.OrderType.values());
        map.put("customers", res);
        return "progressTrace/signIndex";
    }

    @RequestMapping(value = "/orderSign", method = RequestMethod.GET)
    public String orderSign() {
        return "progressTrace/orderSign";
    }

    @RequestMapping(value = "/orderRefuse", method = RequestMethod.GET)
    public String orderRefuse() {
        return "progressTrace/orderRefuse";
    }

    @RequestMapping(value = "/orderCollectSign", method = RequestMethod.GET)
    public String orderCollectSign(ModelMap map) {
        List<SelectObject> res = new ArrayList<>();
        List<Customer> customers = customerService.getAvailable().getBody();
        res = customers.stream().map(m -> new SelectObject(m.getCode(), m.getName())).collect(Collectors.toList());

        map.put("customers", res);
        return "progressTrace/orderCollectSign";
    }

    @RequestMapping(value = "/orderProgressSearch", method = RequestMethod.GET)
    public String orderProgressSearch(ModelMap map) {
        List<SelectObject> res = new ArrayList<>();
        List<Customer> customers = customerService.getAvailable().getBody();
        if (null != customers)
            res = customers.stream().map(m -> new SelectObject(m.getCode(), m.getName())).collect(Collectors.toList());

        map.put("customers", res);
        return "progressTrace/orderProgressSearch";
    }

    @RequestMapping(value = "/subscribe", method = RequestMethod.GET)
    public String subscribe() {
        return "progressTrace/subscribe";
    }

    @RequestMapping(value = "/refuseSign", method = RequestMethod.GET)
    public String refuseSign(ModelMap map) {
        List<SelectObject> res = new ArrayList<>();
//        List<Customer> customers = customerService.getAvailable().getBody();
//        res = customers.stream().map(m -> new SelectObject(m.getCode(), m.getName())).collect(Collectors.toList());

        map.put("customers", res);
        return "progressTrace/orderRefuse";
    }

    @RequestMapping(value = "/orderSearch", method = RequestMethod.POST)
    public
    @ResponseBody
    Object search(@RequestBody KendoGridRequest params) {
        params.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        params.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        params.addOrder("createdDate", "desc");
        return logisticsOrderService.pageByType(params.getPage(), params.getPageSize(), params.getParams(), null);
    }
}

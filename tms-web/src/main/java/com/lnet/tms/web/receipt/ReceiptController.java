package com.lnet.tms.web.receipt;


import com.lnet.framework.core.*;
import com.lnet.framework.security.UserPrincipal;
import com.lnet.model.oms.order.orderEntity.Receipt;
import com.lnet.oms.contract.api.LogisticsOrderService;
import com.lnet.model.ums.transprotation.transprotationEntity.LogisticsOrder;
import com.lnet.model.oms.order.orderDto.LogisticsOrderReceiptDto;
import com.lnet.model.tms.order.orderDto.OrderReceiptDto;
import com.lnet.tms.contract.spi.ReceiptService;
import com.lnet.ums.contract.api.CustomerService;
import com.lnet.model.ums.customer.customerEntity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/receipt")
@Controller
public class ReceiptController {

    @Resource
    LogisticsOrderService logisticsOrderService;
    // TODO: 2017/1/4
   /* @Autowired
    private FileStorage fileStorage;*/
    @Autowired
    private ReceiptService receiptService;
    @Autowired
    private UserPrincipal userPrincipal;
    @Autowired
    private CustomerService customerService;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public String statusIndex(ModelMap map) {
        map.put("orderTypes", LogisticsOrder.OrderType.values());
        map.put("statuses", LogisticsOrder.Status.values());
        map.put("receiptStatuses", Receipt.Status.values());
        return "/receipt/status/index";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String uploadIndex(ModelMap map) {
        List<SelectObject> res = new ArrayList<>();
        Response<List<Customer>> customers = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        if (customers.isSuccess())
            res = customers.getBody().stream().map(m -> new SelectObject(m.getCode(), m.getName())).collect(Collectors.toList());

        map.addAttribute("customers", res);
        return "/receipt/upload/index";
    }

    @RequestMapping(value = "/scan", method = RequestMethod.GET)
    public String scanIndex(ModelMap map) {
        List<SelectObject> res = new ArrayList<>();
        Response<List<Customer>> response = customerService.findCustomerForBranch(userPrincipal.getCurrentBranchCode());
        if (response.isSuccess())
            res = response.getBody().stream().map(m -> new SelectObject(m.getCode(), m.getName())).collect(Collectors.toList());

        map.addAttribute("customers", res);
        return "/receipt/scan/index";
    }

    @RequestMapping(value = "/delay", method = RequestMethod.GET)
    public String deployIndex(ModelMap map) {
        map.put("orderTypes", LogisticsOrder.OrderType.values());
        map.put("statuses", LogisticsOrder.Status.values());
        map.put("receiptStatuses", Receipt.Status.values());
        return "/receipt/delay/index";
    }

    @RequestMapping(value = "/returnReceipt", method = RequestMethod.POST)
    @ResponseBody
    public Response returnReceipt(@RequestBody List<String> orderIds) {
        return logisticsOrderService.returnReceipt(orderIds);
    }

    @RequestMapping(value = "/retroactive", method = RequestMethod.POST)
    @ResponseBody
    public Response retroactive(@RequestBody List<String> orderIds) {
        return logisticsOrderService.retroactive(orderIds);
    }

    @RequestMapping(value = "/getDelayData", method = RequestMethod.POST)
    @ResponseBody
    public PageResponse<LogisticsOrderReceiptDto> getDelayData(@RequestBody KendoGridRequest request) {
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        PageResponse<LogisticsOrderReceiptDto> pageResponse = logisticsOrderService.delayReceipt(request.getPage(), request.getPageSize(), request.getParams());
        List<LogisticsOrderReceiptDto> dtos = pageResponse.getBody();
        List<Customer> customers = customerService.getAll().getBody();

        dtos.forEach(e -> {
            Optional<Customer> customerOptional = customers.stream().filter(f -> f.getCode().equals(e.getCustomerCode())).findFirst();
            if (customerOptional.isPresent()) {
                e.setCustomerName(customerOptional.get().getName());
            }
        });
        return pageResponse;
    }

    @RequestMapping(value = "/receiptScan/{customerCode}", method = RequestMethod.POST)
    @ResponseBody
    public Response receiptScan(@PathVariable String customerCode, @RequestBody List<String> orderNos) {
        return receiptService.receiptScan(orderNos, customerCode);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Object uploadReceipt(@RequestParam("files[]") MultipartFile[] multipartFiles, @RequestParam("customerCode") String customerCode) throws IOException {
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();

        // TODO: 2016/10/9 这样处理上传的回单，会产生大量废弃的回单图片到图片服务器上，此处可优化
        if (multipartFiles != null && 0 < multipartFiles.length) {
            if (100 < multipartFiles.length)
                return ResponseBuilder.fail("单次上传图片不大于100张！");

            for (MultipartFile file : multipartFiles) {
                Map<String, Object> info = new HashMap<>();
                info.put("name", file.getOriginalFilename());
                info.put("size", file.getSize());

                String fileName = file.getOriginalFilename();

                // TODO: 2017/1/4
                /*FileStorage.StorageFileInfo fileInfo = fileStorage.save(file);
                info.put("url", fileInfo.getHost() + fileInfo.getPath());
                info.put("thumbUrl", fileInfo.getHost() + fileInfo.getThumbPath());*/

                OrderReceiptDto dto = null; /*OrderReceiptDto.builder()
                        .orderNo(fileName.substring(0, fileName.lastIndexOf(".")).trim())
                        .customerCode(customerCode)
                        .uploadedUserId(userPrincipal.getUserId())
                        .fileName(fileInfo.getFilename())//图片的原始名称，包含扩展名
                        .contentType(fileInfo.getContentType())
                        .filePath(fileInfo.getPath())
                        .thumbPath(fileInfo.getThumbPath())
                        .build();*/

                if (fileName.contains("_"))
                    dto.setOrderNo((fileName.substring(0, fileName.indexOf("_"))).trim());

                //上传回单到dfs服务器
                Response response = receiptService.uploadOrderReceipt(dto);
                if (!response.isSuccess())
                    info.put("error", response.getMessage());

                uploadedFiles.add(info);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("files", uploadedFiles);
        return result;
    }

    @RequestMapping(value = "/orderNotReceipt", method = RequestMethod.GET)
    public String orderNotReceipt() {
        return "report/orderNotReceipt";
    }

    @RequestMapping(value = "/receiptRatioStr", method = RequestMethod.GET)
    public String receiptRatioStr() {
        return "report/receiptRatioStr";
    }

    @RequestMapping(value = "/getOrderReceipt", method = RequestMethod.GET)
    public String getOrderReceipt() {
        return "receipt/picture/index";
    }

    @RequestMapping(value = "/getOrderReceipt/{customerCode}/{cOrderNo}", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<OrderReceiptDto>> getOrderReceipt(@PathVariable String customerCode, @PathVariable String cOrderNo) {
        return receiptService.getOrderReceipt(customerCode, cOrderNo);
    }

    @RequestMapping(value = "/searchForReceipt", method = RequestMethod.POST)
    @ResponseBody
    public Response searchForReceipt(@RequestBody KendoGridRequest request) {
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        request.addOrder("o.orderDate", "desc");
        return receiptService.searchForReceipt(request);
    }
}

package com.lnet.tms.web.payableAdd;

import com.lnet.cnaps.contract.api.PayableService;
import com.lnet.framework.core.KendoGridRequest;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.SelectObject;
import com.lnet.model.cnaps.payDto.PayableAddDto;
import com.lnet.model.cnaps.payEntity.PayableAdd;
import com.lnet.model.tms.consign.consignEntity.ConsignOrder;
import com.lnet.tms.contract.spi.consgin.ConsignOrderService;
import com.lnet.tms.web.util.UserPrincipalImpl;
import com.lnet.ums.contract.api.CarrierService;
import com.lnet.ums.contract.api.ExpenseAccountService;
import com.lnet.model.ums.carrier.carrierDto.CarrierListDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("payableAdd")
public class PayableAddController {

    @Resource
    UserPrincipalImpl userPrincipal;

    @Autowired
    CarrierService carrierService;

    @Autowired
    PayableService payableService;

    @Autowired
    ExpenseAccountService expenseAccountService;

    @Resource
    ConsignOrderService consignService;


    @RequestMapping(value = "apply")
    public String apply(ModelMap map){
        getBaseInfo(map);
        return "payableAdd/payableAddApply";
    }


    @RequestMapping(value = "audit", method = RequestMethod.GET)
    public String audit(ModelMap map){
        getBaseInfo(map);
        return "payableAdd/payableAddAudit";
    }


    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(ModelMap map){
        getBaseInfo(map);
        return "payableAdd/payableAddSearch";
    }

    @RequestMapping(value = "searchPayableAdd", method = RequestMethod.POST)
    @ResponseBody
    public Object searchPayableAdd(@RequestBody KendoGridRequest request){
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        return payableService.searchPayableAdd(request.getPage(), request.getPageSize(), request.getParams());
    }

    @RequestMapping(value = "searchAuditPayableAdd", method = RequestMethod.POST)
    @ResponseBody
    public Object searchAuditPayableAdd(@RequestBody KendoGridRequest request){
        request.setParams("branchCode", userPrincipal.getCurrentBranchCode());
        request.setParams("siteCode", userPrincipal.getCurrentSiteCode());
        request.setParams("status", PayableAdd.Status.AUDITED);
        payableService.get("1111");
        return payableService.searchPayableAdd(request.getPage(), request.getPageSize(), request.getParams());
    }

    @RequestMapping(value = "detailEdit/{payableAddId}", method = RequestMethod.GET)
    public String detailEdit(@PathVariable String payableAddId, ModelMap map){
        Response<PayableAddDto> response = payableService.getPayableAddDtoById(payableAddId);
        if(response.isSuccess() && response.getBody() != null){
            PayableAddDto dto = response.getBody();
            Response<ConsignOrder> responseConsignOrder = consignService.getByNo(dto.getCarrierCode(), dto.getConsignOrderCode());
            if(responseConsignOrder.isSuccess() && responseConsignOrder.getBody() != null){
                List<SelectObject> orderNos = responseConsignOrder.getBody().getItems()
                        .stream().map(item ->
                                new SelectObject(item.getOrderNo(), item.getOrderNo()))
                        .collect(Collectors.toList());
                map.put("orderNos", orderNos);
            }
        }
        List<SelectObject> exaccts = expenseAccountService.findChildren("120100").getBody()
                .stream().map(account ->
                        new SelectObject(account.getCode(), account.getName()))
                .collect(Collectors.toList());
        map.put("receiveExaccts", exaccts);

        return "payableAdd/detailEdit";
    }

    @RequestMapping(value = "detailEdit/{payableAddId}", method = RequestMethod.POST)
    @ResponseBody
    public Object getPayableAddById(@PathVariable String payableAddId){
        return payableService.getPayableAddDtoByIdA(payableAddId);
    }

    @RequestMapping(value = "updatePayableAdd", method = RequestMethod.POST)
    public
    @ResponseBody
    Response<PayableAddDto> updatePayableAdd(@RequestBody PayableAddDto payableAdd){
        return payableService.updatePayableAdd(payableAdd);
    }

    @RequestMapping(value = "createPayableAdd", method = RequestMethod.GET)
    public String create(ModelMap map){
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.put("carriers", carObject);
        List<SelectObject> exaccts = expenseAccountService.findChildren("120100").getBody()
                .stream().map(account ->
                        new SelectObject(account.getCode(), account.getName()))
                .collect(Collectors.toList());
        map.put("receiveExaccts", exaccts);
        return "payableAdd/create";
    }

    @RequestMapping(value = "createPayableAdd", method = RequestMethod.POST)
    @ResponseBody
    public Object createPayableAdd(@RequestBody PayableAddDto dto){
        dto.setBranchCode(userPrincipal.getCurrentBranchCode());
        dto.setSiteCode(userPrincipal.getCurrentSiteCode());
        dto.setCreateUserId(userPrincipal.getUserId());
        return payableService.createPayableAdd(dto);
    }

    @RequestMapping(value = "getConsignOrder/{carrierCode}", method = RequestMethod.GET)
    @ResponseBody
    public List<SelectObject> getConsignOrder(@PathVariable String carrierCode) {
        // TODO: 2017/1/5  
        /*if(!(carrierCode == null || carrierCode.isEmpty())){
            Response<List<ConsignOrder>> resp = consignService.payableModifyQuery(carrierCode);
            if (resp.isSuccess()) {
                return resp.getBody().stream()
                        .map(m -> new SelectObject(m.getConsignOrderId(), m.getConsignOrderNo(), m.getConsignOrderNo()))
                        .collect(Collectors.toList());
            }
        }*/
        return null;
    }

    @RequestMapping(value = "getOrderByConsignOrderNo/{carrierCode}/{consignOrderNo}", method = RequestMethod.GET)
    @ResponseBody
    public List<SelectObject> getOrderByConsignOrderId(@PathVariable String carrierCode, @PathVariable String consignOrderNo) {
        // TODO: 2017/1/5  
        /*if(!(consignOrderNo == null || carrierCode == null)){
            Response<ConsignOrder> response = consignService.getByNo(carrierCode, consignOrderNo);
            if(response.isSuccess() && response.getBody() != null){
                List<SelectObject> orderNos = response.getBody().getItems()
                        .stream().map(item ->
                                new SelectObject(item.getOrderNo(), item.getOrderNo(), item.getOrderNo()))
                        .collect(Collectors.toList());
                return orderNos;
            }
        }*/
        return null;
    }

    @RequestMapping(value = "detailAudit", method = RequestMethod.GET)
    public String detailAudit(){
        return "payableAdd/detailAudit";
    }

    @RequestMapping(value = "passPayableAdd", method = RequestMethod.POST)
    @ResponseBody
    public Object passPayableAdd(@RequestBody PayableAddDto dto){
        dto.setApprovedUserId(userPrincipal.getUserId());
        dto.setApprovedDate(LocalDateTime.now());
        return payableService.auditPayableAdd(dto);
    }

    @RequestMapping(value = "reject", method = RequestMethod.GET)
    public String reject(){
        return "payableAdd/reject";
    }

    @RequestMapping(value = "rejectPayableAdd", method = RequestMethod.POST)
    @ResponseBody
    public Object rejectPayableAdd(@RequestBody Map request){
        return payableService.rejectPayableAdd((String) request.get("payableAddId"), (String) request.get("rejectedNotes"));
    }

    @RequestMapping(value = "detailSearch", method = RequestMethod.GET)
    public String detailSearch(){    return "payableAdd/detailSearch";   }

    private void getBaseInfo(ModelMap map){
        Map<String, String> userPrincipalBindings = userPrincipal.getBindings();
        Response<List<CarrierListDto>> carrierResponse = carrierService.getBranchAvailable(userPrincipal.getCurrentBranchCode());
        List<SelectObject> carObject = new ArrayList<>();
        if (carrierResponse.isSuccess()) {
            carObject = carrierResponse.getBody().stream().map(m -> new SelectObject(m.getCarrierId(), m.getCode(), m.getName())).collect(Collectors.toList());
        }
        map.addAttribute("carriers", carObject);
        map.addAttribute("status", PayableAdd.Status.values());
    }
}

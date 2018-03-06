<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="TransferArriveController">
    <div class="action-bar">
        <button class="k-button" ng-click="arrive()">
            <i class="fa fa-plus"></i> 到货确认
        </button>
        <button class="k-button" ng-click="enterReceiptInfo()">
            <i class="fa fa-plus"></i> 回单信息录入
        </button>
    </div>
    <div class="search-bar">
        <form id="transferSearchForm" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <@form.ComboBox listItem=customers ngModel="customer" id="searchCustomerCode" name = "customerCode" label="客户"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchOrderNo" name = "orderNo" label= "单号"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchCustomerOrderNo" name = "customerOrderNo" label= "客户单号"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchConsignOrderNo" name = "consignOrderNo" label= "托运单号"/>
            </div>
            <div class="form-group">
                <@form.Dropdown id = "searchStatus" name = "status" listItem = statusEnum label= "状态" valueField="" placeholder="-------请选择-------"/>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="transferGrid"></div>
</div>
</@layout.master>

<script>
    var dispatchTypeEnum = ${jsonMapper.writeValueAsString(dispatchTypeEnum)};
    var transportTypeEnum = ${jsonMapper.writeValueAsString(transportTypeEnum)};
    var handoverTypeEnum = ${jsonMapper.writeValueAsString(handoverTypeEnum)};
</script>
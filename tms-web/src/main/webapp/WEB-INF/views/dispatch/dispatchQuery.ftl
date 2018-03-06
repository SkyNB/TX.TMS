<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="DispatchQueryController">
    <div class="action-bar">
        <button class="k-button" ng-click="createDispatch()" >
            <i class="fa fa-plus"></i>创建派车单 </button>
    </div>
    <div class="search-bar">
        <form id="fromSearchDispatchQuery" class="form-inline" ng-submit="search()">
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
                <@form.Dropdown id="searchOrderType" name = "orderType" label="订单指令" valueField="" listItem=orderTypes placeholder="-------请选择-------"/>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="gridDispatchQuery" ></div>
    <#--<div class="selected-data">
        <span ng-repeat="item in selectedData" class="label label-primary">{{item.code}}    <i class="fa fa-remove"></i></span>
    </div>-->
</div>
</@layout.master>
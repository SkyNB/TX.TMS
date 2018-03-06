<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="ConsignController">
    <div class="action-bar">
    <#--<a href="#/select" data-target="#addConsign" data-toggle="modal" kendo-button id="createBtn">
        <i class="fa fa-plus"></i> 创建 </a>-->
    <#--<button class="k-button" ng-click="select()">
        <i class="fa fa-plus"></i> 创建
    </button>-->
        <button class="k-button" ng-click="update()">
            <i class="fa fa-plus-circle"></i> 修改
        </button>
        <button class="k-button" ng-click="consign()">
            <i class="fa fa-male"></i> 发运
        </button>
    <#--<button class="k-button" ng-click="startUp()">
        <i class="fa fa-truck"></i> 启运
    </button>-->
        <button class="k-button" ng-click="arrive()">
            <i class="fa fa-institution"></i> 到达
        </button>
        <button class="k-button" ng-click="finish()">
            <i class="fa fa-check-square-o"></i> 完成
        </button>
        <button class="k-button" ng-click="cancel()">
            <i class="fa fa-remove"></i> 取消
        </button>
        <button class="k-button" ng-click="updateOrderNo()">
            <i class="fa fa-check-square-o"></i> 替换临时单号
        </button>
    </div>
    <div class="search-bar">
        <form id="fromSearchConsign" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <@form.ComboBox listItem=carriers ngModel="carrier" id="searchCarrierCode" name = "carrierCode" label="承运商"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchConsignOrderNo" name = "consignOrderNo" label= "托运单号"/>
            </div>
            <div class="form-group">
                <@form.Dropdown id = "searchStatus" name = "status" listItem = statusList label= "状态" valueField="" placeholder="-------请选择-------"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchConsignee" name = "consignee" label= "收货人"/>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="gridConsign"></div>
<#--<div class="selected-data">
    <span ng-repeat="item in selectedData" class="label label-primary">{{item.code}}    <i class="fa fa-remove"></i></span>
</div>-->
</div>
<script>
    var transportTypes = ${jsonMapper.writeValueAsString(transportTypes)};
</script>
</@layout.master>
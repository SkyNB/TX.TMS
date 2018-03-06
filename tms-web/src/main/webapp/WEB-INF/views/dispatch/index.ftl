<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
<script src="${request.contextPath}/resources/layouts/LodopFuncs.js" type="text/javascript"></script>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="DispatchController">
    <div class="action-bar">
        <button class="k-button" ng-click="create()" >
            <i class="fa fa-plus"></i> 创建 </button>
        <button class="k-button" ng-click="addOrders()" >
            <i class="fa fa-plus-circle"></i> 加单 </button>
        <button class="k-button" ng-click="removeOrders()" >
            <i class="fa fa-remove"></i> 减单 </button>
        <button class="k-button" ng-click="assign()" >
            <i class="fa fa-male"></i> 指派司机 </button>
        <button class="k-button" ng-click="loading()" >
            <i class="fa fa-check-square-o"></i> 扫描装车 </button>
        <button class="k-button" ng-click="loaded()" >
            <i class="fa fa-unlock-alt"></i> 完成装车 </button>
        <button class="k-button" ng-click="start()" >
            <i class="fa fa-truck"></i> 发车 </button>
        <button class="k-button" ng-click="updateFee()" >
            <i class="fa fa-check-square-o"></i> 修改费用 </button>
        <button class="k-button" ng-click="finish()" >
            <i class="fa fa-institution"></i> 完成 </button>
        <button class="k-button" ng-click="trucking()">
            <i class="fa fa-print"></i> 打印派车单 </button>
        <button class="k-button" ng-click="cancel()" >
            <i class="fa fa-remove"></i> 取消 </button>
    </div>
    <div class="search-bar">
        <form id="fromSearchDispatch" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchNumber">派车单号</label>
                <div class="col-sm-9">
                    <input type="text" class="k-textbox" id="searchNumber"
                           name="dispatchNumber" autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchDriver">司机</label>
                <div class="col-sm-9">
                    <input class="k-textbox" id="searchDriver" name="driver"
                           autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <@form.Dropdown id = "searchStatus" name = "status" listItem = statusEnum label= "状态" valueField="" placeholder="-------请选择-------"/>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="gridDispatch"></div>
</div>
</@layout.master>
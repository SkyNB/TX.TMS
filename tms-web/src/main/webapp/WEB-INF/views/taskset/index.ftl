<#import "/WEB-INF/layouts/master.ftl" as layout/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="MangageController">
    <div class="action-bar">
        <a href="#/addtask" data-target="#addTask" data-toggle="modal" kendo-button id="add">
            <i class="fa fa-plus"></i> 增加 </a>

        <#--<a href="#/importtask" class="k-button" data-target="#importCustomer" data-toggle="modal">
            <i class="fa fa-reply"></i> 导入 </a>-->

        <a href="#" kendo-button ng-click="">
            <i class="fa fa-ban"></i> 删除 </a>
    </div>
    <div class="search-bar">
        <form id="fromSearchCustomer" class="form-inline" ng-submit="search()">
            <div class="form-group ">
                <label class="control-label col-sm-3" for="searchName">名称</label>

                <div class="col-sm-9">
                    <input type="text" class="k-textbox" id="searchName"
                           name="name" autocomplete="off">
                </div>
            </div>
            <div class="form-group ">
                <label class="control-label col-sm-3" for="searchCode">类型</label>

                <div class="col-sm-9">
                    <select kendo-drop-down-list  id="taskTeamType"
                            name="taskTeamType"
                            required="required">
                        <option value="">请选择...</option>
                        <#list taskTeamTypes as item>
                            <option value="${item}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="control-label col-sm-3" for="ratingCodeIn">司机</label>
                <div class="col-sm-9">
                    <input kendo-combo-box k-options="vehicleOptions" id="searchVehicle" name="vehicleCode"
                           autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>

    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="gridCustomer"></div>

</div>
</@layout.master>
<#import "/WEB-INF/layouts/master.ftl" as layout/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="OrderTraceController">
    <div class="action-bar">
        <button class="k-button" ng-click="addOrderTrace()" >
            <i class="fa fa-plus"></i> 添加跟踪信息 </button>
        <button class="k-button" ng-click="subscribe()" >
            <i class="fa fa-edit"></i> 预约 </button>
    </div>
    <div class="search-bar">
        <form id="fromSearchOrder" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchOrderNo">单号</label>

                <div class="col-sm-9">
                    <input class="k-textbox" id="searchOrderNo" name="orderNo"
                           autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchCustomerNo">客户单号</label>

                <div class="col-sm-9">
                    <input type="text" class="k-textbox" id="searchCustomerNo" name="customerOrderNo" autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchStatus">状态</label>
                <div class="col-sm-9">
                    <select kendo-drop-down-list id="searchStatus" name="status">
                        <option value="">请选择...</option>
                        <#list statuses as item>
                            <option value="${item}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchType">订单类型</label>
                <div class="col-sm-9">
                    <select kendo-drop-down-list id="searchType" name="orderType">
                        <option value="">请选择...</option>
                        <#list orderTypes as item>
                            <option value="${item}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="gridOrder"></div>
</div>
</@layout.master>
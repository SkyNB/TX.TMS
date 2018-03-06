<#import "/WEB-INF/layouts/master.ftl" as layout/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="ReceiptDelayController">
    <div class="action-bar">

    </div>
    <div class="search-bar">
        <form id="fromSearchReceiptDelay" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchCustomer">客户</label>

                <div class="col-sm-9">
                    <input kendo-combo-box k-options="customerOptions" id="searchCustomer" name="customerCode"
                           autocomplete="off">
                </div>
            </div>
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
                <label class="control-label col-sm-3" for="orderDateStart">订单日期</label>
                <div class="col-sm-9">
                    <input id="orderDateStart" name="orderDateStart" kendo-date-picker k-format="'yyyy-MM-dd'"
                           k-culture="'zh-CN'">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-2" for="orderDateEnd">至</label>
                <div class="col-sm-9">
                    <input id="orderDateEnd" name="orderDateEnd" kendo-date-picker k-format="'yyyy-MM-dd'"
                           k-culture="'zh-CN'">
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
                <label class="control-label col-sm-3" for="searchStatus">订单状态</label>
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
                <label class="control-label col-sm-3" for="searchReceiptStatus">回单状态</label>
                <div class="col-sm-9">
                    <select kendo-drop-down-list id="searchReceiptStatus" name="receiptStatus">
                        <option value="">请选择...</option>
                        <#list receiptStatuses as item>
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
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="gridDelay"></div>
</div>
</@layout.master>
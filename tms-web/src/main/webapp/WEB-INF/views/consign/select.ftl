<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
</#assign>

<@layout.master bodyEnd=bodyEnd>

<div ui-view="content" ng-controller="SelectOrderController">
    <div class="action-bar">
        <button class="k-button" ng-click="select()">
            <i class="fa fa-plus"></i> 开单
        </button>
        <button class="k-button" ng-click="batchConsign()">
            <i class="fa fa-plus"></i> 批量开单
        </button>
        <button class="k-button" ng-click="createByDriver()">
            <i class="fa fa-plus"></i> 按发运司机开单
        </button>
    </div>
    <div class="search-bar">
        <form id="selectOrderForm" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <@form.ComboBox listItem=customers ngModel="customer" id="searchCustomerCode" name = "customerCode" label="客户"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchOrderNo" name = "orderNo" label= "单号"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchCustomerOrderNo" name = "customerOrderNo" label= "客户单号"/>
            </div>
            <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索
            </button>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions"
         k-data-bound='dataBound'  id="selectOrderGrid"></div>
</div>

</@layout.master>
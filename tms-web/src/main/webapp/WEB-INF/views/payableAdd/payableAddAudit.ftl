<#import "/WEB-INF/layouts/master.ftl" as layout>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<@layout.master>
<div ui-view="content" ng-controller="payableAddAuditController">
    <div class="action-bar">
        <button class="k-button" ng-click="audit()" >
            <i class="fa fa-plus"></i> 通过 </button>
        <button class="k-button" ng-click="reject()" >
            <i class="fa fa-ban"></i> 拒绝 </button>
    </div>
    <div class="search-bar">
        <form id="payableAddSearch" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <@form.Dropdown placeholder="请选择" listItem=carriers id="carrierCode" name = "carrierCode" label="承运商" />
            </div>
            <div class="form-group">
                <label class="control-label col-sm-4" for="consignOrderCode">托运单号</label>
                <div class="col-sm-8">
                    <input class="k-textbox" id="orderNo" name="consignOrderCode"
                           autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <@form.Dropdown listItem=status placeholder="请选择" valueField="" id="status" label="审核状态"/>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button">
                    <i class="fa fa-search"></i>&nbsp;搜索
                </button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="payableAddGrid"></div>
</div>

</@layout.master>
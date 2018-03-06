<#import "/WEB-INF/layouts/master.ftl" as layout>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<@layout.master>
<div ui-view="content" ng-controller="payableAddSearchController">
    <div class="search-bar">
        <form id="payableAddSearch" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <@form.Dropdown placeholder="请选择" listItem=carriers valueField="" id="carrierCode" name = "carrierCode" label="承运商" />
            </div>
            <div class="form-group">
                <label class="control-label col-sm-4" for="consignOrderCode">托运单号</label>
                <div class="col-sm-8">
                    <input class="k-textbox" id="orderNo" name="consignOrderCode"
                           autocomplete="off">
                </div>
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
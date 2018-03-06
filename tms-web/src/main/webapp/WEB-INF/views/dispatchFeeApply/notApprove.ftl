<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="DispatchFeeApplyController">
    <div class="action-bar">
    </div>
    <div class="search-bar">
        <form id="searchDispatchFeeApplyForm" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchDispatchNumber">派车单号</label>
                <div class="col-sm-9">
                    <input type="text" class="k-textbox" id="searchDispatchNumber"
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
                <label class="control-label col-sm-3" for="searchVehicleNo">车牌号</label>
                <div class="col-sm-9">
                    <input class="k-textbox" id="searchVehicleNo" name="vehicleNo"
                           autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="dispatchFeeApplyGrid"></div>
</div>
</@layout.master>
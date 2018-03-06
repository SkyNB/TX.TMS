<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
<script src="${request.contextPath}/resources/layouts/LodopFuncs.js" type="text/javascript"></script>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="DispatchController">
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
                <label class="control-label col-sm-3" for="searchOrderNo">订单号</label>
                <div class="col-sm-9">
                    <input class="k-textbox" id="searchOrderNo" name="orderNo"
                           autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="gridDispatch"></div>
</div>
</@layout.master>
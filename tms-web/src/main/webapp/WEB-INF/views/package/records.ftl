<#import "/WEB-INF/layouts/master.ftl" as layout>
<@layout.master>
<div ui-view="content" ng-controller="PackageController">
    <div class="action-bar"><#--
        <button class="k-button" ng-click="mergePackage()">
            <i class="fa fa-plus"></i> 合单打包
        </button>

        <button class="k-button" ng-click="batchConfirm()">
            <i class="fa fa-remove"></i> 批量确认
        </button>-->
    </div>
    <div class="search-bar">
        <form id="fromSearchPackage" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <label class="control-label col-sm-3" for="searchPackageNo">箱号</label>

                <div class="col-sm-9">
                    <input type="text" class="k-textbox" id="searchPackageNo"
                           name="packageNo" autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3" for="goodsDesc">货物描述</label>
                <div class="col-sm-9">
                    <input class="k-textbox" id="goodsDesc" name="goodsDesc"
                           autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="gridPackage"></div>
</div>
</@layout.master>
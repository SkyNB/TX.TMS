<#import "/WEB-INF/layouts/master.ftl" as layout>
<@layout.master>
<div ui-view="content" ng-controller="OrderController">
    <div class="tabbable-line">
        <ul class="nav nav-tabs ">
            <li class="active">
                <a href="#orderPackage" data-toggle="tab" > 已打包 </a>
            </li>
            <li>
                <a href="#allOrders" data-toggle="tab" ng-click="packageShow()"> 所有 </a>
            </li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane active" id="orderPackage">

                <div class="action-bar">
                <#-- <button class="k-button" ng-click="mergePackage()">
                        <i class="fa fa-plus"></i> 合单打包
                    </button>

                    <button class="k-button" ng-click="batchConfirm()">
                        <i class="fa fa-remove"></i> 批量确认
                    </button>-->
                </div>
                <div class="search-bar">
                    <form id="fromPackageSearch" class="form-inline" ng-submit="packageSearch()">
                        <div class="form-group">
                            <label class="control-label col-sm-3" for="orderNo2">单号</label>

                            <div class="col-sm-9">
                                <input type="text" class="k-textbox" id="orderNo2"
                                       name="orderNo" autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-sm-3" for="packageNo2">箱号</label>
                            <div class="col-sm-9">
                                <input class="k-textbox" id="packageNo2" name="packageNo"
                                       autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索
                            </button>
                        </div>
                    </form>
                </div>
                <div class="dynamic-height" kendo-ex-grid k-options="gridPackOptions" id="gridOrderPackage" ></div>
            </div>
            <div class="tab-pane" id="allOrders">
                <div class="action-bar">
                    <button class="k-button" ng-click="mergePackage()">
                        <i class="fa fa-plus"></i> 打包
                    </button>

                    <button class="k-button" ng-click="batchConfirm()">
                        <i class="fa fa-remove"></i> 批量确认
                    </button>
                </div>
                <div class="search-bar">
                    <form id="fromSearchOrder" class="form-inline" ng-submit="search()">
                        <div class="form-group">
                            <label class="control-label col-sm-3" for="customerOrderNo">客户单号</label>

                            <div class="col-sm-9">
                                <input type="text" class="k-textbox" id="customerOrderNo"
                                       name="customerOrderNo" autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-sm-3" for="searchCode">单号</label>
                            <div class="col-sm-9">
                                <input class="k-textbox" id="searchCode" name="orderNo"
                                       autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索
                            </button>
                        </div>
                    </form>
                </div>
                <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="gridOrder" k-auto-bind="false"></div>
                <div class="selected-data">
        <span ng-repeat="item in lastPageData" class="label label-primary">{{item.orderNo}}
            <i class="fa fa-remove" ng-click="remove(item.orderId)"></i></span>
                </div>
            </div>
        </div>
    </div>
</div>
</@layout.master>
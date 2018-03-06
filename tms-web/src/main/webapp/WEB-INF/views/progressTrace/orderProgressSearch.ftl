<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="OrderProgressSearchController">

    <div>
        <form id="orderProgressSearchForm" class="form-horizontal" ng-submit="submit()" data-role="form">
            <br/>
            <div class="form-group col-sm-6">
                <label class="control-label col-sm-4" for="customerCode">客户</label>
                <div class="col-sm-8">
                    <select kendo-drop-down-list id="customerCode" name="customerCode" required="required">
                        <option value="">请选择...</option>
                        <#if item?size gt 0 >
                            <#list customers as item>
                                <option value="${item.value}">${item.text}</option>
                            </#list>
                        </#if>
                    </select>
                </div>
            </div>
            <div class="form-group col-sm-6">
                <label class="control-label col-sm-2">客户单号</label>
                <div class="col-sm-10">
                    <input type="text" class="k-textbox" id="customerOrderNo" name="customerOrderNo"/>
                    <button kendo-button type="button" class="k-button" id="findConsignOrder"
                            ng-click="findOrder()"><i class="fa fa-search"></i>&nbsp;搜索
                    </button>
                </div>
            </div>


            <div class="form-group col-sm-12">
                <kendo-grid options="mainGridOptions" id="orderGrid">
                    <div k-detail-template>
                        <div>
                            <div kendo-grid k-options="detailGridOptions(dataItem)"></div>
                        </div>
                    </div>
                </kendo-grid>
            </div>

        <#--<div>
            <div class="row">
                <div class="form-group col-sm-5" align="right">
                    <span >物流信息</span>
                </div>
            </div>
        </div>-->
        <#--timeline-->
            <div class="about4">
                <div id="isHide"><h3>无信息显示,请输入客户单号查询。</h3></div>
                <ul class="main_li">
                    <li ng-repeat="item in traceInfo">
                        <span class="time" ng-bind="item.operateTime"></span>
                        <span class="decttion"></span>
                        <div class="straight_line"></div>
                        <div class="event_conter">
                        <#--地点：<span ng-bind="item.operateAddress"></span> <br/>-->
                            内容：<span ng-bind="item.description"></span><br/>
                        </div>
                    </li>
                </ul>

            </div>

        <#--timeline-->

        <#--       <div class="container">
                   <div class="row">
                       <div class="col-sm-9">
                           <div class="VivaTimeline">
                               <dl>
                                   <dd class="pos-right clearfix" ng-repeat="item in traceInfo">
                                       <div class="circ"></div>
                                       <div class="time" ng-bind="item.operateTime"></div>
                                       <div class="events">
                                           <div class="events-header" ng-bind="item.description"></div>
                                       </div>
                                   </dd>
                               </dl>
                           </div>
                       </div>
                   </div>
               </div>-->

        </form>

    </div>

</div>

</@layout.master>

<link href="${request.contextPath}/resources/global/plugins/timeline/css/style.css" rel="stylesheet" type="text/css"/>
<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ng-controller="OrderCollectSignController">
    <div>
        <form id="collectSignForm" class="form-horizontal" ng-submit="submit()" data-role="form">
            <br/>
            <div class="row">
            <div class="form-group col-sm-4">
                <label class="control-label col-sm-4" for="customerCode">客户</label>
                <div class="col-sm-8">
                    <select kendo-drop-down-list id="customerCode" name="customerCode" required="required">
                        <option value="">请选择...</option>
                        <#list customers as item>
                            <option value="${item.value}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="form-group col-sm-5">
                <label class="control-label col-sm-2">客户单号</label>
                <div class="col-sm-10">
                    <input type="text" class="k-textbox" id="customerOrderNo" name="customerOrderNo"/>
                    <button kendo-button type="button" class="k-button" id="findConsignOrder"
                            ng-click="findOrder()"><i
                            class="fa fa-search"></i>&nbsp;搜索
                    </button>
                </div>
            </div>
            <div class="form-group col-sm-3">
                <@form.Text id="signMan" label="签收人" ngModel="orderCollectSign.signMan"/>
            </div>
            </div>
            <div class="row">
            <div class="form-group col-sm-4">
                <label class="control-label col-sm-4">签收人身份证</label>
                <div class="col-sm-8">
                    <input type="text" kendo-masked-text-box k-mask="'000000-00000000-0000'"
                           ng-model="orderCollectSign.signManCard"/>
                </div>
            </div>

            <div class="form-group col-sm-4" style="width: 21%;">
                <@form.Text id="agentSignMan" label="代理签收人" ngModel="orderCollectSign.agentSignMan"/>
            </div>
            <div class="form-group col-sm-4">
                <label class="control-label col-sm-6" style="width: 88%;">代理签收人身份证</label>
                <div class="col-sm-1">
                    <input type="text" kendo-masked-text-box k-mask="'000000-00000000-0000'"
                           ng-model="orderCollectSign.agentSignManCard"/>
                </div>
            </div>
            </div>
            <div class="row">
            <div class="form-group col-sm-4">
                <@form.DateTimePicker id="signTime" label="签收时间" ngModel="orderCollectSign.signTime" required="required"/>
            </div>
            <div class="form-group col-sm-4" style="width: 21%;">
                <label class="control-label col-sm-4">反馈签收时间
                    <span class="required" aria-required="true"> * </span>
                </label>
                <div class="col-sm-8">
                    <input type="datetime" kendo-date-time-picker ng-model="orderCollectSign.feedbackSignTime"
                           k-culture="'zh-CN'" k-format="'yyyy-MM-dd HH:mm:ss'"
                           name="feedbackSignTime" required="required"/>
                </div>
            </div>

            <div class="form-group col-sm-4">
                <label class="control-label col-sm-6" style="width: 88%;">备注</label>
                <div class="col-sm-1">
                        <textarea type="text" class="k-textbox" rows="3" cols="20"
                                  ng-model="orderCollectSign.remark"></textarea>
                </div>
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
            <div align="right">
                <button type="submit" id="submit" class="btn btn-primary">
                    <i class="fa fa-check"></i>&nbsp;确定
                </button>
                <button type="button" tabindex="-1" class="btn btn-danger" id="cancel" ng-click="clear()">
                    <i class="fa fa-close"></i>&nbsp;取消
                </button>

            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="gridOrder"></div>
</div>
</@layout.master>
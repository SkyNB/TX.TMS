<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ng-controller="SignController">
    <div>
        <form id="signForm" class="form-horizontal" ng-submit="submit()" data-role="form">
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
                    <label class="control-label col-sm-3">客户单号</label>
                    <div class="col-sm-9">
                        <input type="text" class="k-textbox" id="customerOrderNo" name="customerOrderNo"/>
                        <button kendo-button type="button" class="k-button" id="findConsignOrder"
                                ng-click="findOrder()"><i
                                class="fa fa-search"></i>&nbsp;搜索
                        </button>
                    </div>
                </div>
                <div class="form-group col-sm-3">
                    <@form.Text id="signMan" label="签收人" ngModel="orderSign.signMan"/>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-4">
                    <label class="control-label col-sm-4">签收人身份证</label>
                    <div class="col-sm-6">
                        <input type="text" kendo-masked-text-box k-mask="'000000-00000000-0000'"
                               ng-model="orderSign.signManCard"/>
                    </div>
                </div>

                <div class="form-group col-sm-4" style="width: 31%">
                    <@form.Text id="agentSignMan" label="代理签收人" ngModel="orderSign.agentSignMan" />
                </div>
                <div class="form-group col-sm-4">
                    <label class="control-label col-sm-6" style="width: 58%;">代理签收人身份证</label>
                    <div class="col-sm-5">
                        <input type="text" kendo-masked-text-box k-mask="'000000-00000000-0000'"
                               ng-model="orderSign.agentSignManCard"/>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-4">
                    <@form.DateTimePicker id="signTime" label="签收时间" ngModel="orderSign.signTime" required="required"/>
                </div>
                <div class="form-group col-sm-5">
                    <label class="control-label col-sm-3">反馈签收时间
                        <span class="required" aria-required="true"> * </span>
                    </label>
                    <div class="col-sm-8">
                        <input type="datetime" kendo-date-time-picker ng-model="orderSign.feedbackSignTime"
                               k-culture="'zh-CN'" k-format="'yyyy-MM-dd HH:mm:ss'"
                               name="feedbackSignTime" required="required"/>
                    </div>
                </div>

                <div class="form-group col-sm-3" style="color:red">
                    <@form.NumberBox id="yeSignNumber" label="未签收箱数" ngModel="orderSign.yetSignNumber" readonly="readonly" required="required" format="0"/>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-3" style="width: 32%">
                    <@form.NumberBox id="signNumber" label="签收箱数" ngModel="orderSign.signNumber" required="required" format="0"/>
                </div>
                <div class="col-sm-8">
                    <label class="control-label col-sm-1" style="width: 15%;">备注&nbsp;&nbsp;</label>
                    &nbsp;&nbsp;
                    <div class="col-sm-10">
                        <textarea type="text" class="k-textbox" rows="3" cols="20"
                                  ng-model="orderSign.remark"></textarea>
                    </div>
                </div>
            </div>
            <br/>
            <div class="form-group col-sm-12">
                <kendo-grid options="mainGridOptions" id="orderGrid">
                    <div k-detail-template>
                        <div>
                            <div kendo-grid k-options="detailGridOptions(dataItem)"></div>
                        </div>
                    </div>
                </kendo-grid>
            </div>
            <div class="col-sm-12" align="right">

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
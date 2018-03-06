<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ng-controller="ConsignOrderStartupController">
    <div>
        <form id="formSearchConsign" class="form-horizontal" data-role="form">
            <br/>
            <div class="form-group col-md-4">
                <label class="control-label col-sm-4" for="carrierCode">承运商
                    <span class="required" aria-required="true"> * </span>
                </label>
                <div class="col-sm-8">
                    <select kendo-drop-down-list id="carrierCode" name="carrierCode" required="required">
                        <option value="">请选择...</option>
                        <#list carriers as item>
                            <option value="${item.value}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="form-group col-sm-5">
                <label class="control-label col-md-3">托运单号</label>
                <div class="col-sm-9">
                    <input type="text" class="k-textbox" id="consignNo" name="consignNo"/>
                    <button kendo-button type="button" class="k-button" id="findConsignOrder"
                            ng-click="findConsignOrder()"><i
                            class="fa fa-search"></i>搜索
                    </button>
                </div>
            </div>
            <div class="form-group col-sm-3">
                <@form.DateTimePicker id="startupTime" label="启运时间" ngModel="startupTime"/>
            </div>

            <div class="form-group col-md-12">
                <kendo-grid options="mainGridOptions" id="consignGrid">
                    <div k-detail-template>
                        <div>
                            <div kendo-grid k-options="detailGridOptions(dataItem)"></div>
                        </div>
                    </div>
                </kendo-grid>
            </div>
            <div class="col-md-12" align="right">

                <button type="button" id="submit" ng-click="submit()" class="btn btn-primary">
                    <i class="fa fa-check"></i>&nbsp;确定
                </button>
                <button type="button" tabindex="-1" class="btn btn-danger" id="cancel" ng-click="clear()">
                    <i class="fa fa-close"></i>&nbsp;取消
                </button>
            </div>
        </form>
    </div>
</div>
</@layout.master>
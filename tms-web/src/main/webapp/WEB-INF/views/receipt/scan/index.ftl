<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ng-controller="ReceiptScanController">
    <div>
        <form id="receiptScanForm" class="form-horizontal" data-role="form">
            <br/><br/>
            <div class="form-group col-sm-6">
                <label class="control-label col-sm-4" for="customerCode">客户
                    <span class="required" aria-required="true"> * </span>
                </label>
                <div class="col-sm-8">
                    <select kendo-drop-down-list id="customerCode" name="customerCode" required="required">
                        <option value="">请选择...</option>
                        <#list customers as item>
                            <option value="${item.value}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="form-group col-sm-6">
                <label class="control-label col-sm-2">客户单号</label>
                <div class="col-sm-10">
                    <input type="text" class="k-textbox" id="customerOrderNo" name="customerOrderNo"/>
                    <button kendo-button type="button" class="k-button" id="findOrder" ng-click="findOrder()"><i
                            class="fa fa-search"></i>&nbsp;搜索
                    </button>
                </div>
            </div>

            <div class="form-group col-sm-12">
                <div kendo-grid k-data-source="orderDataSource"
                     k-editable="true"
                     k-columns="orderColumns"
                     id="orderInfo"
                     name="orderInfo">
                </div>
            </div>
            <div align="right">
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
<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>

</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ng-controller="RefuseController">
    <div>
        <form id="refuseForm" class="form-horizontal" ng-submit="submit()" data-role="form">
            <br/><br/>
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
                <@form.NumberBox id="refuseNum" label="拒签箱数" ngModel="refuseNum" required="required" format="0" min=1/>
            </div>
                <div class="form-group col-sm-4">
                    <@form.Textarea id="reason" label="拒签理由" ngModel="reason" required="required"/>
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


            <div  class="col-sm-12" align="right">

                <button type="submit" id="submit" class="btn btn-primary">
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
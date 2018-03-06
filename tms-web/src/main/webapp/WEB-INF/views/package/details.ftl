<#import "/WEB-INF/layouts/form.ftl" as form>
<div id="detailsPackage" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="detailsPackageForm" class="form-horizontal" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;打包详情</h4>
                </div>
                <div class="modal-body">

                    <div class="row">
                        <div class="form-group col-sm-12">
                        <@form.Textarea id="orderNos" label="订单号" ngModel="package.orderNoStr"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalPackageQty" label="总箱数" required="requried" readonly="readonly" ngModel="package.totalPackageQty" format="0" decimals=0/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalItemQty" label="总数量" required="requried" readonly="readonly" ngModel="package.totalItemQty" format="0" decimals=0/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalWeight" label="总重量" required="requried" readonly="readonly" ngModel="package.totalWeight" format="0.0000㎏" decimals=4/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalVolume" label="总体积" required="requried" readonly="readonly" ngModel="package.totalVolume" format="0.000000m³" decimals=6/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-12">
                        <@form.MultiSelect label="打包人" required="required" ngModel="package.packers" id="packers" listItem=users valueField="userId" textField="fullName" />
                        </div>
                    </div>
                <#--打包明细-->
                    <div id="packageItemGrid" kendo-grid k-data-source="itemDataSource" k-editable="false"
                         k-columns="itemColumns"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
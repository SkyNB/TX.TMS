<#import "/WEB-INF/layouts/form.ftl" as form>

<#--<script>
    receiveExaccts = ${jsonMapper.writeValueAsString(receiveExaccts)};
</script>-->

<div id="create" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="createForm" class="form-horizontal" data-role="form" ng-submit="submit()">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;增加</h4>
                </div>
                <div class="modal-body">

                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.ComboBox listItem=carriers id="carrierCode" label="承运商" options="carrierOptions" ngModel="payableAdd.carrierCode" required="required" />
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.ComboBox listItem="" id="consignOrderCode" label="托运单号" options="consignOrderOptions" ngModel="payableAdd.consignOrderCode" required="required" />
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="totalAmount" label="总金额" ngModel="payableAdd.totalAmount" readonly="readonly" />
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="remark" label="备注" ngModel="payableAdd.remark" />
                        </div>
                    </div>

                    <div id="payableAddGrid" kendo-grid k-data-source="itemDataSource" k-columns="itemColumns" k-options="gripOptions" ></div>
                </div>
                <div class="modal-footer">

                    <button type="submit" class="btn btn-primary">
                        <i class="fa fa-check"></i>&nbsp;保存
                    </button>
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
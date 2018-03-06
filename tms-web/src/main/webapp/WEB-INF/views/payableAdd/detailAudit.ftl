<#import "/WEB-INF/layouts/form.ftl" as form>

<div id="detailAudit" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="createForm" class="form-horizontal" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;费用变更审核</h4>
                </div>
                <div class="modal-body">

                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="carrierCode" label="承运商" ngModel="payableAdd.carrierName" readonly="readonly" />
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="consignOrderCode" label="托运单号" ngModel="payableAdd.consignOrderCode" readonly="readonly" />
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="totalAmount" label="总金额" ngModel="payableAdd.totalAmount" readonly="readonly" />
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="confirmAmount" label="审核金额" ngModel="payableAdd.confirmAmount" readonly="readonly" />
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="remark" label="备注" ngModel="payableAdd.remark" />
                        </div>
                    </div>

                    <div id="payableAddGrid" kendo-grid k-data-source="itemDataSource" k-columns="itemColumns" k-options="gripOptions" ></div>
                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-primary" id="passBtn" ng-click="pass()">
                        <i class="fa fa-check"></i>&nbsp;通过
                    </button>
                    <button type="submit" class="btn btn-primary" id="rejectBtn" ng-click="reject()" ng-show="!isAudit">
                        <i class="fa fa-ban"></i>&nbsp;拒绝
                    </button>
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<div id="reject" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="createForm" class="form-horizontal" data-role="form" ng-submit="rejectSubmit()">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;拒绝</h4>
                </div>
                <div class="modal-body">

                    <div class="row">
                        <div class="form-group container">
                        <@form.Textarea id="remark" rows=4 label="原因" ngModel="rejectedNotes" />
                        </div>
                    </div>

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
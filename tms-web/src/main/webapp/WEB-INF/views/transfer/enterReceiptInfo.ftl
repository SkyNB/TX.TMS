<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="transferEnterReceiptInfo" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="transferEnterReceiptInfoForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;调配回单信息录入</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="receiptInfo" label="回单状况" ngModel="receiptInfo" />
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id = "receiptPostTime" label = "到达时间" ngModel= "receiptPostTime" required="required"/>
                        </div>
                    </div>
                    <div style="padding-left: 88px;margin-bottom: 10px;">
                        <label for="orderNo" class="control-label">单号&nbsp;&nbsp;</label>

                        <input type="text" id="orderNo" name="orderNo" class="k-textbox"/>
                        <button type="button" class="k-button" ng-click="searchOrder()">确定</button>
                    </div>
                    <div class="row">
                        <div kendo-grid k-data-source="transferDataSource" k-data-bound="dataBound" k-editable="false"
                             k-columns="transferColumns" id="transferGrid"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="submit" id="submit" class="btn  btn-primary">
                        <i class="fa fa-check"></i>&nbsp;确定
                    </button>
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>

                </div>
            </form>
        </div>
    </div>
</div>
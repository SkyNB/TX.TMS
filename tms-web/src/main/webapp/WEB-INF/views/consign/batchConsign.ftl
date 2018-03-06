<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="batchConsign" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="batchConsignForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;批量开单发运</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.ComboBox listItem=carriers ngModel="batchConsignModel.carrierCode" id="carrierCode" name = "carrierCode" label="承运商"  required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="transportType" label="运输方式" listItem=transportTypes ngModel="batchConsignModel.transportType" required="required" valueField=""/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id = "consignTime" label = "发运时间" ngModel= "batchConsignModel.consignTime" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id = "feedbackConsignTime" label = "反馈发运时间" ngModel= "batchConsignModel.feedbackConsignTime" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.CheckBox id = "whetherHaveUpstairsFee" label = "是否产生上楼费" ngModel= "batchConsignModel.whetherHaveUpstairsFee"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalOrderCount" label="总单数" ngModel="totalOrderCount" readonly="readonly"  required=true placeholder="自动计算"/>
                        </div>
                    </div>
                    <div class="row">
                        <div><h4>订单明细</h4></div>
                    </div>
                    <div class="row">
                        <div kendo-grid k-data-source="consignItemsDataSource" k-editable="true"
                             k-columns="itemsColumns" id="consignItems"></div>
                    </div>
                </div>
                <div class="modal-footer">

                    <button type="submit" id="submit" class="btn btn-primary">
                        <i class="fa fa-check"></i>&nbsp;按单个订单批量发运
                    </button>
                    <button type="button" id="mergeConsignBtn" class="btn btn-primary" ng-click="mergeConsign()">
                        <i class="fa fa-check"></i>&nbsp;按收货公司合并批量发运
                    </button>
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
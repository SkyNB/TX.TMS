<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="dispatchRemoveOrder" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="dispatchRemoveOrderForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;减单</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="dispatchNumber" label="派车单号" ngModel="dispatch.dispatchNumber" required="required" readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="vehicleNumber" label="车牌号" ngModel="dispatch.vehicleNumber" required="required" readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="driver" label="司机" ngModel="dispatch.driver" required="required" readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="driverPhone" label="司机电话" ngModel="dispatch.driverPhone" required="required" readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="vehicleTypeId" label="车型" ngModel="dispatch.vehicleTypeId" listItem=vehicleTypes required="required"  readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalPackageQuantity" label="总箱数" ngModel="dispatch.totalPackageQuantity" readonly="readonly" format="0" required="required" placeholder="自动计算"  readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="status" label="状态" ngModel="dispatch.status.text" readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalFee" label="费用" ngModel="dispatch.totalFee" readonly="readonly" format="0" required="required" placeholder="自动计算"  readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalVolume" label="总体积" ngModel="dispatch.totalVolume" format="0.000000m³" decimals=6 readonly="readonly"  required=true placeholder="自动计算"  readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalWeight" label="总重量" ngModel="dispatch.totalWeight" format="0.0000kg" decimals=2 readonly="readonly"  required=true placeholder="自动计算"  readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="startAddress" label="起始地" ngModel="dispatch.startAddress" required="required"  readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="destAddress" label="目的地" ngModel="dispatch.destAddress" required="required"  readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="remark" label="备注" ngModel="dispatch.remark"  readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div><h4>派车明细</h4></div>
                    </div>
                    <div class="row">
                        <div kendo-ex-grid k-data-source="dispatchItemsDataSource" k-locked="false"
                             k-extend="false" k-editable="false" k-data-bound='dataBound'
                             k-columns="itemsColumns" id="dispatchItems" name="dispatchItems"></div>
                    </div>
                <#--<div class="row">
                    <div><h4>包（箱）明细</h4></div>
                </div>
                <div class="row">
                    <div kendo-grid k-data-source="dispatchPackagesDataSource" k-editable="false"
                         k-columns="PackagesColumns" id="dispatchPackages" name="dispatchPackages"></div>
                </div>-->
                </div>
                <div class="modal-footer">

                    <button class="btn btn-primary" ng-click="removeOrder()">
                        <i class="fa fa-remove"></i> 减单
                    </button>
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    var orderDispatchTypes = ${jsonMapper.writeValueAsString(orderDispatchTypes)};
    var orderTypes = ${jsonMapper.writeValueAsString(orderTypes)};
    var carriers = ${jsonMapper.writeValueAsString(carriers)};
</script>
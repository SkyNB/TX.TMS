<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="dispatchAssign" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="dispatchAssignForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;派车单指派司机</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="oldDriver" label="原司机"  readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="oldVehicleNumber" label="原车牌号" readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.ComboBox id="vehicleId" label="车辆" ngModel="dispatch.vehicleId" listItem=null options="vehicleOptions" ngChange="vehicleChange()"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="vehicleNumber" label="车牌号" ngModel="dispatch.vehicleNumber" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="driver" label="司机" ngModel="dispatch.driver" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="driverPhone" label="司机电话" ngModel="dispatch.driverPhone" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="vehicleTypeId" label="车型" ngModel="dispatch.vehicleTypeId" listItem=vehicleTypes required="required"/>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="submit" id="submit" class="btn btn-primary">
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
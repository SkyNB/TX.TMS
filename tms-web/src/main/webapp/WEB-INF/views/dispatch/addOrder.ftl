<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="dispatchAddOrder" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="dispatchAddOrderForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;加单</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="vehicleNumber" label="车牌号" ngModel="dispatch.vehicleNumber" required="required" readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="driver" label="司机" ngModel="dispatch.driver" required="required" readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="driverPhone" label="司机电话" ngModel="dispatch.driverPhone" required="required" readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="vehicleTypeId" label="车型" ngModel="dispatch.vehicleTypeId" listItem=vehicleTypes required="required"  readonly="readonly"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalFee" label="总费用" ngModel="dispatch.totalFee" readonly="readonly" format="0" required="required" placeholder="自动计算"  readonly="readonly"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalPackageQuantity" label="总箱数" ngModel="dispatch.totalPackageQuantity" readonly="readonly" format="0" required="required" placeholder="自动计算"  readonly="readonly"/>
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
                        <div kendo-grid k-data-source="dispatchItemsDataSource" k-editable="false"
                             k-columns="itemsColumns" id="dispatchItems" name="dispatchItems"></div>
                    </div>
                    <div class="row">
                        <div><h4>新增明细</h4></div>
                    </div>
                    <div>
                        <label for="orderNo">单号：</label><input type="text" id="orderNo" name="orderNo"
                                                               class="k-textbox"/>
                        <button type="button" class="k-button" ng-click="addOrder()">增加</button>
                        <button type="button" class="k-button" ng-click="addOrderSearch()">更多...</button>
                    </div>
                    <div class="row">
                        <div kendo-grid k-data-source="addItemsDataSource" k-editable="true"
                             k-columns="addItemsColumns" id="addItems" name="addItems"></div>
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

<div id="addOrderSearch" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-body">
                <div class="search-bar">
                    <form id="fromSearchDispatchQuery" class="form-inline" ng-submit="search()">
                        <div class="form-group">
                        <@form.ComboBox listItem=customers ngModel="customer" id="searchCustomerCode" name = "customerCode" label="客户"/>
                        </div>
                        <div class="form-group">
                        <@form.Text id = "searchOrderNo" name = "orderNo" label= "单号"/>
                        </div>
                        <div class="form-group">
                        <@form.Text id = "searchCustomerOrderNo" name = "customerOrderNo" label= "客户单号"/>
                        </div>
                        <div class="form-group">
                        <@form.Dropdown id="searchOrderType" name = "orderType" label="订单/指令" valueField="" listItem=orderTypes placeholder="-------请选择-------"/>
                        </div>
                        <div class="form-group">
                            <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索
                            </button>
                        </div>
                    </form>
                </div>
                <div class="action-bar">
                    <button class="k-button" ng-click="select()">
                        <i class="fa fa-plus"></i> 选择
                    </button>
                </div>
                <div class="dynamic-height" kendo-ex-grid k-options="dispatchQueryGridOptions" k-locked="false"
                     k-extend="false" k-data-bound='dispatchQueryDataBound'
                     id="dispatchQueryGrid"></div>
            </div>

            <div class="modal-footer">
                <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                    <i class="fa fa-close"></i>&nbsp;关闭
                </button>
            </div>
        </div>
    </div>
</div>

<div id="batchConfirm" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="batchConfirmForm" class="form-horizontal" ng-submit="batchPack()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;确认体积重量</h4>
                </div>
                <div class="modal-body">
                    <div class="table-scrollable">
                        <table class="table table-striped table-hover table-bordered dataTable no-footer">
                            <thead>
                            <tr>
                                <th>序号</th>
                                <th>单号</th>
                                <th>客户单号</th>
                                <th>客户</th>
                                <th>重量</th>
                                <th>体积</th>
                                <th>箱数</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr ng-repeat="item in infoData">
                                <td>{{$index + 1}}</td>
                                <td>{{item.orderNo}}</td>
                                <td>{{item.customerOrderNo}}</td>
                                <td>{{item.customerName}}</td>
                                <td><input type="number" required="required" min="0" k-decimals="'4'"
                                           k-format="'0.0000㎏'"
                                           ng-model="item.weight" kendo-numeric-text-box></td>
                                <td><input type="number" required="required" min="0" k-decimals="'6'"
                                           k-format="'0.000000m³'"
                                           ng-model="item.volume" kendo-numeric-text-box></td>
                                <td><input type="number" required="required" min="1" k-decimals="'0'" k-format="'0'"
                                           ng-model="item.packageQty" kendo-numeric-text-box></td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <button id="btnConfirmCreateUser" type="submit" class="btn btn-primary">
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


<script>
    var orderDispatchTypes = ${jsonMapper.writeValueAsString(orderDispatchTypes)};
    var orderTypes = ${jsonMapper.writeValueAsString(orderTypes)};
    var carriers = ${jsonMapper.writeValueAsString(carriers)};
</script>
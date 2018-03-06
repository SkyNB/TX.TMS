<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="addDispatch" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="addDispatchForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;创建派车单</h4>
                </div>
                <div class="modal-body">
                    <div id="rootwizard">
                        <ul class="nav nav-pills nav-justified steps">
                            <li>
                                <a data-toggle="tab" href="#tab1">
                                    <span class="number"> 1 </span>基本信息
                                </a>
                            </li>
                            <li>
                                <a data-toggle="tab" href="#tab2">
                                    <span class="number"> 2 </span>费用
                                </a>
                            </li>
                        </ul>
                        <div class="tab-content">
                            <hr>
                            <div class="tab-pane" id="tab1">
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
                                    <div class="form-group col-sm-6">
                                    <@form.NumberBox id="totalPackageQuantity" label="总箱数" ngModel="dispatch.totalPackageQuantity" readonly="readonly" format="0" required="required" placeholder="自动计算"/>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                    <@form.NumberBox id="totalVolume" label="总体积" ngModel="dispatch.totalVolume" format="0.000000m³" decimals=6 readonly="readonly"  required=true placeholder="自动计算"/>
                                    </div>
                                    <div class="form-group col-sm-6">
                                    <@form.NumberBox id="totalWeight" label="总重量" ngModel="dispatch.totalWeight" format="0.0000kg" decimals=2 readonly="readonly"  required=true placeholder="自动计算"/>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                    <@form.Text id="startAddress" label="起始地" ngModel="dispatch.startAddress" required="required"/>
                                    </div>
                                    <div class="form-group col-sm-6">
                                    <@form.Text id="destAddress" label="目的地" ngModel="dispatch.destAddress" required="required"/>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-12">
                                    <@form.MultiSelect label="跟车人" ngModel="" id="followUserIds" listItem=users valueField="userId" textField="fullName" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                    <@form.Textarea id="remark" label="备注" ngModel="dispatch.remark" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div><h4>派车明细</h4></div>
                                </div>
                                <div>
                                    <label for="orderNo">单号：</label><input type="text" id="orderNo" name="orderNo"
                                                                           class="k-textbox"/>
                                    <button type="button" class="k-button" ng-click="addOrder()">增加</button>
                                    <button type="button" class="k-button" ng-click="addOrderSearch()">更多...</button>
                                </div>
                                <div class="row">
                                    <div kendo-grid k-data-source="dispatchItemsDataSource" k-editable="true" k-on-save="itemChange(kendoEvent)"
                                         k-columns="itemsColumns" id="dispatchItems" name="dispatchItems"></div>
                                </div>
                            </div>
                            <div class="tab-pane" id="tab2">
                                <div class="row">
                                    <div><h4>派车单费用</h4></div>
                                </div>
                                <div class="row">
                                    <div kendo-grid k-data-source="dispatchFeeDetailDataSource" k-editable="true"
                                         k-columns="feeDetailColumns" id="dispatchFeeDetailGrid"
                                         name="dispatchFeeDetailGrid"></div>
                                </div>
                            </div>
                        <#--<div class="row">
                            <div><h4>包（箱）明细</h4></div>
                        </div>
                        <div class="row">
                            <div kendo-grid k-data-source="dispatchPackagesDataSource" k-editable="false"
                                 k-columns="PackagesColumns" id="dispatchPackages" name="dispatchPackages"></div>
                        </div>-->
                        </div>
                        <ul class="pager wizard">
                            <li class="previous"><a href="#"><i class="fa fa-angle-left"></i>上一页</a></li>
                            <li class="next"><a href="#">下一页<i class="fa fa-angle-right"></i></a></li>
                            <li><button type="button" class="btn btn-primary" ng-click="submit();" ng-show="showSubmitBtn">
                                <i class="fa fa-check"></i>&nbsp;确定
                            </button></li>
                        </ul>
                    </div>
                </div>
            <#--<div class="modal-footer">
                <button type="button" tabindex="-1" class="k-button" data-dismiss="modal">
                    <i class="fa fa-close"></i>&nbsp;取消
                </button>
                <button type="submit" id="submit" class="k-button k-primary">
                    <i class="fa fa-check"></i>&nbsp;确定
                </button>
            </div>-->
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
                <div class="action-bagridDispatchQueryr">
                    <button class="k-button" ng-click="select()">
                        <i class="fa fa-plus"></i> 选择
                    </button>
                </div>
                <div class="dynamic-height" kendo-ex-grid k-options="dispatchQueryGridOptions" k-locked="false"
                     k-extend="false" k-data-bound='dispatchQueryDataBound'
                     id="dispatchQueryGrid"></div>
            </div>

            <div class="modal-footer">
                <button type="button" tabindex="-1" class="k-button" data-dismiss="modal">
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
                    <button type="button" tabindex="-1" class="k-button" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                    <button id="btnConfirmCreateUser" type="submit" class="k-button k-primary">
                        <i class="fa fa-check"></i>&nbsp;确定
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>


<script>
    var orderDispatchTypes = ${jsonMapper.writeValueAsString(orderDispatchTypes)};
    <#--var orderDispatchTypes = ${orderDispatchTypes};-->
    var carriers = ${jsonMapper.writeValueAsString(carriers)};
    var orderTypes = ${jsonMapper.writeValueAsString(orderTypes)};
    var startAddress = '${startAddress}';
    var feeDetailDtos = ${jsonMapper.writeValueAsString(feeDetailDtos)};
</script>
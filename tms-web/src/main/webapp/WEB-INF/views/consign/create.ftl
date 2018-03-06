<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="createConsign" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="createConsignForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;创建托运单</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.ComboBox listItem=carriers ngModel="consign.carrierCode" id="carrierCode" name = "carrierCode" label="承运商" ngChange = "carrierChange()" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="consignOrderNo" label="托运单号" ngModel="consign.consignOrderNo" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalPackageQuantity" label="总箱数" ngModel="consign.totalPackageQuantity" readonly="readonly" format="0" required="required" placeholder="自动计算"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.CheckBox id = "isTemporaryNo" label = "是否临时单号" ngModel= "consign.isTemporaryNo"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalVolume" label="总体积" ngModel="consign.totalVolume" format="0.000000m³" decimals=6 readonly="readonly"  required=true placeholder="自动计算"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="totalWeight" label="总重量" ngModel="consign.totalWeight" format="0.0000kg" decimals=2 readonly="readonly"  required=true placeholder="自动计算"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DistrictSelect label="始发城市" id="startCityCode" province="#shipProvince" city="#shipCity" area="#shipDistrict" street="#shipStreet" ngModel="consign.startCityCode" required=true/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.DistrictSelect label="目的城市" id="destCityCode" province="#shipProvince" city="#shipCity" area="#shipDistrict" street="#shipStreet" ngModel="consign.destCityCode" required=true/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="consignee" label="收货人" ngModel="consign.consignee" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="consigneeAddress" label="收货地址" ngModel="consign.consigneeAddress" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="consigneePhone" label="收货人电话" ngModel="consign.consigneePhone" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="receiptPageNumber" label="回单总页数" ngModel="consign.receiptPageNumber" readonly="readonly" format="0" required="required" placeholder="自动计算"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="settlementCycle" label="结算周期" listItem=settleCycles ngModel="consign.settlementCycle" required="required" valueField=""/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="paymentType" label="支付方式" listItem=paymentTypes ngModel="consign.paymentType" required="required" valueField=""/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="calculateType" label="计费方式" listItem=calculateTypes ngModel="consign.calculateType" required="required" valueField=""/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="transportType" label="运输方式" listItem=transportTypes ngModel="consign.transportType" required="required" valueField=""/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="handoverType" label="交接方式" listItem=handoverTypes ngModel="consign.handoverType" required="required" valueField=""/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="goodsName" label="货物名称" ngModel="consign.goodsName"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="carrierDriver" label="承运商司机" ngModel="consign.carrierDriver"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="carrierDriverPhone" label="司机电话" ngModel="consign.carrierDriverPhone"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="carrierVehicleNo" label="车牌号" ngModel="consign.carrierVehicleNo"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="remark" label="备注" ngModel="consign.remark" />
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.CheckBox id = "whetherSignReceipt" label = "是否签回单" ngModel= "consign.whetherSignReceipt"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.CheckBox id = "whetherHaveUpstairsFee" label = "是否产生上楼费" ngModel= "consign.whetherHaveUpstairsFee"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id = "consignTime" label = "发运时间" ngModel= "consign.operationTime.consignTime" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id = "feedbackConsignTime" label = "反馈发运时间" ngModel= "consign.operationTime.feedbackConsignTime" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id = "predictArriveTime" label = "预计到货时间" ngModel= "consign.operationTime.predictArriveTime" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6" style="color: red">
                        <@form.ComboBox id="transferOrganizationCode" label="中转分公司" ngModel="consign.transferOrganizationCode"  listItem=organizations ngChange="getSiteList()"/>
                        </div>
                        <div class="form-group col-sm-6" style="color: red">
                            <label class="control-label col-sm-4" for="transferSiteCode">中转站点</label>
                            <div class="col-sm-8">
                                <select kendo-combo-box ng-model="consign.transferSiteCode" id="transferSiteCode" name="transferSiteCode"
                                        k-placeholder="'选择站点'"
                                        k-data-text-field="'name'"
                                        k-data-value-field="'code'"
                                        k-filter="'contains'"
                                        k-auto-bind="true">
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div><h4>托运单明细</h4></div>
                    </div>
                    <button type="button" class="k-button" ng-click="searchOrder()">增加</button>
                    <div class="row">
                        <div kendo-grid k-data-source="consignItemsDataSource" k-editable="true"
                             k-columns="itemsColumns" id="consignItems" ></div>
                    </div>
                </div>
                <div class="modal-footer">

                    <button type="submit" id="submit" class="btn btn-primary">
                        <i class="fa fa-check"></i>&nbsp;保存
                    </button>
                    <button type="button" id="consignBtn" class="btn btn-primary" ng-click="createAndConsign()">
                        <i class="fa fa-check"></i>&nbsp;发运
                    </button>
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<div id="searchOrder" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-body">
                <div class="search-bar">
                    <form id="searchOrderForm" class="form-inline" ng-submit="search()">
                        <div class="form-group">
                        <@form.ComboBox listItem=customers ngModel="customer" id="searchCustomerCode" name = "customerCode" label="客户"/>
                        </div>
                        <div class="form-group">
                        <@form.Text id = "searchOrderNo" name = "orderNo" label= "单号"/>
                        </div>
                        <div class="form-group">
                        <@form.Text id = "searchCustomerOrderNo" name = "customerOrderNo" label= "客户单号"/>
                        </div>
                        <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索
                        </button>
                    </form>
                </div>
                <div class="action-bar">
                    <button class="k-button" ng-click="select()">
                        <i class="fa fa-plus"></i> 选择
                    </button>
                </div>
                <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-locked="false" k-extend="false"
                     k-data-bound='dataBound'  id="searchOrderGrid"></div>
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
                                <td><input type="number" required="required" min="0" k-decimals="'4'" k-format="'0.0000㎏'"
                                           ng-model="item.weight" kendo-numeric-text-box></td>
                                <td><input type="number" required="required" min="0" k-decimals="'6'" k-format="'0.000000m³'"
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

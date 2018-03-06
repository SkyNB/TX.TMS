<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="consignDetail" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" >
            <form id="consignDetailForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;托运单详情</h4>
                </div>
                <div class="modal-body">
                    <div class="tabbable-line">
                        <ul class="nav nav-tabs ">
                            <li class="active">
                                <a href="#consignInfo" data-toggle="tab"> 基本信息 </a>
                            </li>
                            <li>
                                <a href="#consignLogInfo" data-toggle="tab"> 日志 </a>
                            </li>
                            <li>
                                <a href="#information" data-toggle="tab" ng-click="information()" >跟踪信息</a>
                            </li>
                        </ul>
                        <div class="tab-content">
                            <div class="tab-pane active" id="consignInfo">
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
                                    <@form.Text id="status" label="状态" ngModel="consign.status.text"/>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                    <@form.DateTimePicker id = "consignTime" label = "发运时间" ngModel= "consign.operationTime.consignTime" />
                                    </div>
                                    <div class="form-group col-sm-6">
                                    <@form.DateTimePicker id = "feedbackConsignTime" label = "反馈发运时间" ngModel= "consign.operationTime.feedbackConsignTime" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                    <@form.DateTimePicker id = "startupTime" label = "启运时间" ngModel= "consign.operationTime.startupTime" />
                                    </div>
                                    <div class="form-group col-sm-6">
                                    <@form.DateTimePicker id = "arriveTime" label = "到达时间" ngModel= "consign.operationTime.arriveTime" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                    <@form.DateTimePicker id = "finishTime" label = "完成时间" ngModel= "consign.operationTime.finishTime" />
                                    </div>
                                    <div class="form-group col-sm-6">
                                    <@form.DateTimePicker id = "predictArriveTime" label = "预计到货时间" ngModel= "consign.operationTime.predictArriveTime"/>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6" style="color: red">
                                    <@form.ComboBox id="transferOrganizationCode" label="中转分公司" ngModel="consign.transferOrganizationCode"  listItem=organizations ngChange="getSiteList()"/>
                                    </div>
                                    <div class="form-group col-sm-6" style="color: red">
                                    <@form.ComboBox id="transferSiteCode" label="中转站点" ngModel="consign.transferSiteCode"  listItem=null options="siteOptions"/>
                                    </div>
                                </div>
                                <div class="row">
                                    <div><h4>托运单明细</h4></div>
                                </div>
                                <div class="row">
                                    <div kendo-grid k-data-source="consignItemsDataSource" k-editable="true"
                                         k-columns="itemsColumns" id="consignItems"></div>
                                </div>
                            </div>
                            <div class="tab-pane" id="consignLogInfo" >
                                <div class="row">
                                    <div kendo-grid k-data-source="consignLogDataSource" k-editable="false"
                                         k-columns="logColumns" id="consignLog" name="consignLog"></div>
                                </div>
                            </div>
                            <div class="tab-pane" id="information"  >
                                <div class="about4">
                                    <div id="isHide"><p>无物流跟踪信息。</p></div>
                                    <ul class="main_li">
                                        <li ng-repeat="item in traceInfo">
                                            <span class="time ">{{item.operateTime}}</span>
                                            <span class="decttion"></span>
                                            <div class="straight_line"></div>
                                            <div class="event_conter">
                                                地点： {{item.operateAddress}}<br/>
                                                操作内容：{{item.description}}<br/>
                                            </div>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
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
<link href="${request.contextPath}/resources/global/plugins/timeline/css/style.css" rel="stylesheet" type="text/css"/>
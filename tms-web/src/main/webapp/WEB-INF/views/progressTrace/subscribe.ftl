<#import "/WEB-INF/layouts/form.ftl" as form>
<div id="subscribe" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="subscribeForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;维护预约信息</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="marketOrderNo" label="商超单号" ngModel="marketOrderNo" required="required" pattern="[A-Za-z0-9_-]+"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="subscribeDeliveryNo" label="送货预约号" ngModel="subscribeDeliveryNo" required="required" pattern="[A-Za-z0-9_-]+"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id="subscribeTime" label="预约时间" ngModel="subscribeTime" required="required"/>
                        </div>
                    </div>
                    <div class="table-scrollable">
                        <table class="table table-striped table-hover table-bordered dataTable no-footer">
                            <thead>
                            <tr>
                                <th>序号</th>
                                <th>单号</th>
                                <th>货物编码</th>
                                <th>货物名称</th>
                                <th>货物型号</th>
                                <th>产品类型</th>
                                <th>货物数量</th>
                                <th>箱数</th>
                                <th>体积</th>
                                <th>重量</th>
                                <th>预约数量</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr ng-repeat="item in
">
                                <td>{{$index + 1}}</td>
                                <td>{{item.orderNo}}</td>
                                <td>{{item.goodsCode}}</td>
                                <td>{{item.goodsName}}</td>
                                <td>{{item.goodsNumber}}</td>
                                <td>{{item.goodsTypeCode}}</td>
                                <td>{{item.goodsQuantity}}</td>
                                <td>{{item.packageQuantity}}</td>
                                <td>{{item.volume}}</td>
                                <td>{{item.weight}}</td>
                                <td><input kendo-numeric-text-box k-min="1" k-max="{{item.goodsQuantity}}"
                                           ng-model="item.subscribeItemQuantity" k-decimals="'0'" k-format="'0'"
                                           required="required"/></td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-primary">
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
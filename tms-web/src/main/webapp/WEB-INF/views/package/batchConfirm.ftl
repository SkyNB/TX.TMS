<#import "/WEB-INF/layouts/form.ftl" as form>
<div id="batchConfirm" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="batchConfirmForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;确认体积重量</h4>
                </div>
                <div class="modal-body">

                <#--打包明细-->
                    <#--<div id="packageInfoGrid" kendo-grid k-data-source="infoDataSource" k-editable="true"-->
                         <#--k-columns="infoColumns"></div>-->
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
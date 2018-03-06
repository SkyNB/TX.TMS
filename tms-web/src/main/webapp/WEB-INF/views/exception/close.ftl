<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="exceptionClose" class="modal fade " role="basic" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="closeExceptionForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;关闭异常信息</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="goodsValue" label="货值" ngModel="exceptionClose.goodsValue" required="required" format="0.000000￥" decimals=6/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="compensationToCustomer" label="赔偿客户金额" ngModel="exceptionClose.compensationToCustomer" required="required" format="0.000000￥" decimals=6/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="insurance" label="保险赔偿金额" ngModel="exceptionClose.insurance" required="required" format="0.000000￥" decimals=6/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="damage" label="实际损失金额" ngModel="exceptionClose.damage" required="required" format="0.000000￥" decimals=6/>
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
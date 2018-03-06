<#--
<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="orderSign" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="orderSignForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-edit"></i>&nbsp;订单签收</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="signMan" label="签收人" ngModel="orderSign.signMan"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.MaskText id="signManCard" label="签收人身份证" ngModel="orderSign.signManCard" mask="000000-00000000-0000" pattern="\\d{6}-\\d{8}-\\d{4}"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.NumberBox id="signNumber" label="签收箱数" ngModel="orderSign.signNumber" required="required" format="0" min=1/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="agentSignMan" label="代理签收人" ngModel="orderSign.agentSignMan"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.MaskText id="agentSignManCard" label="代理签收人身份证" ngModel="orderSign.agentSignManCard" mask="000000-00000000-0000" pattern="\\d{6}-\\d{8}-\\d{4}"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id="signTime" label="签收时间" ngModel="orderSign.signTime" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id="feedBackSignTime" label="反馈签收时间" ngModel="orderSign.feedbackSignTime" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="remark" label="备注" ngModel="orderSign.remark"/>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" tabindex="-1" class="k-button" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                    <button type="submit" id="submit" class="k-button k-primary">
                        <i class="fa fa-check"></i>&nbsp;确定
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>-->

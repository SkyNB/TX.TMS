<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="consignOrderUpdateOrderNo" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="consignOrderUpdateOrderNoForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;托运单替换临时单号</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="temporaryConsignOrderNo" label="临时单号" ngModel="temporaryConsignOrderNo" readonly="readonly" />
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="consignOrderNo" label="正式单号" ngModel="consignOrderNo" required= "required" />
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
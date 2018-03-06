<#import "/WEB-INF/layouts/form.ftl" as form>

<div id="reject" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="createForm" class="form-horizontal" data-role="form" ng-submit="submit()">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;拒绝</h4>
                </div>
                <div class="modal-body">

                    <div class="row">
                        <div class="form-group container">
                        <@form.Textarea id="remark" rows=4 label="原因" ngModel="rejectedNotes" />
                        </div>
                    </div>

                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-primary">
                        <i class="fa fa-check"></i>&nbsp;保存
                    </button>
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
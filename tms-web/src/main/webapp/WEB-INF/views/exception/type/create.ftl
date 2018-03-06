<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="exceptionTypeCreate" class="modal fade " role="basic" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="addExceptionTypeForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;添加异常类型</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-3"></div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="code" label="编码" ngModel="exceptionType.code" required="required" pattern="[A-Za-z0-9_-]+"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-3"></div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="name" label="名称" ngModel="exceptionType.name" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-3"></div>
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="remark" label="备注" ngModel="exceptionType.remark"/>
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
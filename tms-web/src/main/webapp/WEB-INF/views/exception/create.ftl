<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="exceptionCreate" class="modal fade " role="basic" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="addExceptionForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;添加异常信息</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="ordrNo" label="订单号" ngModel="exception.orderNo" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="status" label="状态" listItem=statusList ngModel="exception.status" valueField="" required="required"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="classification" label="异常分类" listItem=classifications ngModel="exception.classification" valueField="" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Dropdown id="typeCode" label="异常类型" listItem=types ngModel="exception.typeCode" required="required"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.ComboBox id="branchCode" label="分公司" ngModel="exception.branchCode" required="required" listItem=branches ngChange="getSite()"/>
                        </div>
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="sites">站点
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-8">
                                <select kendo-combo-box ng-model="exception.siteCode" id="siteCode" name="siteCode"
                                        k-placeholder="'选择站点'"
                                        k-data-text-field="'name'"
                                        k-data-value-field="'code'"
                                        k-filter="'contains'"
                                        k-auto-bind="true"
                                        k-data-source="sitesDataSource" required="required">
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id="occurTime" label="产生时间" ngModel="exception.occurTime" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="address" label="产生地点" ngModel="exception.address" required="required"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Text id="personsResponsible" ngModel="exception.personsResponsible" label="责任人" required="required"/>
                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="remark" label="备注" ngModel="exception.remark"/>
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
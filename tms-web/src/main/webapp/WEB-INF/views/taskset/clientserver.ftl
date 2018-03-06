<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="clientTaskset" class="modal fade " role="basic" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-edit"></i>&nbsp;任务组明细</h4>
                </div>
                <div class="modal-body">
       <div class="row">
                <div class="form-group col-sm-6">
                    <label class="control-label col-sm-4" for="name">名称
                        <span class="required" aria-required="true"> * </span>
                    </label>
                    <div class="col-sm-8">
                        <input type="text" class="k-textbox" ng-model="" required="required"
                               name="name"/>
                    </div>
                </div>
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="">类型
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-6">
                                <select kendo-drop-down-list ng-model="" id="bizGroup"
                                        name="bizGroup"
                                        required="required">
                                    <option value="">请选择...</option>
                                <#list businessGroups as item>
                                    <option value="${item.code}">${item.name}</option>
                                </#list>
                                </select>
                            </div>
                        </div>

                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="">司机
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-6">
                                <select kendo-drop-down-list ng-model="" id="bizGroup"
                                        name="bizGroup"
                                        required="required">
                                    <option value="">请选择...</option>
                                <#list businessGroups as item>
                                    <option value="${item.code}">${item.name}</option>
                                </#list>
                                </select>
                            </div>
                        </div>
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4">电话
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-8">
                                <input type="text" class="k-textbox" ng-model="customer.contactMan" name="contactMan"
                                       required="required"/>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4">车牌号
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-8">
                                <input type="text" class="k-textbox" ng-model="" name="contactMan"
                                       required="required"/>
                            </div>
                        </div>
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4">创建人
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-8">
                                <input type="text" class="k-textbox" ng-model="" name="contactMan"
                                       required="required"/>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4">创建时间</label>
                            <div class="col-sm-8">
                                <input type="text" class="k-textbox" ng-model=""/>
                            </div>
                        </div>
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4">备注</label>
                            <div class="col-sm-8">
                                <textarea class="k-textbox" ng-model=""/>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="">门店
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-8">
                                <select kendo-drop-down-list ng-model="" id="bizGroup"
                                        name="bizGroup"
                                        required="required">
                                    <option value="">请选择...</option>
                                <#list businessGroups as item>
                                    <option value="${item.code}">${item.name}</option>
                                </#list>
                                </select>
                            </div>
                        </div>
                        <div class="col-sm-6">
                        <button class="btn btn-primary">添加</button>
                        </div>
                    </div>
                    <div class="row">
                        <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" id="gridCustomer""></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button id="btnConfirmCreateOrganization" type="submit" class="btn btn-primary">
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
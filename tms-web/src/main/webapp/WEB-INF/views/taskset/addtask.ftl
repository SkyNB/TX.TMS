<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="addTask" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="addTaskForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp; 增加任务组</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="name">名称
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-8">
                                <input type="text" class="k-textbox" ng-model="taskTeam.name"  required="required"
                                       name="name"/>
                            </div>
                        </div>
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="type">类型
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-6">
                                <select kendo-drop-down-list  id="taskTeamType"  ng-model="taskTeam.type"
                                        name="taskTeamType"
                                        required="required">
                                    <option value="">请选择...</option>
                                <#list taskTeamTypes as item>
                                    <option value="${item}">${item.text}</option>
                                </#list>
                                </select>
                            </div>
                        </div>

                    </div>

                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="driver">司机
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-6">
                                <input kendo-combo-box k-options="vehicleOptions" ng-model="taskTeam.driver" id="searchVehicle" name="vehicleCode"
                                       autocomplete="off"  >
                            </div>
                        </div>
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4">备注</label>
                            <div class="col-sm-8">
                                <textarea class="k-textbox" ng-model="taskTeam.remark" />
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label class="control-label col-sm-4" for="">门店
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-8">
                                <input kendo-combo-box k-options="storeOptions" id="searchStore" name="storeCode" autocomplete="off" />
                            </div>
                        </div>
                        <div class="col-sm-6">
                            <button class="btn btn-primary" ng-click="addition()">添加</button>
                        </div>
                    </div>
                </div>
            <h3>任务组明细</h3>

                <div id="example" >
                <div kendo-grid k-data-source="idleData" k-columns="gridColumns" k-editable="true"
                         style="height: 250px ;overflow: auto;" ></div>
                </div>
                <div class="modal-footer">
                    <button id="submit" type="submit" class="btn btn-primary">
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

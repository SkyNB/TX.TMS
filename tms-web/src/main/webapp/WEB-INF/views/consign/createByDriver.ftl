<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="createByDriver" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                按发运司机开单
            </div>
            <div class="modal-body">
                <div class="row">


                <div class="search-bar col-sm-6">
                    <form id="createByDriverForm" class="form-inline" ng-submit="search()">
                        <div class="form-group">
                        <@form.ComboBox listItem=vehicles ngModel="vehicleId" id="searchvehicleId" name = "vehicleId" label="司机"/>
                        </div>
                        <button kendo-button type="submit" class="btn btn-primary"><i class="fa fa-search"></i>&nbsp;搜索
                        </button>
                    </form>

                </div>
                 <div class="col-md-3">
                    <button class="btn btn-primary " ng-click="select()"  style="margin-top:10px ">
                        <i class="fa fa-plus"></i> 选择
                    </button>
                 </div>
                </div>

                <div class="dynamic-height" kendo-ex-grid k-options="createByDriverGridOptions" k-locked="false"
                     k-extend="false" k-data-bound='createByDriverDataBound' id="createByDriverGrid"></div>
            </div>

            <div class="modal-footer">
                <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                    <i class="fa fa-close"></i>&nbsp;取消
                </button>
            </div>
        </div>
    </div>
</div>

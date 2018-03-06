<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="dispatchLoading" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="dispatchLoadingForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;按单扫描装车</h4>
                </div>
                <div class="modal-body">
                    <div >
                        <label for="orderNo">单号：</label><input type = "text" id="orderNo" name="orderNo" class="k-textbox"/>
                        <button type="button" class="k-button" ng-click="scan()">确定</button>
                    </div>
                    <div class="row">
                        <div kendo-grid k-data-source="dispatchItemsDataSource" k-editable="false" k-data-bound="dataBound"
                             k-columns="itemsColumns" id="dispatchItems" k-selectable="'multiple,row'" name="dispatchItems"></div>
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
<script>
    var orderDispatchTypes = ${jsonMapper.writeValueAsString(orderDispatchTypes)};
    var orderTypes = ${jsonMapper.writeValueAsString(orderTypes)};
    var carriers = ${jsonMapper.writeValueAsString(carriers)};
</script>
<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="dispatchFinish" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="dispatchFinishForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;派车单完成</h4>
                </div>
                <div class="modal-body">
                    <div>
                        <h4><span style='color: #ff0000;font-weight: bold;'>如派车作业未完成，请去掉对应单号前的勾选，订单或指令将需要再次派车！</span>
                        </h4>
                    </div>
                    <div class="row">
                        <div kendo-ex-grid k-data-source="dispatchItemsDataSource" k-editable="false"
                             k-data-bound="dataBound"
                             k-columns="itemsColumns" id="dispatchItems" k-selectable="'multiple,row'"
                             name="dispatchItems"></div>
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
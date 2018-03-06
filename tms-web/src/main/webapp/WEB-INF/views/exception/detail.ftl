<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="exceptionDetail" class="modal fade " role="basic" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title"><i class="fa fa-edit"></i>&nbsp;异常信息</h4>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="orderNo">订单号</label>
                        <div class="col-sm-8">
                            <span id="orderNo">{{exception.orderNo}}</span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="status">状态</label>
                        <div class="col-sm-8">
                            <span id="status">{{exception.status}}</span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="classification">异常分类</label>
                        <div class="col-sm-8">
                            <span id="classification">{{exception.classification}}</span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="type">异常类型</label>
                        <div class="col-sm-8">
                            <span id="type">{{exception.typeName}}</span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="occurTime">产生时间</label>
                        <div class="col-sm-8">
                            <span id="occurTime">{{exception.occurTime}}</span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="address">产生地点</label>
                        <div class="col-sm-8">
                            <span id="address">{{exception.address}}</span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="personsResponsible">责任人</label>
                        <div class="col-sm-8">
                            <span id="personsResponsible">{{exception.personsResponsible}}</span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="processor">处理人</label>
                        <div class="col-sm-8">
                            <span id="processor">{{exception.processor}}</span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="remark">备注</label>
                        <div class="col-sm-8">
                            <span id="remark">{{exception.remark}}</span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <hr/>
                </div>

                <div class="row">
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="personsResponsible">货值</label>
                        <div class="col-sm-8">
                            <span id="personsResponsible">{{exception.goodsValue}}</span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="processor">赔偿客户金额</label>
                        <div class="col-sm-8">
                            <span id="processor">{{exception.compensationToCustomer}}</span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="personsResponsible">保险赔偿金额</label>
                        <div class="col-sm-8">
                            <span id="personsResponsible">{{exception.insurance}}</span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label class="control-label col-sm-4" for="processor">实际损失金额</label>
                        <div class="col-sm-8">
                            <span id="processor">{{exception.damage}}</span>
                        </div>
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                    <i class="fa fa-close"></i>&nbsp;取消
                </button>
            <#--<button type="submit" id="submit" class="k-button k-primary">
            <i class="fa fa-check"></i>&nbsp;确定
            </button>-->
            </div>
        </div>
    </div>
</div>
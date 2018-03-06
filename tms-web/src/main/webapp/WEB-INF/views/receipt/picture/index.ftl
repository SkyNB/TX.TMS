<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="receiptPic" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">

                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;回单图片</h4>
                </div>
                <div class="modal-body">
                    <div ng-if="receiptPic.length > 0">
                        <div class="row about4">
                            <ul class="picture">
                                <li ng-repeat="item in receiptPic">
                                    <div>
                                        <a target="_blank" href="http://filex.lnetco.com/{{item.filePath}}"><img class="img-thumbnail"
                                                                                         src="http://filex.lnetco.com/{{item.thumbPath}}" width="200" height="200" alt="无图片信息"/> </a>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div ng-if="receiptPic.length == 0">
                        <div class="row">
                            <div align="center"><h4>还未上传回单</h4></div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" tabindex="-1" class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
        </div>
    </div>
</div>
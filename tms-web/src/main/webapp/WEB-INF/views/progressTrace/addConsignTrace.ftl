<#import "/WEB-INF/layouts/form.ftl" as form/>
<div id="addConsignTrace" class="modal fade in" role="basic" aria-hidden="false">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form id="addConsignForm" class="form-horizontal" ng-submit="submit()" data-role="form">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 class="modal-title"><i class="fa fa-plus"></i>&nbsp;添加托运单跟踪信息</h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.DateTimePicker id="dateinfo" label="操作时间" ngModel="consignTrace.operateTime" required="required"/>

                        </div>
                        <div class="form-group col-sm-6">
                        <@form.Text id="operateAddress" label="操作地点" ngModel="consignTrace.operateAddress" required="required"/>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                        <@form.Textarea id="description" label="描述" ngModel="consignTrace.description" required="required"/>
                        </div>
                    </div>
                <#--timeline-->
                    <div class="about4">
                        <div id="isHide"><p>无物流跟踪信息。</p></div>
                        <ul class="main_li">
                            <li ng-repeat="item in traceInfo">
                                <span class="time ">{{item.operateTime}}</span>
                                <span class="decttion"></span>
                                <div class="straight_line"></div>
                                <div class="event_conter">
                                   <span> 地点:</span> {{item.operateAddress}}<br/>
                                    <span>  操作内容:</span>{{item.description}}<br/>
                                </div>
                            </li>
                        </ul>

                    </div>
                <#--timeline-->
                </div>


            <#--  <div cla="row">
                     <div class="col-sm-12">
                         <div class="portlet light portlet-fit bg-inverse ">
                             <div class="portlet-title">
                                 <div class="caption">
                                     <i class="icon-microphone font-white"></i>
                                     <span class="caption-subject bold font-green uppercase"> 托运单物流信息 </span>
                                 </div>
                             </div>
                             <div class="portlet-body">
                                 <div class="timeline  white-bg ">
                                     <div class="timeline-item" ng-repeat="item in traceInfo">
                                         <div class="timeline-badge"><img class="timeline-badge-userpic" src="${request.contextPath}/resources/layouts/user.png">
                                         </div>
                                         <div class="timeline-body">
                                             <div class="timeline-body-arrow"></div>
                                             <div class="timeline-body-head font-grey-cascade">记录员：{{item.operator}}
                                             </div>
                                             <div class="timeline-body-content">
                                                 <span class="font-grey-cascade">
                                                     时间：{{item.operateTime}}<br/>
                                                     地点： {{item.operateAddress}}<br/>
                                                     操作内容：{{item.description}}<br/>
                                                 </span>
                                             </div>
                                         </div>
                                     </div>

                                 </div>
                             </div>
                         </div>
                     </div>
                 </div>-->

                <div class="modal-footer">

                    <button type="submit" id="submit" class="btn btn-primary">
                        <i class="fa fa-check"></i>&nbsp;确定
                    </button>
                    <button type="button" tabindex="-1"  class="btn btn-danger" data-dismiss="modal">
                        <i class="fa fa-close"></i>&nbsp;取消
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>


<link href="${request.contextPath}/resources/global/plugins/timeline/css/style.css" rel="stylesheet" type="text/css"/>


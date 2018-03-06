<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>

<script type="text/javascript" src="http://api.map.baidu.com/api?v=1.2"></script>
<script type="javascript">

</script>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="GpsController">

    <div class="row">
        <div class="col-md-7" style="padding: 10px;">
            <div style="width:100%;height: 700px;border: 1px solid #ddd" id="container"></div>
        </div>
        <div class="col-md-5" style="padding: 10px;">
            <div class="row">
                <div class="form-group col-md-6">
                    <label class="control-label col-md-3">已打包:</label>
                    <div class=" col-md-9">
                        <input class="form-control" ng-model="count.todayPackage" type="text">
                    </div>
                </div>
                <div class="form-group col-md-6">
                    <label class="control-label col-md-3">待打包:</label>
                    <div class="col-md-9">
                        <input class="form-control" ng-model="count.todayOrder" type="text">
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-md-6">
                    <label class="control-label col-md-3">已派车:</label>
                    <div class="col-md-9">
                        <input class="form-control" ng-model="count.todayDispatch" type="text">
                    </div>
                </div>
                <div class="form-group col-md-6">
                    <label class="control-label col-md-3">待派车:</label>
                    <div class="col-md-9">
                        <input class="form-control" ng-model="count.needDispatch" type="text">
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-md-6">
                    <label class="control-label col-md-3">已发运:</label>
                    <div class="col-md-9">
                        <input class="form-control" ng-model="count.todayConsign" type="text">
                    </div>
                </div>
                <div class="form-group col-md-6">
                    <label class="control-label col-md-3">待发运:</label>
                    <div class="col-md-9">
                        <input class="form-control" ng-model="count.needConsign" type="text">
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="headgrid">空闲车辆</div>
                <div kendo-grid k-data-source="idleData" k-columns="gridColumns" k-selectable="true"  style="height: 250px ;overflow: auto;"></div>


                <div class="headgrid">作业车辆</div>
                <div kendo-grid k-data-source="busyData" k-columns="gridColumns" k-selectable="true" style="height: 250px ;overflow: auto;"></div>
            </div>
        </div>


    </div>


</div>
</@layout.master>
<#--<link href="${request.contextPath}/resources/global/layouts/stie.css" rel="stylesheet" type="text/css"/>-->
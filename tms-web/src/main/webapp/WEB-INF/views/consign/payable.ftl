<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="ConsignController">
    <div class="search-bar">
        <form id="fromSearchConsign" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <@form.ComboBox listItem=carriers ngModel="carrier" id="searchCarrierCode" name = "carrierCode" label="承运商"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchConsignOrderNo" name = "consignOrderNo" label= "托运单号"/>
            </div>
            <div class="form-group">
                <@form.Dropdown id = "searchStatus" name = "status" listItem = statusList label= "状态" valueField="" placeholder="-------请选择-------"/>
            </div>
            <div class="form-group">
                <@form.Text id = "searchConsignee" name = "consignee" label= "收货人"/>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="gridConsign"></div>
</div>
<script>
    var transportTypes = ${jsonMapper.writeValueAsString(transportTypes)};
</script>
</@layout.master>
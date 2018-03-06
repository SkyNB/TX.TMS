<#import "/WEB-INF/layouts/master.ftl"  as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<@layout.master bodyEnd=bodyEnd>
<div ui-view="content" ng-controller="ExceptionController">
    <div class="action-bar">
        <a href="#/create" ng-click="create();" kendo-button>
            <i class="fa fa-plus"></i> 增加 </a>
        <a href="#/close" ng-click="close();" kendo-button>
            <i class="fa fa-close"></i> 关闭 </a>
    </div>
    <div class="search-bar">
        <form id="fromSearchException" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <label class="control-label col-sm-3">编码</label>
                <div class="col-sm-9">
                    <input type="text" class="k-textbox" name="code" autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3">订单号</label>
                <div class="col-sm-9">
                    <input type="text" class="k-textbox" name="orderNo" autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3">类型</label>
                <div class="col-sm-9">
                    <select kendo-drop-down-list name="typeCode">
                        <option value="">请选择...</option>
                        <#list types as item>
                            <option value="${item.value}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3">状态</label>
                <div class="col-sm-9">
                    <select kendo-drop-down-list name="status">
                        <option value="">请选择...</option>
                        <#list statusList as item>
                            <option value="${item}">${item.text}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="gridException"></div>
</div>

</@layout.master>

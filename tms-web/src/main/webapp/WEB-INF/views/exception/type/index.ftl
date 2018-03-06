<#import "/WEB-INF/layouts/master.ftl"  as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<@layout.master>
<div ui-view="content" ng-controller="ExceptionTypeController">
    <div class="action-bar">
        <a href="#/create" ng-click="create();" kendo-button>
            <i class="fa fa-plus"></i> 增加 </a>
    </div>
    <div class="search-bar">
        <form id="formSearchExceptionType" class="form-inline" ng-submit="search()">
            <div class="form-group">
                <label class="control-label col-sm-3">编码</label>
                <div class="col-sm-9">
                    <input type="text" class="k-textbox" name="code" autocomplete="off">
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-3">名称</label>
                <div class="col-sm-9">
                    <input type="text" class="k-textbox" name="name" autocomplete="off">
                </div>
            </div>

            <div class="form-group">
                <button kendo-button type="submit" class="k-button"><i class="fa fa-search"></i>&nbsp;搜索</button>
            </div>
        </form>
    </div>
    <div class="dynamic-height" kendo-ex-grid k-options="gridOptions" k-data-bound='dataBound' id="gridExceptionType"></div>
</div>

</@layout.master>

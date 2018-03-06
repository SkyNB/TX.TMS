<#macro master title="" headEnd="" bodyEnd="">
<!DOCTYPE html>

<!--[if IE 8]> <html lang="en" class="ie8 no-js"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9 no-js"> <![endif]-->
<!--[if !IE]><!-->
<html lang="en">
<!--<![endif]-->
<head>
    <meta charset="utf-8"/>
    <title>新易泰物流应用平台 | ${title}</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport"/>
    <link href="${request.contextPath}/resources/global/plugins/bootstrap/css/bootstrap.min.css" rel="stylesheet"
          type="text/css"/>
    <link href="${request.contextPath}/resources/global/kendoui/styles/kendo.common.min.css" rel="stylesheet">
    <link href="${request.contextPath}/resources/global/kendoui/styles/kendo.custom.css" rel="stylesheet">
    <link href="${request.contextPath}/resources/global/kendoui/styles/toastr/toastr.css" rel="stylesheet">
    <link href="${request.contextPath}/resources/global/plugins/fontawesome/css/font-awesome.min.css" rel="stylesheet">
    <link href="${request.contextPath}/resources/global/plugins/typeahead/css/typeahead.css" rel="stylesheet">
    <link href="${request.contextPath}/resources/layouts/layout.css" rel="stylesheet" type="text/css"/>
<#--<link href="${request.contextPath}/resources/layouts/blue.css" rel="stylesheet" type="text/css"/>-->
    <link href="${request.contextPath}/resources/layouts/site.css" rel="stylesheet">
    <link rel="shortcut icon" href="${request.contextPath}/resources/global/favicon.ico"/>
    <#if headEnd?has_content> ${headEnd}</#if>
</head>
<body>

<div class="page-content" data-ng-app="RoadnetApp">
    <div class="refreshContent">
        <#nested />
    </div>
    <div ui-view></div>
</div>
<!--[if lt IE 9]>
<script src="${request.contextPath}/resources/global/plugins/respond.min.js"></script>
<script src="${request.contextPath}/resources/global/plugins/excanvas.min.js"></script>
<![endif]-->
<script src="http://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/jquery.validate.min.js"></script>

<script src="http://cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<script src="${request.contextPath}/resources/global/plugins/bootstrap/js/bootstrap-confirmation.js"></script>
<script src="${request.contextPath}/resources/global/plugins/bootstrap/js/bootstrap-wizard.min.js"></script>

<script src="http://cdn.bootcss.com/angular.js/1.4.6/angular.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/angular-ui-router.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/ocLazyLoad.min.js"></script>

<script src="${request.contextPath}/resources/global/kendoui/js/kendo.all.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/jszip.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/toastr.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/kendo.extensions.js"></script>

<script src="${request.contextPath}/resources/global/kendoui/js/messages/kendo.messages.zh-CN.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/cultures/kendo.culture.zh-CN.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/messages/jquery.validate.messages_zh.min.js"></script>

<script src="${request.contextPath}/resources/global/plugins/typeahead/js/typeahead.bundle.min.js"></script>
<script src="${request.contextPath}/resources/global/kendoui/js/kendo.districtSelect.js"></script>
<script src="${request.contextPath}/resources/global/scripts/kendo.common.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/scripts/js.cookie.min.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/scripts/app.js" type="text/javascript"></script>
    <#assign pageScriptUrl = .main_template_name?keep_after_last("WEB-INF/views/")?keep_before_last(".") />
<script src="${request.contextPath}/resources/pages/${pageScriptUrl}.js"></script>
    <#if bodyEnd?has_content>${bodyEnd}</#if>
<script>
    var contextPath = "${request.contextPath}";
</script>
</body>
</html>
</#macro>
<#import "/WEB-INF/layouts/master.ftl" as layout/>
<#import "/WEB-INF/layouts/form.ftl" as form/>

<#assign bodyEnd>
<link href="${request.contextPath}/resources/global/plugins/fileupload/css/jquery.fancybox.css" rel="stylesheet" type="text/css"/>
<link href="${request.contextPath}/resources/global/plugins/fileupload/css/blueimp-gallery.min.css" rel="stylesheet" type="text/css"/>
<link href="${request.contextPath}/resources/global/plugins/fileupload/css/jquery.fileupload.css" rel="stylesheet" type="text/css"/>
<link href="${request.contextPath}/resources/global/plugins/fileupload/css/jquery.fileupload-ui.css" rel="stylesheet" type="text/css"/>
<link href="${request.contextPath}/resources/global/plugins/fileupload/css/components.min.css" rel="stylesheet" type="text/css"/>

<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fancybox.pack.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.ui.widget.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/tmpl.min.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/load-image.min.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/canvas-to-blob.min.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.blueimp-gallery.min.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.iframe-transport.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fileupload.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fileupload-process.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fileupload-image.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fileupload-audio.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fileupload-video.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fileupload-validate.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/jquery.fileupload-ui.js" type="text/javascript"></script>
<script src="${request.contextPath}/resources/global/plugins/fileupload/js/form-fileupload.min.js" type="text/javascript"></script>
</#assign>

<@layout.master bodyEnd=bodyEnd>
<div class="row">
    <div class="col-md-12">
        <div class="portlet light portlet-fit bordered">
            <#--<div class="portlet-title">
                <div class="caption">
                    <i class="fa fa-file"></i>
                    <span class="caption-subject uppercase" id="msg">回单上传</span>
                </div>
            </div>-->
            <div class="portlet-body">
                <form class="form-horizontal" data-role="form" id="fileupload" method="POST" enctype="multipart/form-data">
                    <div class="row">
                        <div class="form-group col-sm-7">
                            <label class="control-label col-sm-1" for="customerCode">客户
                                <span class="required" aria-required="true"> * </span>
                            </label>
                            <div class="col-sm-11">
                                <select kendo-drop-down-list id="customerCode" name="customerCode" required="required">
                                    <option value="">请选择...</option>
                                    <#list customers as item>
                                        <option value="${item.value}">${item.text}</option>
                                    </#list>
                                </select>
                            </div>
                        </div>
                    </div>

                    <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
                    <div class="row fileupload-buttonbar">
                        <div class="col-sm-7">
                            <!-- The fileinput-button span is used to style the file input field as button -->

                                <span class="btn green fileinput-button">
                                    <i class="fa fa-plus"></i>
                                    <span> 选择文件... </span>
                                    <input type="file" name="files[]" multiple="" accept="image/*">
                                </span>

                            <button type="submit" class="btn blue start">
                                <i class="fa fa-upload"></i>
                                <span> 上传文件 </span>
                            </button>
                            <button type="reset" class="btn grey-mint cancel">
                                <i class="fa fa-close"></i>
                                <span> 清除屏幕 </span>
                            </button>
                            <#--<button type="button" class="btn red delete">
                                <i class="fa fa-trash"></i>
                                <span> 删除文件 </span>
                            </button>
                            <input type="checkbox" class="toggle">-->
                            <!-- The global file processing state -->
                            <span class="fileupload-process"> </span>
                        </div>
                        <!-- The global progress information -->
                        <div class="col-sm-5 fileupload-progress fade">
                            <!-- The global progress bar -->
                            <div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                                <div class="progress-bar progress-bar-success" style="width:0%;"></div>
                            </div>
                            <!-- The extended global progress information -->
                            <div class="progress-extended"> &nbsp; </div>
                        </div>
                    </div>

                    <!-- The table listing the files available for upload/download -->
                    <table role="presentation" class="table table-striped clearfix">
                        <tbody class="files"></tbody>
                    </table>
                </form>
            </div>
        </div>

    </div>
</div>
<!-- The blueimp Gallery widget -->
<div id="blueimp-gallery" class="blueimp-gallery blueimp-gallery-controls" data-filter=":even">
    <div class="slides"></div>
    <h3 class="title"></h3>
    <a class="prev"> ‹ </a>
    <a class="next"> › </a>
    <a class="close white"> </a>
    <a class="play-pause"> </a>
    <ol class="indicator"></ol>
</div>
<!-- BEGIN JAVASCRIPTS(Load javascripts at bottom, this will reduce page load time) -->
<script id="template-upload" type="text/x-tmpl"> {% for (var i=0, file; file=o.files[i]; i++) { %}
                        <tr class="template-upload fade">
                            <td>
                                <span class="preview"></span>

                            </td>
                            <td>
                                <p class="name">{%=file.name%}</p>
                                <strong class="error text-danger label label-danger"></strong>
                                <p class="size">Processing...</p>
                                <div class="progress progress-striped active" style="background-color:#FFFFFF;" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">
                                    <div class="progress-bar progress-bar-success" style="width:0%;"></div>
                                </div>

                            </td>
                            <td> {% if (!i && !o.options.autoUpload) { %}
                                <button class="btn blue start" disabled>
                                    <i class="fa fa-upload"></i>
                                    <span>开始</span>
                                </button> {% } %} {% if (!i) { %}
                                <button class="btn red cancel">
                                    <i class="fa fa-ban"></i>
                                    <span>取消</span>
                                </button> {% } %} </td>
                        </tr> {% } %}

    </script>
<!-- The template to display files available for download -->
<script id="template-download" type="text/x-tmpl"> {% for (var i=0, file; file=o.files[i]; i++) { %}
                        <tr class="template-download fade">
                            <td>
                                <span class="preview"> {% if (file.thumbUrl) { %}
                                    <#--<a href="${request.contextPath}/order/originalImg/{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" data-gallery>
                                        <img src="${request.contextPath}/receipt/thumbnailImg/{%=file.thumbnailUrl%}">
                                    </a>-->
                                     <a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" data-gallery>
                                        <img src="{%=file.thumbUrl%}">
                                    </a>
                                    {% } %} </span>
                            </td>
                            <td>
                                <p class="name"> {% if (file.url) { %}
                                    <#--<a href="${request.contextPath}/order/originalImg/{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" {%=file.thumbnailUrl? 'data-gallery': ''%}>{%=file.name%}</a>-->
                                    <a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" {%=file.thumbUrl? 'data-gallery': ''%}>{%=file.name%}</a>{% } else { %}
                                    <span>{%=file.name%}</span> {% } %} </p> {% if (file.error) { %}
                                <div>
                                    <span class="label label-danger">Error</span> {%=file.error%}</div> {% } %} </td>
                            <td>
                                <span class="size">{%=o.formatFileSize(file.size)%}</span>
                            </td>
                            <td> {% if (file.deleteUrl) { %}
                                <button class="btn red delete btn-sm" data-type="{%=file.deleteType%}" data-url="{%=file.deleteUrl%}" {% if (file.deleteWithCredentials) { %} data-xhr-fields='{"withCredentials":true}' {% } %}>
                                    <i class="fa fa-trash-o"></i>
                                    <span>删除</span>
                                </button>
                                <input type="checkbox" name="delete" value="1" class="toggle"> {% } else { %}
                                <button class="btn yellow cancel btn-sm">
                                    <i class="fa fa-ban"></i>
                                    <span>清除</span>
                                </button> {% } %} </td>
                        </tr> {% } %}

    </script>
</@layout.master>
var App = function () {
    var handleBootstrapConfirmation = function () {
        if (!$().confirmation) {
            return;
        }
        $('[data-toggle=confirmation]').confirmation({
            container: 'body', btnOkClass: 'btn btn-sm btn-success', btnCancelClass: 'btn btn-sm btn-danger'
        });
    }

    // Handles Bootstrap Tabs.
    var handleTabs = function () {
        //activate tab if tab id provided in the URL
        if (location.hash) {
            var tabid = encodeURI(location.hash.substr(1));
            $('a[href="#' + tabid + '"]').parents('.tab-pane:hidden').each(function () {
                var tabid = $(this).attr("id");
                $('a[href="#' + tabid + '"]').click();
            });
            $('a[href="#' + tabid + '"]').click();
        }

        if ($().tabdrop) {
            $('.tabbable-tabdrop .nav-pills, .tabbable-tabdrop .nav-tabs').tabdrop({
                text: '<i class="fa fa-ellipsis-v"></i>&nbsp;<i class="fa fa-angle-down"></i>'
            });
        }
    };
    var defaultValidate = function () {
        jQuery.validator.setDefaults({
            errorElement: 'span',
            errorClass: 'error',
            focusInvalid: false,
            ignore: "",
            invalidHandler: function (event, validator) {
                //success.hide();
                //error.show();
                //App.scrollTo(error, -200);
            },

            errorPlacement: function (error, element) {
                if ($(element).closest('.k-widget').size() > 0) {
                    error.insertAfter($(element).closest('.k-widget'));
                } else {
                    error.appendTo($(element).parent());
                }

            },

            highlight: function (element) {
                $(element).closest('.form-group').addClass('has-error');
            },

            unhighlight: function (element) {
                $(element).closest('.form-group').removeClass('has-error');
            },

            success: function (label, element) {
                label.closest('.form-group').removeClass('has-error');
            },

            submitHandler: function (form) {
                //success.show();
                //error.hide();
                //form[0].submit();
            }
        });

        jQuery.validator.addMethod("pattern", function (value, element, params) {
            var match;
            if (this.optional(element)) {
                return true;
            }
            match = new RegExp(params).exec(value);
            return (match && (match.index === 0) && (match[0].length === value.length));
        }, "必须是数字字母");

        jQuery.validator.addMethod("compareDate", function (value, element, params) {
            var startDate = jQuery(params).val();
            return startDate <= value;
        }, "结束日期必须大于开始日期");
    };
    var resizeGrid = function () {
        var gridElement = $(".dynamic-height:visible"),
            pageHeader = $(".page-header"),
            pageFooter = $(".page-footer"),
            pageBar = $(".page-bar"),
            dataArea = gridElement.find(".k-grid-content"),
            otherGridElements = gridElement.children().not(".k-grid-content,.k-grid-content-locked"),
            otherElementsHeight = 0;
        otherElementsHeight = $(".action-bar:visible").outerHeight() + $(".search-bar:visible").outerHeight() + $(".nav.nav-tabs").outerHeight();
        otherGridElements.each(function () {
            otherElementsHeight += $(this).innerHeight();
        });
        var height = $(window).innerHeight() - pageHeader.innerHeight() - pageBar.innerHeight() - pageFooter.innerHeight() - otherElementsHeight - 23;
        var minHeight = 200;
        height = height > minHeight ? height : minHeight;
        dataArea.height(height);
        gridElement.find(".k-grid-content-locked").height(height);

        $("[data-toggle='popover']").popover({
            trigger: "manual",
            html: true,
            container: "div.page-content",
            content: function () {
                return $(this).siblings(".data-popover-content").html();
            }
        }).on("mouseenter", function () {
            var _this = this;
            $(this).popover("show");
            $(".popover").on("mouseleave", function () {
                $(_this).popover('hide');
            });
        }).on("mouseleave", function () {
            var _this = this;
            setTimeout(function () {
                if (!$(".popover:hover").length) {
                    $(_this).popover("hide")
                }
            }, 100);
        });
        ;
    };
    // handle group element heights
    var handleHeight = function () {
        $('[data-auto-height]').each(function () {
            var parent = $(this);
            var items = $('[data-height]', parent);
            var height = 0;
            var mode = parent.attr('data-mode');
            var offset = parseInt(parent.attr('data-offset') ? parent.attr('data-offset') : 0);

            items.each(function () {
                if ($(this).attr('data-height') == "height") {
                    $(this).css('height', '');
                } else {
                    $(this).css('min-height', '');
                }

                var height_ = (mode == 'base-height' ? $(this).outerHeight() : $(this).outerHeight(true));
                if (height_ > height) {
                    height = height_;
                }
            });

            height = height + offset;

            items.each(function () {
                if ($(this).attr('data-height') == "height") {
                    $(this).css('height', height);
                } else {
                    $(this).css('min-height', height);
                }
            });

            if (parent.attr('data-related')) {
                $(parent.attr('data-related')).css('height', parent.height());
            }
        });
    };

    return {

        //main function to initiate the theme
        init: function () {
            handleTabs(); // handle tabs
            handleBootstrapConfirmation(); // handle bootstrap confirmations
            defaultValidate();
        }, resizeGrid: function () {
            resizeGrid();
        },
        scrollTo: function (el, offeset) {
            var pos = (el && el.size() > 0) ? el.offset().top : 0;

            if (el) {
                if ($('body').hasClass('page-header-fixed')) {
                    pos = pos - $('.page-header').height();
                } else if ($('body').hasClass('page-header-top-fixed')) {
                    pos = pos - $('.page-header-top').height();
                } else if ($('body').hasClass('page-header-menu-fixed')) {
                    pos = pos - $('.page-header-menu').height();
                }
                pos = pos + (offeset ? offeset : -1 * el.height());
            }

            $('html,body').animate({
                scrollTop: pos
            }, 'slow');
        },

        initSlimScroll: function (el) {
            $(el).each(function () {
                if ($(this).attr("data-initialized")) {
                    return; // exit
                }

                var height;

                if ($(this).attr("data-height")) {
                    height = $(this).attr("data-height");
                } else {
                    height = $(this).css('height');
                }

                $(this).slimScroll({
                    allowPageScroll: true, // allow page scroll when the element scroll is ended
                    size: '7px',
                    color: ($(this).attr("data-handle-color") ? $(this).attr("data-handle-color") : '#bbb'),
                    wrapperClass: ($(this).attr("data-wrapper-class") ? $(this).attr("data-wrapper-class") : 'slimScrollDiv'),
                    railColor: ($(this).attr("data-rail-color") ? $(this).attr("data-rail-color") : '#eaeaea'),
                    position: isRTL ? 'left' : 'right',
                    height: height,
                    alwaysVisible: ($(this).attr("data-always-visible") == "1" ? true : false),
                    railVisible: ($(this).attr("data-rail-visible") == "1" ? true : false),
                    disableFadeOut: true
                });

                $(this).attr("data-initialized", "1");
            });
        },
        scrollTop: function () {
            App.scrollTo();
        },
        getActualVal: function (el) {
            el = $(el);
            if (el.val() === el.attr("placeholder")) {
                return "";
            }
            return el.val();
        },

        getURLParameter: function (paramName) {
            var searchString = window.location.search.substring(1), i, val, params = searchString.split("&");

            for (i = 0; i < params.length; i++) {
                val = params[i].split("=");
                if (val[0] == paramName) {
                    return unescape(val[1]);
                }
            }
            return null;
        },

        getUniqueID: function (prefix) {
            return prefix + Math.floor(Math.random() * (new Date()).getTime());
        },
        alert: function (msg, type) {
            var options = {
                reset: true, focus: true
            };
            switch (type) {
                case "success":
                    options.type = "success";
                    options.icon = "check";
                    break;
                case "warning":
                    options.type = "warning";
                    options.icon = "warning";
                    break;
                case "error":
                    options.type = "danger";
                    options.icon = "danger";
                    break;
                default:
                    options.type = "info";
            }
            ;
            var alert = $('<div class="custom-alerts alert alert-' + options.type + ' fade in">');
            alert.append($('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">' + '</button><i class="fa-lg fa fa-' + options.icon + '"></i>'));
            alert.append(msg);

            if (options.reset) {
                $('.custom-alerts').remove();
            }
            $('.page-bar').after(alert);
            setTimeout(function () {
                alert.remove();
            }, 5000);
        }, confirm: function (title, message) {
            var modalElement = $('<div class="modal fade" role="basic" aria-hidden="true"></div>')
                .append($('<div class="modal-dialog"/>').append($('<div class="modal-content"/>')).append($('<div class="modal-body"/>')))
                .appendTo(document.body);
            var header = '<div class="modal-header"><span class="font-green"><i class="fa fa-check"></i>&nbsp;' + title + '</span></div>';
            var body = '<div class="modal-body"><div class="row"><div class="col-md-offset-1"><span>' + message + '</span></div></div></div>';
            var btnCancel = $('<button type="button" tabindex="-1" class="k-button" data-dismiss="modal">').append('<i class="fa fa-close"></i>&nbsp;取消');
            var btnOK = $('<button type="button" tabindex="-1" class="k-primary k-button" data-dismiss="modal">').append('<i class="fa fa-check"></i>&nbsp;确定');
            var footer = $('<div class="modal-footer">').append(btnCancel, btnOK);
            modalElement.find("div.modal-content").append(header).append(body).append(footer);
            modalElement.on('hidden.bs.modal', function () {
                modalElement.remove();
            });
            modalElement.modal("show");
            return {
                on: function (callback) {
                    if (callback && callback instanceof Function) {
                        btnOK.click(function () {
                            callback(true)
                        });
                        btnCancel.click(function () {
                            callback(false)
                        });
                    }
                }
            };
        }, toastr: function (mgs, type) {
            switch (type) {
                case "success":
                    toastr.success(mgs);
                    break;
                case "warning":
                    toastr.warning(mgs);
                    break;
                case "error":
                    toastr.error(mgs);
                    break;
                default:
                    toastr.info(mgs);
            }
            ;
        }, modal: function (urlOrEle) {
            if (urlOrEle.indexOf("/") >= 0) {//url
                var modalElement = $('<div class="modal fade" role="basic" aria-hidden="true"></div>')
                    .append($('<div class="modal-dialog modal-lg"/>').append($('<div class="modal-content"/>')))
                    .appendTo(document.body);
                modalElement.modal({remote: urlOrEle});
                modalElement.on('hidden.bs.modal', function () {
                    modalElement.remove();
                });
            } else {
                $(urlOrEle).modal("show");
            }
        }
    };

}();

var RoadnetApp = angular.module("RoadnetApp", ["ui.router", "oc.lazyLoad", "kendo.directives"]);
RoadnetApp.directive('ngConfirmClick', [function () {
    return {
        priority: 1,//优先级
        terminal: false,//是否终止其他绑定
        link: function (scope, element, attr) {
            var msg = attr.ngConfirmMessage || "是否确定?";
            var clickConfirm = attr.ngConfirmClick;
            var clickCancel = attr.ngCancleClick;
            element.confirmation({
                title: msg, popout: true, btnOkLabel: "确定", btnCancelLabel: "取消", onConfirm: function () {
                    scope.$eval(clickConfirm);
                }, onCancel: function () {
                    scope.$eval(clickCancel);
                }
            })
        }
    };
}]);
RoadnetApp.directive('kendoComboBox', [function () {
    return {
        priority: 2,//优先级
        terminal: false,//是否终止其他绑定
        link: function (scope, element, attr) {
            var that = $(element).data("kendoComboBox");
            $(element).siblings("span").children("input.k-input").keyup(function (event) {
                that.search($(this).val());
            });
        }
    };
}]);
RoadnetApp.directive('ngName', [function () {
    return {
        priority: 1,//优先级
        terminal: false,//是否终止其他绑定
        link: function (scope, element, attr) {
            var $name = $(element).siblings("span.k-state-default").children(".k-input");
            $(element).change(function () {
                var text = $name.val() || $name.text();
                scope.$eval(attr.ngName + "=" + "'" + text + "'");
            })
        }
    };
}]);
function getDeps(files) {
    return {
        deps: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
                name: 'RoadnetApp', insertBefore: '#ng_load_plugins_before', files: files
            });
        }]
    }
}
function orderPopover(dataItem) {
    var content = "<div class='data-popover-content'>" +
        "<p>单号：" + dataItem.orderNo + "</p>" +
        "<p>订单日期：" + dataItem.orderDate + "</p>" +
        "<p>运输方式：" + dataItem.transportType.text + "</p>" +
        "<p>状态：" + dataItem.status.text + "</p>" +
        "<p>箱数：" + dataItem.totalPackageQty + "(箱)  数量：" + dataItem.totalItemQty + "(个)</p>" +
        "<p>体积：" + dataItem.totalVolume + "(方)  重量：" + dataItem.totalWeight + "(千克)</p>";
    if (dataItem.deliveryCode) {
        content += "<p>送货门店：" + (dataItem.deliveryCode || "") + "（" + dataItem.deliveryName || "" + "）</p>";
    }
    content += "<p>收货人：" + dataItem.deliveryContacts + "   " + dataItem.deliveryContactPhone + "</p>" +
        "<p>收货地址：" + (dataItem.deliveryProvince || "") + (dataItem.deliveryCity || "") + (dataItem.deliveryDistrict || "") + (dataItem.deliveryStreet || "") + dataItem.deliveryAddress + "</p>" +
        "<a href='#/update/" + dataItem.orderId + "' data-target='#updateOrder' data-toggle='modal'>详情</a></div>";
    return content;
}

Date.prototype.format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours() % 12 == 0 ? 12 : this.getHours() % 12, //小时
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    var week = {
        "0": "/u65e5",
        "1": "/u4e00",
        "2": "/u4e8c",
        "3": "/u4e09",
        "4": "/u56db",
        "5": "/u4e94",
        "6": "/u516d"
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    if (/(E+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, ((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "/u661f/u671f" : "/u5468") : "") + week[this.getDay() + ""]);
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
};

jQuery(document).ready(function () {
    App.init(); // init metronic core componets
});

$(window).on("resize", function () {
    App.resizeGrid();
});

'use strict';
var app = angular.module('RoadnetApp');

app.controller('OrderProgressSearchController', function ($scope) {

    $scope.textFocus = function () {
        $("#customerOrderNo").focus();
    }
    $scope.textFocus();

    $(document).keydown(function (event) {
        if (event.keyCode == 13) {
            $scope.findOrder();
            event.preventDefault();
        }

    });

    $scope.mainGridDataSource = new kendo.data.DataSource({
        data: []
    });

    $scope.findOrder = function () {

        //清空数据源
        $scope.mainGridDataSource.data([]);
        $("#orderGrid").data("kendoGrid").setDataSource($scope.mainGridDataSource);
        var data = $scope.mainGridDataSource.data();

        var customerOrderNo = $("#customerOrderNo").val();
        var customerCode = $("#customerCode").val();

        if (!customerCode) {
            App.toastr("请选择客户！");
            $scope.textFocus();
            return;
        }

        if (!customerOrderNo) {
            App.toastr("请输入客户单号！");
            $scope.textFocus();
            return;
        }

        $.ajax({
            type: "GET",
            url: contextPath + "/order/getByCustomerOrderNo/" + customerCode + "/" + customerOrderNo
        }).done(function (result) {
            if (result.body) {
                data.splice(0, 0, result.body);
                $scope.findOrderProgressInfo(result.body.orderNo);
                $("#isHide").hide();
            } else {
                App.toastr("订单不属于当前客户！");
            }
        }).fail(function () {
            App.toastr("提交失败！");
        });
    }

    $scope.traceInfo = [];
    $scope.findOrderProgressInfo = function (orderNo) {
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: contextPath + "/progressTrace/orderTraceQuery/" + orderNo
        }).done(function (result) {
            if (result.success) {
                if (result.body) {
                    $scope.traceInfo = result.body.items;
                    $scope.$apply();
                } else {
                    $scope.traceInfo = [];
                    $scope.$apply();
                }
            } else {
                $scope.traceInfo = [];
                $scope.$apply();
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败", "error");
        });
    }

    $scope.clear = function () {
        $scope.mainGridDataSource.data([]);
        $("#orderGrid").data("kendoGrid").setDataSource($scope.mainGridDataSource);
        $scope.textFocus();
    };

    $scope.mainGridOptions = {
        dataSource: {
            data: $scope.mainGridDataSource.data(),
            schema: {
                model: {
                    id: "logisticsOrderId",
                    fields: {
                        orderNo: {type: "string", editable: false,},
                        customerOrderNo: {editable: false,}
                    }
                }
            }
        },
        dataBound: function () {
            this.expandRow(this.tbody.find("tr.k-master-row").first());
        },
        editable: true,
        columns: [{
            field: "orderNo", title: "单号", width: 135/*,
             template: function (dataItem) {
             var content = "<div class='data-popover-content'>" +
             "<p>单号：" + dataItem.orderNo + "</p>" +
             "<p>订单日期：" + dataItem.orderDate + "</p>" +
             "<p>运输方式：" + dataItem.transportType.text + "</p>" +
             "<p>状态：" + dataItem.status.text + "</p>" +
             "<a href='#/update/" + dataItem.orderId + "' data-target='#updateOrder' data-toggle='modal'>详情</a></div>";
             return "<a href='#' data-toggle='popover'>" + dataItem.orderNo + "</a> " + content;
             }*/
        }, {
            field: "customerOrderNo", title: "客户单号"
        }, {
            field: "customerName", title: "客户"
        }, {
            field: "deliveryContacts", title: "收货人"
        }, {
            field: "orderType.text", title: "订单类型"
        }, {
            field: "receipDate", title: "接单日期"
        }, {
            field: "orderDate", title: "订单日期"
        }, {
            field: "status.text", title: "状态"
        }, {
            field: "orderType.text", title: "订单类型"
        }, {
            field: "transportType", title: "运输方式", template: function (dataItem) {
                if (dataItem.transportType) {
                    return dataItem.transportType.text;
                }
                return "";
            }
        }, {
            field: "remark", title: "备注"
        }]
    };


    $scope.detailGridOptions = function (dataItem) {
        var dataSource = new kendo.data.DataSource({
            data: []
        });

        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: contextPath + "/order/findItemsByOrderNo/" + dataItem.orderNo
        }).done(function (result) {
            if (result.success) {
                dataSource.data(result.body);
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败！");
        });
        return {
            dataSource: dataSource,
            columns: [
                {
                    field: "goodsCode",
                    title: "货物编码"
                }, {
                    field: "goodsName",
                    title: "货物名称"
                }, {
                    field: "goodsNumber",
                    title: "货物型号"
                }, {
                    field: "goodsQuantity",
                    title: "货物数量"
                }, {
                    field: "weight",
                    title: "重量"
                }, {
                    field: "volume",
                    title: "体积"
                }
            ]
        }
    }
});
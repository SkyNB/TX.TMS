'use strict';
var app = angular.module('RoadnetApp');

app.controller('SignController', function ($scope) {
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
        var customerOrderNo = $("#customerOrderNo").val();
        var customerCode = $("#customerCode").val();
        var data = $scope.mainGridDataSource.data();

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

        if (data.length >= 1) {
            App.toastr("每次只能签收一张订单!");
            $("#customerOrderNo").val("");
            $scope.textFocus();
            return;
        }

        $.ajax({
            type: "GET",
            url: contextPath + "/order/getByCustomerOrderNo/" + customerCode + "/" + customerOrderNo
        }).done(function (result) {
            if (result.body) {
                $scope.packageQty = result.body.totalPackageQty;
                $scope.getHasSignNumber(result.body.orderNo);
                data.splice(0, 0, result.body);
            } else {
                App.toastr("订单不属于当前客户！");
            }
            $("#customerOrderNo").val("");
        }).fail(function () {
            $("#customerOrderNo").val("");
        });
        $scope.textFocus();
    }

    $scope.getHasSignNumber = function (orderNo) {
        $.ajax({
            type: "GET",
            url: contextPath + "/order/getHasSignNum/" + orderNo
        }).done(function (result) {
            if (result.success) {
                $scope.orderSign.signNumber = $scope.packageQty - result.body;
                $scope.orderSign.yetSignNumber = $scope.packageQty - result.body;
            } else {
                App.toastr("签收箱数自动计算失败，请谨慎填写签收箱数！");
            }
        }).fail(function () {
            App.toastr("提交失败！");
        })
    }

    $scope.orderSign = {
        signMan: '0',
        signManCard: '000000-00000000-0000',
        signNumber: 1,
        yetSignNumber: 0,
        agentSignMan: '',
        agentSignManCard: '',
        signTime: new Date().format("yyyy-MM-dd HH:mm:ss"),
        feedbackSignTime: new Date().format("yyyy-MM-dd HH:mm:ss"),
        remark: ''
    };

    $scope.reset = function () {
        $scope.orderSign = {};
    };

    $scope.submit = function () {
        var data = $scope.mainGridDataSource.data();
        if (data.length < 1) {
            App.toastr("数据项为空！");
            $scope.textFocus();
            return;
        }

        if ($scope.orderSign.signNumber < 1) {
            App.toastr("签收箱数必须大于1");
            return;
        }

        $scope.orderSign.orderNo = data[0].orderNo;
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/order/sign",
            data: JSON.stringify($scope.orderSign)
        }).done(function (result) {
            if (result.success) {
                App.toastr("签收成功！", "info");
                $scope.clear();
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败！", "warning");
        });
        $scope.textFocus();
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
            field: "totalPackageQty", title: "箱数"
        },{
            field: "remark", title: "备注"
        }, {
            command: "destroy"
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
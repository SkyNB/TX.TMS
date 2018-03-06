'use strict';
var app = angular.module('RoadnetApp');

app.controller('ReceiptScanController', function ($scope, $rootScope) {
    $scope.textFocus = function () {
        $("#customerOrderNo").focus();
    }
    $scope.textFocus();

    $scope.orderDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    customerCode: {type: "string", editable: false},
                    orderNo: {type: "string", editable: false},
                    customerOrderNo: {type: "string", editable: false},
                    shipAddress: {type: "string", editable: false},
                    deliveryAddress: {type: "string", editable: false}
                }
            }
        }
    });

    $scope.orderColumns = [{
        field: "customerName",
        title: "客户"
    }, {
        field: "orderNo",
        title: "单号"
    }, {
        field: "customerOrderNo",
        title: "客户单号"
    }, {
        field: "shipAddress",
        title: "发货地址"
    }, {
        field: "deliveryAddress",
        title: "收货地址"
    }, {
        command: "destroy"
    }];

    $(document).keydown(function (event) {
        if (event.keyCode == 13) {
            $scope.findOrder();
            event.preventDefault();
        }
    });

    $scope.findOrder = function () {
        var customerCode = $("#customerCode").val();
        var customerOrderNo = $("#customerOrderNo").val();

        if (!customerCode) {
            App.toastr("请选择客户！");
            $scope.textFocus();
            return;
        }

        if (!customerOrderNo) {
            App.toastr("请输入客户订单号！");
            $scope.textFocus();
            return;
        }

        $.ajax({
            type: "GET",
            url: contextPath + "/order/getByCustomerOrderNo/" + customerCode + "/" + customerOrderNo
        }).done(function (result) {
            if (result.body) {
                var data = $scope.orderDataSource.data();
                if (data.length > 0) {
                    var flag = true;
                    for (var i = 0; i < data.length; i++) {
                        if (data[i].customerOrderNo == result.body.customerOrderNo)
                            flag = false;
                    }
                    if (flag)
                        data.splice(0, 0, result.body);
                } else {
                    data.splice(0, 0, result.body);
                }
            } else {
                App.toastr("订单不属于当前客户！");
            }
            $("#customerOrderNo").val("");
        }).fail(function () {
            //App.toastr("提交失败", "error");
            $("#customerOrderNo").val("");
        });
        $scope.textFocus();
    }

    $scope.submit = function () {
        var orderNos = [];
        var customerCode = $("#customerCode").val();
        var data = $scope.orderDataSource.data();

        if (data.length < 1) {
            App.toastr("数据项为空！");
            $scope.textFocus();
            return;
        }

        if (!customerCode) {
            App.toastr("请选择客户！");
            $("#customerCode").focus();
            return;
        }

        for (var i = 0; i < data.length; i++) {
            orderNos.push(data[i].orderNo);
        }

        $.ajax({
            type: "POST",
            url: contextPath + "/receipt/receiptScan/" + customerCode,
            contentType: "application/json",
            data: JSON.stringify(orderNos)
        }).done(function (result) {
            if (result.success) {
                App.toastr(result.message);
                $scope.clear();
            } else {
                App.toastr(result.message, "error");
                $scope.clear();
            }
        }).fail(function () {
            App.toastr("提交失败！");
        });
        $scope.textFocus();
    }

    $scope.clear = function () {
        $scope.orderDataSource.data([]);
        $scope.textFocus();
    }
});
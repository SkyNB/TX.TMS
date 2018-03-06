'use strict';
var app = angular.module('RoadnetApp');

app.controller('ConsignOrderStartupController', function ($scope, $rootScope, $state) {
    $scope.startupTime = new Date().format("yyyy-MM-dd HH:mm:ss");

    $scope.textFocus = function () {
        $("#consignNo").focus();
    }
    $scope.textFocus();

    $(document).keydown(function (event) {
        if (event.keyCode == 13) {
            $scope.findConsignOrder();
            event.preventDefault();
        }
    });

    $scope.mainGridDataSource = new kendo.data.DataSource({
        data: []
    });

    $scope.findConsignOrder = function () {
        var consignNo = $("#consignNo").val();
        var carrierCode = $("#carrierCode").val();

        if (!carrierCode) {
            App.toastr("请选择承运商！");
            $scope.textFocus();
            return;
        }

        if (!consignNo) {
            App.toastr("请输入托运单号！");
            $scope.textFocus();
            return;
        }

        $.ajax({
            type: "GET",
            url: contextPath + "/consign/getByConsignOrderNo/" + carrierCode + "/" + consignNo
        }).done(function (result) {
            if (result.body) {
                var data = $scope.mainGridDataSource.data();
                if (data.length > 0) {
                    var flag = true;
                    for (var i = 0; i < data.length; i++) {
                        if (data[i].consignOrderNo == result.body.consignOrderNo)
                            flag = false;
                    }
                    if (flag)
                        data.splice(0, 0, result.body);
                } else {
                    data.splice(0, 0, result.body);
                }
            } else {
                App.toastr("订单不属于当前承运商！");
            }
            $("#consignNo").val("");
        }).fail(function () {
            $("#consignNo").val("");
        });
        $scope.textFocus();
    }

    $scope.submit = function () {
        var consignOrderIds = [];
        var data = $scope.mainGridDataSource.data();

        if (data.length < 1) {
            App.toastr("数据项为空！");
            $scope.textFocus();
            return;
        }

        if (!$scope.startupTime) {
            App.toastr("请填写启运时间！");
            $scope.textFocus();
            return;
        }

        for (var i = 0; i < data.length; i++) {
            consignOrderIds.push(data[i].consignOrderId);
        }

        $.ajax({
            type: "POST",
            url: contextPath + "/consign/startUp",
            contentType: "application/json",
            data: JSON.stringify({consignOrderIds: consignOrderIds, startUpTime: $scope.startupTime})
        }).done(function (result) {
            if (result.success) {
                App.toastr("启运成功!", "success");
                $scope.clear();
            } else {
                App.toastr("启运失败！" + result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败！");
        });
        $scope.textFocus();
    }

    $scope.clear = function () {
        $scope.mainGridDataSource.data([]);
        $("#consignGrid").data("kendoGrid").setDataSource($scope.mainGridDataSource);
        $scope.textFocus();
    };

    $scope.mainGridOptions = {
        dataSource: {
            data: $scope.mainGridDataSource.data(),
            schema: {
                model: {
                    id: "consignOrderId",
                    fields: {
                        consignOrderNo: {type: "string", editable: false,},
                        carrierName: {editable: false,}
                    }
                }
            }
        },
        dataBound: function () {
            this.expandRow(this.tbody.find("tr.k-master-row").first());
        },
        editable: true,
        columns: [{
            field: "consignOrderNo",
            title: "托运单号",
            width: 135
        }, {
            field: "carrierName",
            title: "承运商",
            width: "120px"
        }, {
            field: "status.text",
            title: "状态"
        }, {
            field: "consigneeAddress",
            title: "目的地"
        }, {
            field: "consignee",
            title: "收货人"
        }, {
            command: "destroy"
        }]
    };


    $scope.detailGridOptions = function (dataItem) {
        var dataSource = new kendo.data.DataSource({
            data: []
        });
        var consignOrderIds = [];
        consignOrderIds.push(dataItem.consignOrderId);
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/consign/findItemByIds",
            data: JSON.stringify(consignOrderIds)
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
                    field: "orderNo",
                    title: "订单号"
                }, {
                    field: "packageQuantity",
                    title: "箱数"
                }, {
                    field: "volume",
                    title: "体积"
                }, {
                    field: "weight",
                    title: "重量"
                }, {
                    field: "receiptPageNumber",
                    title: "回单页数"
                }
            ]
        }
    }

});
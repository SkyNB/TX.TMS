'use strict';
var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('update/:orderId', {
            url: '/update/:orderId',
            reload: true,
            templateUrl: contextPath + '/order/update',
            controller: "OrderUpdateController",
            resolve: getDeps([contextPath + '/resources/pages/order/update.js'])
        })
        .state('receiptPic/:customerCode/:corderNo', {
            url: '/receiptPic/:customerCode/:corderNo',
            reload: true,
            templateUrl: contextPath + '/receipt/getOrderReceipt',
            controller: "ReceiptPicController",
            resolve: getDeps([contextPath + '/resources/pages/receipt/picture/index.js'])
        })
});

app.controller('ReceiptStatusController', function ($scope, $rootScope) {
    $scope.$on('$viewContentLoaded', function () {
        App.resizeGrid();
        $scope.grid = $("#gridOrder").data("kendoExGrid");
    });

    $scope.returnReceipt = function () {
        var orderIds = [];
        var rows = $scope.grid.select();

        if (rows.length < 1) {
            App.toastr("请至少选择一条数据！", "warning");
            return;
        }

        rows.each(function (i, row) {
            var dataItem = $scope.grid.dataItem(row);
            if (dataItem)
                orderIds.push(dataItem.orderId);
        });

        $.ajax({
            url: contextPath + "/receipt/returnReceipt",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(orderIds)
        }).done(function (result) {
            if (result.success) {
                $scope.data.query();
            } else {
                App.toastr(result.message, "error");
            }
        });
    };

    $scope.retroactive = function () {
        var orderIds = [];
        var rows = $scope.grid.select();

        if (rows.length < 1) {
            App.toastr("请至少选择一条数据！", "warning");
            return;
        }

        rows.each(function (i, row) {
            var dataItem = $scope.grid.dataItem(row);
            if (dataItem)
                orderIds.push(dataItem.orderId);
        });

        $.ajax({
            url: contextPath + "/receipt/retroactive",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(orderIds)
        }).done(function (result) {
            if (result.success) {
                $scope.data.query();
            } else {
                App.toastr(result.message, "error");
            }
        });
    }

    $scope.search = function () {
        $scope.data.filter($("#fromSearchOrder").serializeArray());
    };

    $rootScope.data = getDataSource("orderNo", contextPath + "/receipt/searchForReceipt");

    $scope.gridOptions = {
        dataSource: $scope.data, dataBound: function () {
            $scope.grid = $("#gridOrder").data("kendoExGrid");
        }, columns: [{
            field: "orderNo", title: "单号", width: 130,
            template: function (dataItem) {
                var content = "<div class='data-popover-content'>" +
                    "<p>单号：" + dataItem.orderNo + "</p>" +
                    "<p>订单日期：" + dataItem.orderDate + "</p>" +
                    "<p>运输方式：" + dataItem.transportType.text + "</p>" +
                    "<p>状态：" + dataItem.status.text + "</p>"
                if (dataItem.thumbPath) {
                    content += "回单：<a target='_blank' href='" + dataItem.filePath + "'>" + "<img src='" + dataItem.thumbPath + "' width='100' height='100'/>" + "</a></br>"
                } else {
                    content += "回单：未上传回单</br>";
                }
                content += "<a href='#/update/" + dataItem.orderId + "' data-target='#updateOrder' data-toggle='modal'>详情</a></div>";

                // return "<a href='#/update/" + dataItem.orderId + "' data-target='#updateOrder' data-toggle='modal'>" + dataItem.orderNo + "</a>";
                return "<a href='#' data-toggle='popover'>" + dataItem.orderNo + "</a> " + content;
            }
        }, {
            field: "customerName", title: "客户", width: 90, sortable: false
        }, {
            field: "customerOrderNo", title: "客户单号"
        }, {
            field: "receipDate", title: "接单日期"
        }, {
            field: "orderDate", title: "订单日期"
        }, {
            field: "o.status", title: "状态", template: function (dataItem) {
                if (dataItem.status) {
                    return dataItem.status.text;
                }
                return "";
            }
        }, {
            field: "orderType", title: "订单类型", template: function (dataItem) {
                if (dataItem.orderType) {
                    return dataItem.orderType.text;
                }
                return "";
            }
        }, {
            field: "transportType", title: "运输方式", template: function (dataItem) {
                if (dataItem.transportType) {
                    return dataItem.transportType.text;
                }
                return "";
            }
        }, {
            field: "receiptStatus", title: "回单状态", template: function (dataItem) {
                if (dataItem.receiptStatus) {
                    return dataItem.receiptStatus.text;
                }
                return "尚无回单信息";
            }
        }, {
            field: "remark", title: "回单", sortable: false, template: function (dataItem) {
                if (dataItem.thumbPath)
                    return "<a href='#/receiptPic/" +dataItem.customerCode + "/" + dataItem.customerOrderNo +"' data-target='#receiptPic' data-toggle='modal'>"+ "回单图片" + "</a>";
                else
                    return "回单图片";
            }
        }, {
            field: "remark", title: "备注", sortable: false
        }]
    };
});
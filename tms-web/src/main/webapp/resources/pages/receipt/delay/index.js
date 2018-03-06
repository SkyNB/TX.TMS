'use strict';
var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('update/:orderId', {
            url: '/update/:orderId',
            reload: true,
            templateUrl: contextPath + '/order/update',
            controller: "OrderUpdateController",
            resolve: getDeps([contextPath + '/assets/pages/order/update.js'])
        })
});

app.controller('ReceiptDelayController', function ($scope, $rootScope) {
    $scope.customerOptions = {
        dataSource: getComboDatasource('/customer/getAvailableForSelect'),
        filter: "contains",
        dataTextField: "text",
        dataValueField: "value"
    };

    $scope.search = function () {
        $scope.data.filter($("#fromSearchReceiptDelay").serializeArray());
    }

    $scope.$on('$viewContentLoaded', function () {
        App.resizeGrid();
        $scope.grid = $("#gridDelay").data("kendoExGrid");
    });

    $rootScope.data = getDataSource("orderNo", contextPath + "/receipt/getDelayData");

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "orderNo", title: "单号", width: 125,
            template: function (dataItem) {
                var content = "<div class='data-popover-content'>" +
                    "<p>单号：" + dataItem.orderNo + "</p>" +
                    "<p>订单日期：" + dataItem.orderDate + "</p>" +
                    "<p>运输方式：" + dataItem.transportType.text + "</p>" +
                    "<p>状态：" + dataItem.status.text + "</p>" +
                    "<a href='#/update/" + dataItem.orderId + "' data-target='#updateOrder' data-toggle='modal'>详情</a></div>";
                // return "<a href='#/update/" + dataItem.orderId + "' data-target='#updateOrder' data-toggle='modal'>" + dataItem.orderNo + "</a>";
                return "<a href='#' data-toggle='popover'>" + dataItem.orderNo + "</a> " + content;
            }
        }, {
            field: "customerCode", title: "客戶", width: 80,
            template: function (dataItem) {
                if (dataItem.customerName) {
                    return dataItem.customerName;
                } else {
                    return "";
                }
            }
        }, {
            field: "customerOrderNo", title: "客户单号"
        }, {
            field: "receipDate", title: "接单日期"
        }, {
            field: "orderDate", title: "订单日期"
        }, {
            field: "lo.status", title: "状态",
            template: function(dataItem) {
                if(dataItem.status && dataItem.status.text) {
                    return dataItem.status.text
                } else {
                    return "";
                }
            }
        }, {
            field: "orderType", title: "订单类型",
            template: function(dataItem) {
                if(dataItem.orderType && dataItem.orderType.text) {
                    return dataItem.orderType.text
                } else {
                    return "";
                }
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
                return "";
            }
        }, {
            field: "remark", title: "备注",sortable:false
        }]
    };
});
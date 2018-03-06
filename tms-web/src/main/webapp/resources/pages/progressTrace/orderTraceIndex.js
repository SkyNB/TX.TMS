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
        .state('addOrderTrace', {
            url: '/addOrderTrace',
            reload: true,
            templateUrl: function (params) {
                return contextPath + '/progressTrace/addOrderTrace';
            },
            controller: "addOrderTraceController",
            resolve: getDeps([
                contextPath + '/resources/pages/progressTrace/addOrderTrace.js'
            ])
        })
        .state('subscribe', {
            url: '/subscribe',
            reload: true,
            templateUrl: contextPath + '/progressTrace/subscribe',
            controller: "SubscribeController",
            resolve: getDeps([
                contextPath + '/assets/pages/progressTrace/subscribe.js'
            ])
        })
});

app.controller('OrderTraceController', function ($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchOrder").serializeArray());
    };

    $rootScope.data = getDataSource("orderNo", contextPath + "/progressTrace/orderSearch");

    $scope.gridOptions = {
        dataSource: $scope.data, dataBound: function () {
            $scope.grid = $("#gridOrder").data("kendoExGrid");
        }, columns: [{
            field: "orderNo", title: "单号",
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
            field: "customerName", title: "客户",width:90
        }, {
            field: "customerOrderNo", title: "客户单号"
        },  {
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

    $scope.addOrderTrace = function () {
        var row = $scope.grid.select();

        if (row.length > 1) {
            App.toastr("仅能选择一条订单", "warning");
            return;
        }

        if (row.length == 0) {
            App.toastr("请选择一条订单", "warning");
            return;
        }

        var dataItem = $scope.grid.dataItem(row);
        //暂时不考虑状态的控制
        $rootScope.orderNo = dataItem.orderNo;
        $rootScope.customerOrderNo = dataItem.customerOrderNo;
        $rootScope.customerCode = dataItem.customerCode;
        $state.go("addOrderTrace");
        $("#addOrderTrace").modal("show");
    }

    $scope.subscribe = function () {
        var rows = $scope.grid.select();
        var orderNos = [];

        if (rows.length < 1) {
            App.toastr("请选择一条订单", "warning");
            return;
        }

        rows.each(function (i, row) {
            var dataItem = $scope.grid.dataItem(row);
            orderNos.push(dataItem.orderNo);
        })

        $rootScope.orderNos = orderNos;

        $state.go("subscribe");
        $("#subscribe").modal("show");
    }
});
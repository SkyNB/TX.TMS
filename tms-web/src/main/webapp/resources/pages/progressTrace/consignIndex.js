'use strict';
var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('importConsignTrace', {
            url: '/importConsignTrace',
            reload: true,
            templateUrl: contextPath + '/progressTrace/importConsignTrace',
            controller: "ImportConsignTraceController",
            resolve: getDeps([contextPath + '/assets/pages/progressTrace/importConsignTrace.ftl.js'])
        })
        .state('detail/:consignOrderId', {
            url: '/detail/:consignOrderId',
            reload: true,
            templateUrl: contextPath + '/consign/detail',
            controller: "ConsignDetailController",
            resolve: getDeps([contextPath + '/assets/pages/consign/detail.js'])
        })
        .state('addConsignTrace', {
            url: '/addConsignTrace',
            reload: true,
            templateUrl: contextPath + '/progressTrace/addConsignTrace',
            controller: "addConsignTraceController",
            resolve: getDeps([
                contextPath + '/assets/pages/progressTrace/addConsignTrace.js'
            ])
        })
});

app.controller('ConsignTraceController', function ($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchConsign").serializeArray());
    };

    $rootScope.data = getDataSource("consignOrderId", contextPath + "/consign/search");
    $scope.dataBound = function () {
        $scope.grid = $("#gridConsign").data("kendoExGrid");
    };
    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "consignOrderNo",
            title: "托运单号",
            template: function (dataItem) {
                return "<a href='#/detail/" + dataItem.consignOrderId + "' data-target='#consignDetail' data-toggle='modal'>" + dataItem.consignOrderNo + "</a>";
            }
        }, {
            field: "carrierName",
            title: "承运商"
        }, {
            field: "status.text",
            title: "状态"
        }, {
            field: "transportType",
            title: "运输方式",
            values: transportTypes
        }, {
            field: "destCityName",
            title: "目的地"
        }, {
            field: "consignee",
            title: "收货人"
        }, {
            field: "totalVolume",
            title: "总体积"
        }, {
            field: "totalPackageQuantity",
            title: "总箱数"
        }, {
            field: "createUserName",
            title: "发货人"
        }]
    };

    $scope.addConsignTrace = function () {
        var row = $scope.grid.select();

        if (row.length > 1) {
            toastr.warning("仅能选择一条托运单！");
            return;
        }

        if (row.length == 0) {
            toastr.warning("请选择一条托运单！");
            return;
        }

        var dataItem = $scope.grid.dataItem(row);

        //考虑到也许已经进行了后续操作，却没有维护跟踪信息
        /*if (dataItem.status.name != "IN_TRANSIT") {
         toastr.warning("只能为状态为在途的托运单添加跟踪信息！");
         return;
         }*/

        $rootScope.consignOrderNo = dataItem.consignOrderNo;
        $rootScope.carrierCode = dataItem.carrierCode;
        $state.go("addConsignTrace");
        $("#addConsignTrace").modal("show");
    }
});
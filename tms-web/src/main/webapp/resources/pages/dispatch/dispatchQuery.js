'use strict';
var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/dispatch/create',
            controller: "DispatchCreateController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/create.js'])
        })
        .state('batchConfirm', {
            url: '/batchConfirm',
            reload: true,
            templateUrl: contextPath + '/package/batchConfirm',
            controller: "BatchConfirmController",
            resolve: getDeps([contextPath + '/resources/pages/package/batchConfirm.js'])
        })
});

app.controller('DispatchQueryController', function ($scope, $rootScope, $state) {

    $scope.search = function () {
        $scope.data.filter($("#fromSearchDispatchQuery").serializeArray());
    };

    $rootScope.data = getDataSource("dispatchId", contextPath + "/dispatch/dispatchQuerySearch");

    $scope.dataBound = function () {
        $scope.grid = $("#gridDispatchQuery").data("kendoExGrid");
    };

    $scope.createDispatch = function () {
        var rows = $scope.grid.getSelectedData();
        if(rows.length ==0 ){
            App.toastr("请选择数据", "warning");
            return ;
        }
        $rootScope.selectedOrderNos = [];
        $.each($scope.grid.getSelectedData(), function (i, item) {
            $rootScope.selectedOrderNos.push(item.orderNo);
        });

        $.ajax({
            url: contextPath + "/package/judgeOrdersIsHavePacked/",
            type: "POST",
            contentType: "application/json",
            data:JSON.stringify($rootScope.selectedOrderNos)
        }).done(function (result) {
            if (result.success) {
                var orderNos = result.body;
                if(orderNos.length > 0 ){
                    $rootScope.selectedData = [];
                    $.each(orderNos,function(index,item){
                        var obj = new Object();
                        obj.orderNo = item;
                        $rootScope.selectedData.push(obj);
                    });
                    $rootScope.isCreateDispatch = true;
                    $state.go("batchConfirm");
                    $("#batchConfirm").modal("show");
                }else {
                    $state.go("create");
                    $("#addDispatch").modal("show");
                }
            } else {
                toastr.error("操作失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "orderNo",
            title: "单号",
            width: 150
        }, {
            field: "customerName",
            title: "客户",
            width: 150
        },{
            field: "customerOrderNo",
            title: "客户单号",
            width: 150
        }, {
            field: "orderType.text",
            title: "订单/指令",
            width: 100
        }, {
            field: "totalPackageQty",
            title: "总箱数",
            width: 100
        }, {
            field: "totalVolume",
            title: "总体积",
            width: 100
        }, {
            field: "totalWeight",
            title: "总重量",
            width: 100
        }, {
            field: "destinationName",
            title: "目的城市",
            width: 100
        }, {
            field: "deliveryContacts",
            title: "收货人",
            width: 100
        }, {
            field: "address",
            title: "地址",
            width: 200
        }]
    };
});

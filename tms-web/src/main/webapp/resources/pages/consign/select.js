var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/consign/create',
            controller: "ConsignCreateController",
            resolve: getDeps([contextPath + '/resources/pages/consign/create.js'])
        })
        .state('batchConfirm', {
            url: '/batchConfirm',
            reload: true,
            templateUrl: contextPath + '/package/batchConfirm',
            controller: "BatchConfirmController",
            resolve: getDeps([contextPath + '/resources/pages/package/batchConfirm.js'])
        })
        .state('batchConsign', {
            url: '/batchConsign',
            reload: true,
            templateUrl: contextPath + '/consign/batchConsign',
            controller: "ConsignBatchConsignController",
            resolve: getDeps([contextPath + '/resources/pages/consign/batchConsign.js'])
        })
        .state('createByDriver', {
            url: '/createByDriver',
            reload: true,
            templateUrl: contextPath + '/consign/createByDriver',
            controller: "ConsignCreateByDriverController",
            resolve: getDeps([contextPath + '/resources/pages/consign/createByDriver.js'])
        })
});

app.controller('SelectOrderController', ['$scope', '$rootScope', '$state', SelectOrderController]);
function SelectOrderController($scope, $rootScope, $state) {

    $scope.search = function () {
        $scope.data.filter($("#selectOrderForm").serializeArray());
    };

    $rootScope.data = getDataSource("orderId", contextPath + "/consign/selectOrder");

    $scope.dataBound = function () {
        $scope.grid = $("#selectOrderGrid").data("kendoExGrid");
    };

    $scope.select = function () {
        var rows = $scope.grid.getSelectedData();
        if(rows.length ==0 ){
            App.toastr("请选择数据", "warning");
            return ;
        }
        $rootScope.selectOrders = rows;
        var orderNos = [];
        $.each(rows, function (i, item) {
            orderNos.push(item.orderNo);
        });

        $.ajax({
            url: contextPath + "/package/judgeOrdersIsHavePacked/",
            type: "POST",
            contentType: "application/json",
            data:JSON.stringify(orderNos)
        }).done(function (result) {
            if (result.success) {
                var orderNos1 = result.body;
                if(orderNos1.length > 0 ){
                    $rootScope.selectedData = [];
                    $.each(orderNos1,function(index,item){
                        var obj = new Object();
                        obj.orderNo = item;
                        $rootScope.selectedData.push(obj);
                    });
                    $rootScope.isCreateConsign = true;
                    $state.go("batchConfirm");
                    $("#batchConfirm").modal("show");
                }else {
                    $state.go("create");
                    $("#createConsign").modal("show");
                }
            } else {
                toastr.error("操作失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.batchConsign = function () {
        var rows = $scope.grid.getSelectedData();
        if(rows.length ==0 ){
            App.toastr("请选择数据", "warning");
            return ;
        }
        $rootScope.selectOrders = rows;
        var orderNos = [];
        $.each(rows, function (i, item) {
            orderNos.push(item.orderNo);
        });

        $.ajax({
            url: contextPath + "/package/judgeOrdersIsHavePacked/",
            type: "POST",
            contentType: "application/json",
            data:JSON.stringify(orderNos)
        }).done(function (result) {
            if (result.success) {
                var orderNos1 = result.body;
                if(orderNos1.length > 0 ){
                    $rootScope.selectedData = [];
                    $.each(orderNos1,function(index,item){
                        var obj = new Object();
                        obj.orderNo = item;
                        $rootScope.selectedData.push(obj);
                    });
                    $rootScope.isBatchConsign = true;
                    $state.go("batchConfirm");
                    $("#batchConfirm").modal("show");
                }else {
                    $state.go("batchConsign");
                    $("#batchConsign").modal("show");
                }
            } else {
                toastr.error("操作失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.createByDriver = function(){
        $state.go("createByDriver");
        $("#createByDriver").modal("show");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "orderNo",
            title: "单号",
            width: 200
        }, {
            field: "customerOrderNo",
            title: "客户单号",
            width: 150
        }, {
            field: "customerName",
            title: "客户",
            width: 150
        }, {
            field: "transportType.text",
            title: "运输方式",
            width: 150
        }, {
            field: "deliveryContacts",
            title: "收货人",
            width: 150
        },  {
            field: "orderDate",
            title: "订单日期",
            width: 150
        }, {
            field: "shipCity",
            title: "始发城市",
            width: 150
        }, {
            field: "deliveryCity",
            title: "目的城市",
            width: 150
        }]
    };
}
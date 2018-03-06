var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('detail/:exceptionId', {
            url: '/detail/:exceptionId',
            reload: true,
            templateUrl: contextPath + '/exception/detail',
            controller: "ExceptionDetailController",
            resolve: getDeps([contextPath + '/resources/pages/exception/detail.js'])
        })
        .state('create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/exception/create',
            controller: "ExceptionCreateController",
            resolve: getDeps([contextPath + '/resources/pages/exception/create.js'])
        })
        .state('close', {
            url: '/close',
            reload: true,
            templateUrl: contextPath + '/exception/close',
            controller: "ExceptionCloseController",
            resolve: getDeps([contextPath + '/resources/pages/exception/close.js'])
        })
});

app.controller('ExceptionController', ['$scope', '$rootScope', '$state', ExceptionController]);
function ExceptionController($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchException").serializeArray());
    };

    $rootScope.data = getDataSource("id", contextPath + "/exception/search");

    $scope.dataBound = function () {
        $scope.grid = $("#gridException").data("kendoExGrid");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "code",
            title: "编码",
            width: 120,
            template: function (dataItem) {
                return "<a href='#/detail/" + dataItem.id + "' data-target='#exceptionDetail' data-toggle='modal'>" + dataItem.code + "</a>";
            }
        }, {
            field: "orderNo",
            width: 180,
            title: "订单号"
        }, {
            field: "classification.text",
            width: 80,
            title: "分类"
        }, {
            field: "type",
            width: 120,
            title: "类型",
            template:function(dataItem){
                return dataItem.typeName;
            }
        }, {
            field: "status.text",
            width: 80,
            title: "状态"
        }, {
            field: "occurTime",
            width: 175,
            title: "产生时间"
        }, {
            field: "personsResponsible",
            width: 120,
            title: "责任人"
        }, {
            field: "goodsValue",
            width: 100,
            title: "货值(元)"
        }, {
            field: "compensationToCustomer",
            width: 100,
            title: "赔偿(元)"
        }, {
            field: "insurance",
            width: 100,
            title: "保险(元)"
        }, {
            field: "damage",
            width: 100,
            title: "损失(元)"
        }]
    };

    $scope.create = function () {
        $("#exceptionCreate").modal("show");
    }

    $scope.close = function () {
        var row = $scope.grid.select();

        if (row.length != 1) {
            App.toastr("请选择一条数据！");
            return;
        }

        var dataItem = $scope.grid.dataItem(row);

        if (dataItem.status.name === "CLOSE") {
            App.toastr("异常已经关闭！");
            return;
        }

        $rootScope.code = dataItem.code;
        $state.go("close");
        $("#exceptionClose").modal("show");
    }
}

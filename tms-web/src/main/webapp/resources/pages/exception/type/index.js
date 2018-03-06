var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('update/:code', {
            url: '/update/:code',
            reload: true,
            templateUrl: contextPath + '/exceptionType/update',
            controller: "ExceptionUpdateController",
            resolve: getDeps([contextPath + '/resources/pages/exception/type/update.js'])
        })
        .state('create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/exceptionType/create',
            controller: "ExceptionTypeCreateController",
            resolve: getDeps([contextPath + '/resources/pages/exception/type/create.js'])
        })
});

app.controller('ExceptionTypeController', ['$scope', '$rootScope', ExceptionTypeController]);
function ExceptionTypeController($scope, $rootScope) {
    $scope.search = function () {
        $scope.data.filter($("#formSearchExceptionType").serializeArray());
    };

    $rootScope.data = getDataSource("id", contextPath + "/exceptionType/search");

    $scope.dataBound = function () {
        $scope.grid = $("#gridExceptionType").data("kendoExGrid");
    };

    $scope.gridOptions = $.extend(getGridOptions(), {
        dataSource: $scope.data,
        pageable: false,
        columns: [{
            field: "code",
            title: "编码",
            template: function (dataItem) {
                return "<a href='#/update/" + dataItem.code + "' data-target='#exceptionTypeUpdate' data-toggle='modal'>" + dataItem.code + "</a>";
            }
        }, {
            field: "name",
            title: "名称"
        }, {
            field: "remark",
            title: "备注"
        }]
    });

    $scope.create = function () {
        $("#exceptionTypeCreate").modal("show");
    }
}

'use strict';

var app = angular.module('RoadnetApp');

app.controller('ClientServerController', ['$scope', '$rootScope', '$stateParams','$state',ClientServerController]);
function ClientServerController($scope, $rootScope, $stateParams,$state) {
    $("#clientTaskset").modal();

    $rootScope.data = getDataSource("customerId", contextPath + "/customer/search");
    $scope.gridOptions = $.extend(getGridOptions(), {
        dataSource: $scope.data,
        columns: [ {
            field: "",
            title: "编码",
            width: 300
        }, {
            field: "",
            title: "门店",
            width: 300
        }, {
            field: "",
            title: "",
            width: 150,
            template: function () {
                return "<a class='remove' href='javascript:'> <i class='fa fa-times text-danger'></i> </a>";
            }
        }]
    });


}




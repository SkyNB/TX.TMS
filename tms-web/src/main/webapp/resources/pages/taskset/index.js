'use strict';
var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('addtask', {
            url: '/addtask',
            reload: true,
            templateUrl: contextPath + '/taskset/addtask',
            controller: "AddtaskManagementController",
            resolve: getDeps([contextPath + '/resources/taskset/addtask.js'])
        })
        .state('importtask', {
            url: '/importtask',
            reload: true,
            templateUrl: contextPath + '/taskset/importtask',
            controller: "ImportTaskController",
            resolve: getDeps([contextPath + '/resources/taskset/importtask.js'])
        })
        .state('clientserver', {
            url: '/clientserver',
            reload: true,
            templateUrl: contextPath + '/taskset/clientserver',
            controller: "ClientServerController",
            resolve: getDeps([contextPath + '/resources/taskset/clientserver.js'])
        })
});

app.controller('MangageController', function UserController($rootScope, $scope, $state) {
    $scope.vehicleOptions = {
        dataSource: getComboDatasource('/vehicle/getAvailableForSelect'),
        filter: "contains",
        dataTextField: "text",
        dataValueField: "value"
    };

    $rootScope.data = getDataSource("taskTeamId", contextPath + "/taskset/search");
    $scope.gridOptions = $.extend(getGridOptions(), {
        dataSource: $scope.data,
        columns: [{
            field: "name",
            title: "名称",
            width: 200,
            template: function (dataItem) {
                return "<a href='#/clientserver' data-target='#clientTaskset' data-toggle='modal'>" + dataItem.name + "</a>";
            }
        }, {
            field: "type.text",
            title: "类型",
            width: 200
        }, {
            field: "createUserName",
            title: "创建人",
            width: 200
        }, {
            field: "createDate",
            title: "创建时间",
            width: 200
        }, {
            field: "driver",
            title: "司机",
            width: 200
        }]
    });
});
'use strict';
var app = angular.module('RoadnetApp');

app.controller('ImportConsignTraceController', function ($scope, $rootScope) {
    $("#importConsignTrace").modal();

    $scope.submit = function () {
        $("#importConsignTraceForm").ajaxSubmit({
            url: contextPath + "/progressTrace/importConsignTrace",
            success: function (result) {
                if (result.success) {
                    $scope.resultData.data(result.body);
                    App.toastr("导入成功", "success");
                } else {
                    $scope.resultData.data(result.body || []);
                    App.toastr(result.message || "导入失败", "error");
                }
            }
        })
    };

    $scope.resultData = new kendo.data.DataSource({data: []});
    $scope.errorOptions = {
        dataSource: $scope.resultData,
        columns: [
            {
                title: "结果",
                field: "success",
                template: "#=success?'成功':'失败'#",
                width: 50
            },
            {
                title: "原因",
                field: "message",
                width: 200
            }
        ]
    };
});
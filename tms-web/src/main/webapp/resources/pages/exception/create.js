'use strict';

var app = angular.module('RoadnetApp');

app.controller('ExceptionCreateController', function ($scope, $rootScope) {
    $("#exceptionCreate").modal();

    $scope.exception = {};

    $scope.reset = function () {
        $scope.exception = {};
    }

    $scope.validate = $("#addExceptionForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/exception/create",
            data: JSON.stringify($scope.exception)
        }).done(function (result) {
            if (result.success) {
                $("#exceptionCreate").modal("hide");
                $rootScope.data.query();
                $scope.reset();
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败！", "error");
        });
    }

    $scope.getSite = function () {
        var sites = $("#siteCode").data("kendoComboBox");
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: contextPath + "/site/getByBranchCode/" + $("#branchCode").val()
        }).done(function (result) {
            if (result) {
                var dataSource = new kendo.data.DataSource({
                    data: result,
                    serverFiltering: true
                });
                sites.setDataSource(dataSource);
            } else {
                var dataSource = new kendo.data.DataSource({
                    data: [],
                    serverFiltering: true
                });
                sites.setDataSource(dataSource);
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败", "error");
        });
    };
});
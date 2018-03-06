'use strict';

var app = angular.module('RoadnetApp');

app.controller('ExceptionTypeCreateController', function ($scope, $rootScope) {
    $("#exceptionTypeCreate").modal();

    $scope.exceptionType = {};

    $scope.reset = function () {
        $scope.exceptionType = {};
    }

    $scope.validate = $("#addExceptionTypeForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/exceptionType/create",
            data: JSON.stringify($scope.exceptionType)
        }).done(function (result) {
            if (result.success) {
                $("#exceptionTypeCreate").modal("hide");
                $rootScope.data.query();
                $scope.reset();
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败！", "error");
        });
    }
});
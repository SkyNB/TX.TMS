'use strict';

var app = angular.module('RoadnetApp');

app.controller('ExceptionUpdateController', function ($scope, $rootScope, $stateParams) {
    $scope.init = function () {
        $.ajax({
            url: contextPath + "/exceptionType/getByCode/" + $stateParams.code,
            type: "GET"
        }).done(function (result) {
            if (result.success) {
                $scope.exceptionType = result.body;
                if ($scope.exceptionType) {
                    $("#exceptionTypeUpdate").modal();
                    setTimeout(function () {
                        $scope.$apply();
                    }, 200)
                }
            } else {
                toastr.error(result.message);
            }
        }).fail(function () {
            App.toastr("提交失败", "error");
        });
    };

    $scope.validate = $("#updateExceptionTypeForm").validate();
    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        $.ajax({
            url: contextPath + "/exceptionType/update",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.exceptionType)
        }).done(function (result) {
            if (result.success) {
                $("#exceptionTypeUpdate").modal("hide");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        });
    };

    $scope.init();
});




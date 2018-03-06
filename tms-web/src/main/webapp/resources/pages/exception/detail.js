'use strict';

var app = angular.module('RoadnetApp');

app.controller('ExceptionDetailController', function ($scope, $stateParams) {
    $scope.init = function () {
        $.ajax({
            url: contextPath + "/exception/get/" + $stateParams.exceptionId,
            type: "GET",
            contentType: "application/json"
        }).done(function (result) {
            if (result.success) {
                $scope.exception = result.body;
                $scope.exception.status = result.body.status.text;
                $scope.exception.classification = result.body.classification.text;
                if ($scope.exception) {
                    $("#exceptionDetail").modal();
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

    $scope.exception = {};

    $scope.init();
});
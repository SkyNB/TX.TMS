'use strict';

var app = angular.module('RoadnetApp');

app.controller('ExceptionCloseController', function ($scope, $rootScope) {
    if ($rootScope.code)
        $("#exceptionClose").modal();

    $scope.exceptionClose = {
        code: "",
        goodsValue: 0,
        compensationToCustomer: 0,
        insurance: 0,
        damage: 0
    }

    $scope.validate = $("#closeExceptionForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        $scope.exceptionClose.code = $rootScope.code;

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "exception/close",
            data: JSON.stringify($scope.exceptionClose)
        }).done(function (result) {
            if (result.success) {
                App.toastr("保存成功！");
                $("#exceptionClose").modal("hide");
                $rootScope.data.query();
            } else {
                App.toastr(result.message);
            }
        }).fail(function () {
            App.toastr("提交失败！");
        });
    }
});
'use strict';

var app = angular.module('RoadnetApp');

app.controller('addOrderTraceController', function ($scope, $rootScope) {
    if ($rootScope.orderNo && $rootScope.customerOrderNo && $rootScope.customerCode) {
        $("#addOrderTrace").modal();
    }

    $("#addOrderTrace").on("shown.bs.modal", function () {
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: contextPath + "/progressTrace/orderTraceQuery/" + $rootScope.orderNo
        }).done(function (result) {
            if (result.success) {
                if (result.body && result.body.items.length>0) {
                    $scope.traceInfo = result.body.items;
                    $("#isHide").hide();
                    $scope.$apply();
                } else {
                    $scope.traceInfo = [];
                    $("#isHide").show();
                    $scope.$apply();
                }
            } else {
                $scope.traceInfo = [];
                $scope.$apply();
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败", "error");
        });

    });

    $scope.traceInfo = [];
    $scope.orderTrace = {};

    $scope.reset = function () {
        $scope.orderTrace = {};
    }

    $scope.validate = $("#addOrderTraceForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        $scope.orderTrace.orderNo = $rootScope.orderNo;
        $scope.orderTrace.customerOrderNo = $rootScope.customerOrderNo;
        $scope.orderTrace.customerCode = $rootScope.customerCode;

        $.ajax({
            url: contextPath + "/progressTrace/addOrderTrace",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.orderTrace)
        }).done(function (result) {
            if (result.success) {
                App.toastr("保存成功！");
                $("#addOrderTrace").modal("hide");
                $scope.reset();
            } else {
                App.toastr("保存失败！" + result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败", "error");
        })
    }
});
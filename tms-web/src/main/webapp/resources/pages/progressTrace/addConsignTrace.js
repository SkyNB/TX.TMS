'use strict';

var app = angular.module('RoadnetApp');

app.controller('addConsignTraceController', function ($scope, $rootScope) {
    if ($rootScope.consignOrderNo && $rootScope.carrierCode) {
        $("#addConsignTrace").modal();
    }

    $("#addConsignTrace").on("shown.bs.modal", function () {
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: contextPath + "/progressTrace/deliveryOrderProgressQuery/" + $rootScope.consignOrderNo + "/" + $rootScope.carrierCode
        }).done(function (result) {
            if (result.success) {
                if (result.body && result.body.length>0) {
                    $("#isHide").hide();
                    $scope.traceInfo = result.body;
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

    $scope.consignTrace = {};

    $scope.reset = function () {
        $scope.consignTrace = {};
    }

    $scope.validate = $("#addConsignForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        $scope.consignTrace.shipper = $rootScope.carrierCode;
        $scope.consignTrace.deliveryNo = $rootScope.consignOrderNo;

        $.ajax({
            url: contextPath + "/progressTrace/addConsignTrace",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.consignTrace)
        }).done(function (result) {
            console.info(result);
            if (result.success) {
                App.toastr("保存保存成功！");
                $("#addConsignTrace").modal("hide");
                $scope.reset();
            } else {
                App.toastr("保存失败！" + result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败", "error");
        })
    }
});
'use strict';
var app = angular.module('RoadnetApp');

app.controller('SubscribeController', function ($scope, $rootScope, $state) {
    if ($rootScope.orderNos && $rootScope.orderNos.length > 0)
        $("#subscribe").modal();

    $scope.marketOrderNo = "";
    $scope.subscribeDeliveryNo = "";
    $scope.subscribeTime = "";

    $scope.reset = function () {
        $scope.marketOrderNo = "";
        $scope.subscribeDeliveryNo = "";
        $scope.subscribeTime = "";
    }

    $("#subscribe").on("shown.bs.modal", function () {
        $.ajax({
            url: contextPath + "/order/findItemsByOrderNos",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($rootScope.orderNos)
        }).done(function (result) {
            if (result && result.success) {
                $scope.infoData = result.body;
                for (var i = 0; i < $scope.infoData.length; i++) {
                    $scope.infoData[i].subscribeItemQuantity = $scope.infoData[i].goodsQuantity;
                }
                $scope.$apply();
            }
        });
    });
    $scope.infoData = [];

    $scope.validate = $("#subscribeForm").validate();
    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        for (var i = 0; i < $scope.infoData.length; i++) {
            $scope.infoData[i].marketOrderNo = $scope.marketOrderNo;
            $scope.infoData[i].subscribeDeliveryNo = $scope.subscribeDeliveryNo;
            $scope.infoData[i].subscribeTime = $scope.subscribeTime;
        }

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/order/subscribe",
            data: JSON.stringify($scope.infoData)
        }).done(function (result) {
            if (result.success) {
                $("#subscribe").modal("hide");
                $scope.reset();
                App.toastr(result.message);
            }
            else
                App.toastr(result.message, "error");
        }).fail(function () {
            App.toastr("提交失败！");
        });
    }
});
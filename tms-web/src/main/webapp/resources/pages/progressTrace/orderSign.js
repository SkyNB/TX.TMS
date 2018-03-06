/*
'use strict';

var app = angular.module('RoadnetApp');

app.controller('OrderSignController', function ($scope, $rootScope) {
    if ($rootScope.orderNo)
        $("#orderSign").modal();

    $scope.orderSign = {
        signMan: $rootScope.consignee || '0',
        signManCard: '000000-00000000-0000',
        signNumber: 1,
        agentSignMan: '',
        agentSignManCard: '',
        signTime: new Date().format("yyyy-MM-dd HH:mm:ss"),
        feedbackSignTime: new Date().format("yyyy-MM-dd HH:mm:ss"),
        remark: ''
    };

    $scope.reset = function () {
        $scope.orderSign = {};
    }

    $scope.validate = $("#orderSignForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid())
            return;

        $scope.orderSign.orderNo = $rootScope.orderNo;

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/order/sign",
            data: JSON.stringify($scope.orderSign)
        }).done(function (result) {
            if (result.success) {
                $("#orderSign").modal("hide");
                //$scope.reset();
                $rootScope.data.query();
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败！", "warning");
        });

        $rootScope.orderNo = '';
        $rootScope.consignee = '';
    }
});*/

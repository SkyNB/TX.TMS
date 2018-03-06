'use strict';

var app = angular.module('RoadnetApp');

app.controller('ReceiptPicController', function ($scope, $rootScope, $stateParams) {

    $("#receiptPic").on("shown.bs.modal", function () {
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: contextPath + "/receipt/getOrderReceipt/" + $stateParams.customerCode + "/" + $stateParams.corderNo
        }).done(function (result) {
            if (result.success) {
                if (result.body && result.body.length>0) {
                    $scope.receiptPic = result.body;
                    $("#receiptPic").modal();
                    $scope.$apply();
                } else {
                    $scope.receiptPic = [];
                    $("#receiptPic").modal();
                    $scope.$apply();

                }
            } else {
                $("#receiptPic").modal();
                $scope.$apply();
                App.toastr(result.message, "error");
            }
        }).fail(function () {
            App.toastr("提交失败", "error");
        });

    });

    $scope.receiptPic = [];
});
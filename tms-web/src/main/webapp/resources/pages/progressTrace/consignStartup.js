angular.module('RoadnetApp').controller('ConsignOrderStartUpController', ['$rootScope', '$scope', '$state', '$stateParams', ConsignOrderStartUpController]);
function ConsignOrderStartUpController($rootScope, $scope, $state, $stateParams) {

    if ($rootScope.consignOrderIds) {
        $("#consignOrderStartUp").modal();
    }

    $("#consignOrderStartUp").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.startUpTime = new Date().format("yyyy-MM-dd HH:mm:ss");

    $scope.validate = $("#consignOrderStartUpForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;
        $.ajax({
            url: contextPath + "/consign/startUp",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({consignOrderIds: $rootScope.consignOrderIds, startUpTime: $scope.startUpTime})
        }).done(function (result) {
            if (result.success) {
                $("#consignOrderStartUp").modal("hide");
                toastr.success("启运成功!");
                $rootScope.data.query();
            } else {
                toastr.error("启运失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
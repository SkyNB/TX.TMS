angular.module('RoadnetApp').controller('ConsignOrderUpdateOrderNoController', ['$rootScope', '$scope', '$state', '$stateParams', ConsignOrderUpdateOrderNoController]);
function ConsignOrderUpdateOrderNoController($rootScope, $scope, $state, $stateParams) {

    if ($rootScope.consignOrderId) {
        $("#consignOrderUpdateOrderNo").modal();
    }

    $("#consignOrderUpdateOrderNo").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $("#consignOrderUpdateOrderNo").on("shown.bs.modal",function(){
        $scope.consignOrderNo = '';
        $scope.init();
        $scope.$apply();
    });

    $scope.init = function () {
        $.ajax({
            url: contextPath + "/consign/get/" + $rootScope.consignOrderId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            $scope.consign = result.body.consignOrder;
            if ($scope.consign) {
                $scope.temporaryConsignOrderNo = $scope.consign.consignOrderNo;
                $scope.$apply();
            }
        });
    };

    $scope.validate = $("#consignOrderUpdateOrderNoForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;
        $.ajax({
            url: contextPath + "/consign/updateOrderNo",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({consignOrderId: $rootScope.consignOrderId, consignOrderNo: $scope.consignOrderNo})
        }).done(function (result) {
            if (result.success) {
                $("#consignOrderUpdateOrderNo").modal("hide");
                toastr.success("替换成功!");
                $rootScope.data.query();
            } else {
                toastr.error("替换失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
angular.module('RoadnetApp').controller('ConsignOrderArriveController', ['$rootScope', '$scope', '$state', '$stateParams', ConsignOrderArriveController]);
function ConsignOrderArriveController($rootScope, $scope, $state, $stateParams) {

    if ($rootScope.consignOrderIds) {
        $("#consignOrderArrive").modal();
    }

    $("#consignOrderArrive").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.arriveTime = new Date().format("yyyy-MM-dd HH:mm:ss");

    $scope.validate = $("#consignOrderArriveForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;
        $.ajax({
            url: contextPath + "/consign/arrive",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({consignOrderIds: $rootScope.consignOrderIds, arriveTime: $scope.arriveTime})
        }).done(function (result) {
            if (result.success) {
                $("#consignOrderArrive").modal("hide");
                toastr.success("到达成功!");
                $rootScope.data.query();
            } else {
                toastr.error("到达失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
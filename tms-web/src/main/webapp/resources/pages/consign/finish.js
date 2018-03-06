angular.module('RoadnetApp').controller('ConsignOrderFinishController', ['$rootScope', '$scope', '$state', '$stateParams', ConsignOrderFinishController]);
function ConsignOrderFinishController($rootScope, $scope, $state, $stateParams) {

    if ($rootScope.consignOrderIds) {
        $("#consignOrderFinish").modal();
    }

    $("#consignOrderFinish").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.finishTime = new Date().format("yyyy-MM-dd HH:mm:ss");

    $scope.validate = $("#consignOrderFinishForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;
        $.ajax({
            url: contextPath + "/consign/finish",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({consignOrderIds: $rootScope.consignOrderIds, finishTime: $scope.finishTime})
        }).done(function (result) {
            if (result.success) {
                $("#consignOrderFinish").modal("hide");
                toastr.success("操作成功!");
                $rootScope.data.query();
            } else {
                toastr.error("操作失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
angular.module('RoadnetApp').controller('ConsignOrderCancelController', ['$rootScope', '$scope', '$state', '$stateParams', ConsignOrderCancelController]);
function ConsignOrderCancelController($rootScope, $scope, $state, $stateParams) {

    if ($rootScope.consignOrderId) {
        $("#consignOrderCancel").modal();
    }

    $("#consignOrderCancel").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.notes = '';

    $scope.validate = $("#consignOrderCancelForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;
        $.ajax({
            url: contextPath + "/consign/cancel",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({consignOrderId: $rootScope.consignOrderId, notes: $scope.notes})
        }).done(function (result) {
            if (result.success) {
                $("#consignOrderCancel").modal("hide");
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
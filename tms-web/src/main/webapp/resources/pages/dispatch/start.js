angular.module('RoadnetApp').controller('DispatchStartController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchStartController]);
function DispatchStartController($rootScope, $scope, $state, $stateParams) {

    if ($rootScope.dispatchId) {
        $("#dispatchStart").modal();
    }

    $("#dispatchStart").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.startTime = new Date().format("yyyy-MM-dd HH:mm:ss");

    $scope.validate = $("#dispatchStartForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;
        $.ajax({
            url: contextPath + "/dispatch/start",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({dispatchId: $rootScope.dispatchId, startTime: $scope.startTime})
        }).done(function (result) {
            if (result.success) {
                $("#dispatchStart").modal("hide");
                toastr.success("发车成功!");
                $rootScope.data.query();
            } else {
                toastr.error("发车失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
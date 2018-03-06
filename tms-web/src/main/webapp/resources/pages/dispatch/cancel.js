angular.module('RoadnetApp').controller('DispatchCancelController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchCancelController]);
function DispatchCancelController($rootScope, $scope, $state, $stateParams) {

    if($rootScope.dispatchId) {
        $("#dispatchCancel").modal();
    }

    $scope.validate = $("#dispatchCancelForm").validate();

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;
        var notes = $("#notes").val();
        $.ajax({
            url: contextPath + "/dispatch/cancel",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({dispatchId: $rootScope.dispatchId, notes: notes})
        }).done(function (result) {
            if (result.success) {
                $("#dispatchCancel").modal("hide");
                toastr.success("取消成功!");
                $rootScope.data.query();
            } else {
                toastr.error("取消失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
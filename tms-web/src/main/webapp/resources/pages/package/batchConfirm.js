angular.module('RoadnetApp').controller('BatchConfirmController', ['$rootScope', '$scope', '$state', BatchConfirmController]);

function BatchConfirmController($rootScope, $scope, $state) {
    if ($rootScope.selectedData && $rootScope.selectedData.length > 0) {
        $("#batchConfirm").modal();
    }
    $("#batchConfirm").on("shown.bs.modal", function () {
        var orderNos = [];
        $.each($rootScope.selectedData, function (i, item) {
            orderNos.push(item.orderNo);
        });
        $.ajax({
            url: contextPath + "/package/findOrderSummary",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(orderNos)
        }).done(function (result) {
            $scope.infoData = result;
            $scope.$apply();
        });
    });

    $("#batchConfirm").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.infoData = [];
    $scope.validate = $("#batchConfirmForm").validate();
    $scope.submit = function () {
        if (!$scope.validate.valid()) {
            return;
        }
        $scope.packingInfos = $scope.infoData;
        $.ajax({
            url: contextPath + "/package/batchConfirm",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.packingInfos)
        }).done(function (result) {
            if (result.success) {
                $rootScope.selectedData = [];
                $rootScope.lastPageData = [];
                $scope.package = {};
                $("#batchConfirm").modal("hide");
                if ($rootScope.isCreateDispatch) {
                    $rootScope.isCreateDispatch = false;
                    $state.go("create");
                    $("#addDispatch").modal("show");
                }else if($rootScope.isCreateConsign){
                    $rootScope.isCreateDispatch = false;
                    $state.go("create");
                    $("#createConsign").modal("show");
                }else if($rootScope.isBatchConsign){
                    $rootScope.isBatchConsign = false;
                    $state.go("batchConsign");
                    $("#batchConsign").modal("show");
                } else {
                    App.toastr(result.message, "success");
                    $rootScope.grid.clearSelection();
                    $rootScope.data.query();
                }
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function (r) {
            App.toastr("提交失败", "error");
        })
    }
}

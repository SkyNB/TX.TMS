angular.module('RoadnetApp').controller('DispatchUpdateFeeController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchUpdateFeeController]);
function DispatchUpdateFeeController($rootScope, $scope, $state, $stateParams) {

    $scope.dispatch = {};

    $scope.dispatchFeeDetailDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    feeAccountName: {type: "string", editable: false},
                    amount: {type: "number", validation: {min: 0, required: true}},
                    remark: {type: "string"}
                }
            }
        }
    });

    $scope.feeDetailColumns = [{
        field: "feeAccountName",
        title: "科目"
    }, {
        field: "amount",
        title: "金额"
    }, {
        field: "remark",
        title: "备注"
    }];

    $("#dispatchUpdateFee").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.init = function () {
        $.ajax({
            url: contextPath + "/dispatch/get/" + $rootScope.dispatchId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            if (!result.success) {
                return;
            }
            $scope.dispatch = result.body.dispatch;
            if (result.body.feeDetailDtos) {
                $scope.dispatchFeeDetailDataSource.data(result.body.feeDetailDtos);
            }
            $("#dispatchUpdateFee").modal();
            $scope.$apply();
        });
    };

    $scope.$watchCollection('dispatchFeeDetailDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalFee = 0;
        for (var i = 0; i < obj.length; i++) {
            totalFee += parseInt(obj[i].amount);
        }
        $scope.dispatch.totalFee = totalFee;
    });

    $scope.submit = function () {
        $.ajax({
            url: contextPath + "/dispatch/updateFee",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.dispatchFeeDetailDataSource.data())
        }).done(function (result) {
            if (result.success) {
                $("#dispatchUpdateFee").modal("hide");
                toastr.success("保存成功!");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.init();
}
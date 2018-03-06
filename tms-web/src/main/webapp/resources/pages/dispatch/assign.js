angular.module('RoadnetApp').controller('DispatchAssignController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchAssignController]);
function DispatchAssignController($rootScope, $scope, $state, $stateParams) {

    $scope.dispatch = {};
    $scope.reset = function () {
        $scope.dispatch = {};
    };
    if($rootScope.dispatchId){
        $("#dispatchAssign").modal();
    }
    $("#dispatchAssign").on("shown.bs.modal", function () {
        $scope.init();
    });
    $scope.init = function(){
        if($rootScope.dispatchId) {
            $.ajax({
                url: contextPath + "/dispatch/get/" + $rootScope.dispatchId,
                type: "POST",
                contentType: "application/json"
            }).done(function (result) {
                $scope.dispatch = result.body.dispatch;
                if ($scope.dispatch) {
                    $("#oldDriver").val($scope.dispatch.driver);
                    $("#oldVehicleNumber").val($scope.dispatch.vehicleNumber);
                    $scope.dispatch.vehicleNumber = '';
                    $scope.dispatch.driver = '';
                    $scope.dispatch.driverPhone = '';
                    $scope.dispatch.vehicleTypeId = '';
                    $scope.dispatch.vehicleId = '';
                    $scope.$apply();
                }
            });
        }
    };
    $scope.vehicleDataSource = new kendo.data.DataSource({
        serverFiltering: false,
        transport: {
            read: {
                dataType: "json",
                url: contextPath + "/vehicle/getAvailableForSelect"
            }
        }
    });

    $scope.vehicleOptions = {
        dataSource: $scope.vehicleDataSource,
        filter: "contains",
        dataTextField: "text",
        dataValueField: "value",
        change: function (e) {
            var value = this.value();
            var exists = $.grep($scope.vehicleDataSource.data(), function (v) {
                return v.value === value;
            });
            if (exists.length <= 0) {
                this.text('');
                this.value('');
            }
        }
    };
    $scope.validate = $("#dispatchAssignForm").validate();

    $scope.vehicleChange = function () {
        var vehicleId = $scope.dispatch.vehicleId;
        if (vehicleId && vehicleId != '') {
            $.ajax({
                type: "POST",
                contentType: "application/json",
                url: contextPath + "/vehicle/get/" + vehicleId
            }).done(function (result) {
                if (result && result.success) {
                    $scope.dispatch.vehicleNumber = result.body.vehicleNo;
                    $scope.dispatch.driver = result.body.driver;
                    $scope.dispatch.driverPhone = result.body.driverMobile;
                    $scope.dispatch.vehicleTypeId = result.body.vehicleTypeId;
                }
                $scope.$apply();
            });
        } else {
            $scope.dispatch.vehicleNumber = '';
            $scope.dispatch.driver = '';
            $scope.dispatch.driverPhone = '';
            $scope.dispatch.vehicleTypeId = '';
        }
    };
    $scope.submit = function () {
        if (!$scope.validate.valid()) return;

        $.ajax({
            url: contextPath + "/dispatch/assign",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({dispatchId:$scope.dispatch.dispatchId,vehicleNumber:$scope.dispatch.vehicleNumber,
                driver:$scope.dispatch.driver,driverPhone:$scope.dispatch.driverPhone,vehicleTypeId:$scope.dispatch.vehicleTypeId,
                vehicleId:$scope.dispatch.vehicleId})
        }).done(function (result) {
            if (result.success) {
                $("#dispatchAssign").modal("hide");
                toastr.success("保存成功!");
                $scope.reset();
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
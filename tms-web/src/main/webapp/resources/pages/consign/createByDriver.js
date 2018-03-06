angular.module('RoadnetApp').controller('ConsignCreateByDriverController', ['$rootScope', '$scope', '$state', ConsignCreateByDriverController]);
function ConsignCreateByDriverController($rootScope, $scope, $state) {

    $("#createByDriver").modal("show");
    $scope.vehicleId = '';

    $scope.createByDriverData = getDataSource("orderNo", contextPath + "/consign/createByDriverQuery");

    $scope.search = function () {
        $scope.createByDriverData.filter($("#createByDriverForm").serializeArray());
    };

    $scope.createByDriverDataBound = function () {
        $scope.createByDriverGrid = $("#createByDriverGrid").data("kendoExGrid");
    };

    $("#createByDriver").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.createByDriverGridOptions = {
        dataSource: $scope.createByDriverData,
        columns: [{
            field: "orderNo",
            title: "单号",
            width: 150
        }, {
            field: "customerOrderNo",
            title: "客户单号",
            width: 100
        }, {
            field: "customerName",
            title: "客户",
            width: 100
        }, {
            field: "destCityName",
            title: "目的城市",
            width: 100
        }, {
            field: "totalPackageQty",
            title: "总箱数",
            width: 80
        }, {
            field: "totalVolume",
            title: "总体积",
            width: 80
        }, {
            field: "totalWeight",
            title: "总重量",
            width: 80
        }]
    };

    $("#createByDriver").on("shown.bs.modal", function () {
        if ($rootScope.createSelectedVehicleId) {
            $scope.vehicleId = $rootScope.createSelectedVehicleId;
            $("#searchvehicleId").val($scope.vehicleId);
            $scope.$apply();
            $rootScope.createSelectedVehicleId = '';
            $scope.createByDriverData.filter($("#createByDriverForm").serializeArray());
        }
    });

    $scope.select = function () {
        var rows = $scope.createByDriverGrid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        $rootScope.isCreateByDriver = true;
        $rootScope.selectOrders = rows;
        $rootScope.createSelectedVehicleId = $scope.vehicleId;
        $state.go("create");
        $("#createConsign").modal("show");
    };
}
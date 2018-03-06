var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('mergePackage', {
            url: '/mergePackage',
            reload: true,
            templateUrl: contextPath + '/package/mergePackage',
            controller: "MergePackageController",
            resolve: getDeps([contextPath + '/resources/pages/package/mergePackage.js'])
        })
        .state('batchConfirm', {
            url: '/batchConfirm',
            reload: true,
            templateUrl: contextPath + '/package/batchConfirm',
            controller: "BatchConfirmController",
            resolve: getDeps([contextPath + '/resources/pages/package/batchConfirm.js'])
        })
        .state('update/:orderId', {
            url: '/update/:orderId',
            reload: true,
            templateUrl: contextPath + '/order/updateItOrder',
            controller: "OrderUpdateController",
            resolve: getDeps([contextPath + '/resources/pages/order/updateItOrder.js'])
        })
});
app.controller('OrderController', ['$scope', '$rootScope', '$state', OrderController]);
function OrderController($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchOrder").serializeArray());
    };
    $scope.dataBinding = function () {
        if ($rootScope.grid) {
            $.each($rootScope.grid.getSelectedData(), function (i, item) {
                var select = false;
                for (var l = 0; l < $rootScope.lastPageData.length; l++) {
                    if ($rootScope.lastPageData[l].orderId === item.orderId) {
                        select = true;
                    }
                }
                if (!select) {
                    $rootScope.lastPageData.push(item);
                }
            });
            $rootScope.$apply();
            App.resizeGrid();
        }
    };
    $rootScope.selectedData = [];
    $rootScope.lastPageData = [];
    $scope.batchConfirm = function () {

        $rootScope.selectedData = [];
        $.each($rootScope.grid.getSelectedData(), function (i, item) {
            var select = false;
            for (var l = 0; l < $rootScope.lastPageData.length; l++) {
                if ($rootScope.lastPageData[l].orderId === item.orderId) {
                    select = true;
                }
            }
            if (!select) {
                $rootScope.selectedData.push(item);
            }
        });
        if ($rootScope.selectedData.length < 1) {
            App.toastr("请选择订单", "warning");
            return;
        }
        $state.go("batchConfirm");
        $("#batchConfirm").modal("show");
    };
    $scope.mergePackage = function () {
        $rootScope.selectedData = [];
        $.each($rootScope.grid.getSelectedData(), function (i, item) {
            var select = false;
            for (var l = 0; l < $rootScope.lastPageData.length; l++) {
                if ($rootScope.lastPageData[l].orderId === item.orderId) {
                    select = true;
                }
            }
            if (!select) {
                $rootScope.selectedData.push(item);
            }
        });
        if ($rootScope.selectedData.length < 1) {
            App.toastr("请选择订单", "warning");
            return;
        }
        $state.go("mergePackage");
        $("#mergePackage").modal("show");
    };
    $scope.remove = function (orderId) {
        $rootScope.lastPageData = $.grep($rootScope.lastPageData, function (item) {
            return item.orderId !== orderId;
        });
    };
    $rootScope.data = getDataSource("orderNo", contextPath + "/package/searchForPackage");
    $rootScope.orderPackageData = getDataSource("orderNo", contextPath + "/package/pageOrderPacking");
    $rootScope.gridPackOptions = {
        dataSource: $scope.orderPackageData, dataBound: function () {
            $rootScope.packageGrid = $("#gridOrderPackage").data("kendoExGrid");
        }, columns: [{
            field: "orderNo", title: "单号", width: 120, /*template: function (dataItem) {
             return "<a href='#' data-toggle='popover'>" + dataItem.orderNo + "</a> " + orderPopover(dataItem);
             }*/
        }, {
            field: "packageQty", title: "箱数"
        }, {
            field: "weight", title: "重量"
        }, {
            field: "volume", title: "体积"
        }, {
            field: "packageNo", title: "箱号", width: 120
        }, {
            field: "packageSize", title: "箱型", template: function (dataItem) {
                if (dataItem.packageSize) {
                    return dataItem.packageSize.text;
                }
                return "";
            }
        }, {
            field: "wrapMaterial", title: "包装材料", template: function (dataItem) {
                if (dataItem.wrapMaterial) {
                    return dataItem.wrapMaterial.text;
                }
                return "";
            }
        }, {
            field: "goodsDesc", title: "货物描述"
        }]
    };
    $scope.packageShow = function () {
        $scope.data.query();
    }
    $scope.packageSearch = function () {
        $scope.orderPackageData.filter($("#fromPackageSearch").serializeArray());
    };
    $rootScope.gridOptions = {
        dataSource: $scope.data, dataBinding: $scope.dataBinding, dataBound: function () {
            $rootScope.grid = $("#gridOrder").data("kendoExGrid");
        }, columns: [{
            field: "orderNo", title: "单号", width: 120
        }, {
            field: "customerOrderNo", title: "客户单号", width: 120
        }, {
            field: "orderType.text", title: "订单类型"
        }, {
            field: "orderDate", title: "订单日期"
        }, {
            field: "appointConsignDate", title: "指定发运日期"
        }, {
            field: "deliveryContacts", title: "收货人"
        }, {
            field: "deliveryAddress", title: "收货地址"
        }, {
            field: "deliveryCity", title: "目的城市"
        }, {
            field: "totalPackageQty", title: "总箱数"
        }, {
            field: "totalItemQty", title: "总数量"
        }, {
            field: "totalWeight", title: "总重量"
        }, {
            field: "totalVolume", title: "总体积"
        }, {
            field: "packageStatus.text", title: "打包状态"
        }, {
            field: "remark", title: "备注"
        }]
    };
}


var app = angular.module('RoadnetApp');
app.config(function ($stateProvider) {
    $stateProvider
        .state('details/:packageId', {
            url: '/details/:packageId',
            reload: true,
            templateUrl: contextPath + '/package/details',
            controller: "PackageDetailsController",
            resolve: getDeps([contextPath + '/resources/pages/package/details.js'])
        })
});
app.controller('PackageController', ['$scope', '$rootScope', PackageController]);

function PackageController($scope, $rootScope) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchPackage").serializeArray());
    };
    $rootScope.data = getDataSource("packageId", contextPath + "/package/pagePackage");
    $scope.gridOptions = {
        dataSource: $scope.data, dataBound: function () {
            $scope.grid = $("#gridPackage").data("kendoExGrid");
        }, columns: [{
            field: "packageNo", title: "箱号", width: 200, template: function (dataItem) {
                return "<a href='#/details/" + dataItem.packageId + "' data-target='#detailsPackage' data-toggle='modal'>" + dataItem.packageNo + "</a>";
            }
        }, {
            field: "orderNo", title: "订单号"
        }, /* {
         field: "packageQty", title: "箱数"
         },*/ {
            field: "weight", title: "重量"
        }, {
            field: "volume", title: "体积"
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
        }, {
            field: "packingUserName", title: "打包人"
        }, {
            field: "packingTime", title: "打包时间"
        }]
    };
}


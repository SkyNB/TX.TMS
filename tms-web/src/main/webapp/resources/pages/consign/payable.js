var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {

});

app.controller('ConsignController', ['$scope', '$rootScope', '$state', ConsignController]);
function ConsignController($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchConsign").serializeArray());
    };

    $rootScope.data = getDataSource("consignOrderId", contextPath + "/consign/searchPayable");

    $scope.dataBound = function () {
        $scope.grid = $("#gridConsign").data("kendoExGrid");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "consignOrderNo",
            title: "托运单号",width:120
        }, {
            field: "carrierName",
            title: "承运商",width:150
        }, {
            field: "consignTime",
            title: "发运时间",width:150
        },{
            field: "transportType",
            title: "运输方式",
            values: transportTypes,width:120
        }, {
            field: "destCityName",
            title: "目的地",width:150
        }, {
            field: "goodsName",
            title: "货物名称",width:150
        }, {
            field: "consignee",
            title: "收货人",width:150
        }, {
            field: "totalVolume",
            title: "总体积",width:150
        }, {
            field: "totalPackageQuantity",
            title: "总箱数",width:150
        }, {
            field: "transport",
            title: "运费",width:80
        }, {
            field: "send",
            title: "送货费",width:80
        }, {
            field: "upstairs",
            title: "上楼费",width:80
        }, {
            field: "pickup",
            title: "提货费",width:80
        }, {
            field: "other",
            title: "其他费用",width:80
        }, {
            field: "inspection",
            title: "检货费",width:80
        }, {
            field: "unloading",
            title: "卸车费",width:80
        }, {
            field: "receipt",
            title: "回单费",width:80
        }, {
            field: "totalPayable",
            title: "总应付",width:80
        }]
    };
}

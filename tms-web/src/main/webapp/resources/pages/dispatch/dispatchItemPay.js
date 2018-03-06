var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
});

app.controller('DispatchController', ['$scope', '$rootScope', '$state', DispatchController]);
function DispatchController($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchDispatch").serializeArray());
    };

    $rootScope.data = getDataSource("dispatchId", contextPath + "/dispatch/searchDispatchItemPay");

    $scope.dataBound = function () {
        $scope.grid = $("#gridDispatch").data("kendoExGrid");
    };

    $scope.gridOptions = {
        dataSource: $scope.data, toolbar: ['excel'], excel: {
            fileName: "调度明细表.xlsx", allPages: true, filterable: true
        }, columns: [{
            field: "dispatchNumber", title: "派车单号",  width: 150
        }, {
            field: "vehicleNumber", title: "车牌号", width: 120
        }, {
            field: "driver", title: "司机", width: 120
        }, {
            field: "status.text", title: "状态", width: 120
        }, {
            field: "totalPackageQuantity", title: "总箱数", width: 100
        }, {
            field: "createUserName", title: "调度员", width: 150
        }, {
            field: "startAddress", title: "始发地", width: 150
        }, {
            field: "destAddress", title: "目的地", width: 200
        },{
            field: "expectFinishTime",
            title: "预完成时间",
            width: 120
        }, {
            field: "createdDate",
            title: "派车时间",
            width: 120
        },{
            field: "startDate",
            title: "发车时间",
            width: 120
        }, {
            field: "finishedDate",
            title: "回场时间",
            width: 120
        }, {
            field: "leverMan",
            title: "跟车人员",
            width: 90
        }, {
            field: "remark",
            title: "备注",
            width: 90
        }, {
            field: "rental",
            title: "租车费",
            width: 90
        }, {
            field: "waiting",
            title: "等待费",
            width: 90
        }, {
            field: "parking",
            title: "停车费",
            width: 90
        }, {
            field: "bridge",
            title: "路桥费",
            width: 90
        },{
            field: "other",
            title: "其他费",
            width: 90
        }, {
            field: "totalPayable",
            title: "合计",
            width: 90
        },{
            field: "status.text",
            title: "状态",
            width: 90
        }, {
            field: "modifyUserName",
            title: "修改人",
            width: 90
        }, {
            field: "modifiedDate",
            title: "修改时间",
            width: 120
        },{
            field: "customerName",
            title: "客户",
            width: 90
        },{
            field: "orderNo",
            title: "单号",
            width: 90
        }, {
            field: "customerOrderNo",
            title: "客户单号",
            width: 90
        }, {
            field: "controlType",
            title: "调度类型",
            width: 90
        }, {
            field: "destinationName",
            title: "订单目的城市",
            width: 90
        },{
            field: "volume",
            title: "派车体积",
            width: 90
        }, {
            field: "weight",
            title: "派车重量",
            width: 90
        }, {
            field: "packageQuantity",
            title: "派车箱数",
            width: 90
        }, {
            field: "deliveryContacts",
            title: "收货人",
            width: 90
        }, {
            field: "deliveryAddress",
            title: "收货地址",
            width: 90
        }, {
            field: "orderCreateTime",
            title: "订单创建日期",
            width: 90
        }, {
            field: "transferNo", title: "调配单号", width: 90
        }]
    };
}

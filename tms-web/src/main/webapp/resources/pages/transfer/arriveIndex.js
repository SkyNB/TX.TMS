var app = angular.module('RoadnetApp');

app.controller('TransferArriveController', ['$scope', '$rootScope', '$state', TransferArriveController]);
app.config(function ($stateProvider) {
    $stateProvider
        .state('arrive', {
            url: '/arrive',
            reload: true,
            templateUrl: contextPath + '/transfer/arrive',
            controller: "TransferArriveController",
            resolve: getDeps([contextPath + '/assets/pages/transfer/arrive.js'])
        })
        .state('enterReceiptInfo', {
            url: '/enterReceiptInfo',
            reload: true,
            templateUrl: contextPath + '/transfer/enterReceiptInfo',
            controller: "TransferEnterReceiptInfoController",
            resolve: getDeps([contextPath + '/assets/pages/transfer/enterReceiptInfo.js'])
        })
});
function TransferArriveController($scope, $rootScope, $state) {

    $scope.search = function () {
        $scope.data.filter($("#transferSearchForm").serializeArray());
    };

    $rootScope.data = getDataSource("transferId", contextPath + "/transfer/arriveSearch");

    $scope.dataBound = function () {
        $scope.grid = $("#transferGrid").data("kendoExGrid");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "transferNumber",
            title: "调配单号",
            width: 120
        }, {
            field: "orderOrgName",
            title: "发货分支",
            width: 100
        }, {
            field: "orderSiteName",
            title: "发货站点",
            width: 100
        }, {
            field: "orderNo",
            title: "单号",
            width: 150
        }, {
            field: "customerName",
            title: "客户",
            width: 100
        }, {
            field: "customerOrderNo",
            title: "客户单号",
            width: 100
        }, {
            field: "orderDate",
            title: "订单日期",
            width: 100,
            template: function (dataItem) {
                if (dataItem.orderDate) {
                    return kendo.toString(kendo.parseDate(dataItem.orderDate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd");
                }
                return "";
            }
        }, {
            field: "carrierName",
            title: "承运商",
            width: 100
        }, {
            field: "consignOrderNo",
            title: "托运单号",
            width: 100
        }, {
            field: "volume",
            title: "体积",
            width: 100
        }, {
            field: "weight",
            title: "重量",
            width: 100
        }, {
            field: "packageQuantity",
            title: "箱数",
            width: 100
        }, {
            field: "receiptPageNumber",
            title: "回单页数",
            width: 100
        }, {
            field: "dispatchType",
            title: "调配调度类型",
            width: 120,
            values: dispatchTypeEnum
        }, {
            field: "status.text",
            title: "状态",
            width: 100
        }, {
            field: "createDate",
            title: "发货时间",
            width: 100
        }, {
            field: "arriveTime",
            title: "到货时间",
            width: 100
        }, {
            field: "arriveRemark",
            title: "到货备注",
            width: 100
        }, {
            field: "receiptInfo",
            title: "回单状况",
            width: 100
        }, {
            field: "receiptPostDate",
            title: "回单寄出时间",
            width: 100
        }, {
            field: "deliveryCity",
            title: "目的城市",
            width: 100
        }, {
            field: "deliveryCompany",
            title: "收货单位",
            width: 100
        }, {
            field: "deliveryContacts",
            title: "收货人",
            width: 100
        }, {
            field: "deliveryContactPhone",
            title: "收货人电话",
            width: 120
        }, {
            field: "deliveryAddress",
            title: "收货地址",
            width: 100
        }, {
            field: "goodsName",
            title: "货物名称",
            width: 100
        }, {
            field: "transportType",
            title: "运输方式",
            width: 100,
            values: transportTypeEnum
        }, {
            field: "handoverType",
            title: "交接方式",
            width: 100,
            values: handoverTypeEnum
        }, {
            field: "carrierTelephone",
            title: "查货电话",
            width: 150
        }]
    };

    $scope.arrive = function () {
        var rows = $scope.grid.getSelectedData();
        var exceptionOrderNos = '';
        if (rows && rows.length > 0) {
            for (var i = 0; i < rows.length; i++) {
                if (rows[i].status.value != 'NOT_ARRIVED') {
                    exceptionOrderNos += (rows[i].orderNo + ',');
                }
            }
        }
        if (exceptionOrderNos.length > 0) {
            App.toastr("请选择未到货的订单，订单" + exceptionOrderNos.substring(0, exceptionOrderNos.length - 1) + "已到货！", "warning");
            return;
        }
        $rootScope.selectedTransfer = rows;
        $state.go("arrive");
        $("#transferArrive").modal();
    };

    $scope.enterReceiptInfo = function () {
        var rows = $scope.grid.getSelectedData();
        var exceptionOrderNos = '';
        if (rows && rows.length > 0) {
            for (var i = 0; i < rows.length; i++) {
                if (rows[i].status.value == 'NOT_ARRIVED') {
                    exceptionOrderNos += (rows[i].orderNo + ',');
                }
            }
        }
        if (exceptionOrderNos.length > 0) {
            App.toastr("请选择已到货的订单，订单" + exceptionOrderNos.substring(0, exceptionOrderNos.length - 1) + "未到货！", "warning");
            return;
        }
        $rootScope.selectedTransfer = rows;
        $state.go("enterReceiptInfo");
        $("#transferEnterReceiptInfo").modal();
    };
}

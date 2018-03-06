var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('select', {
            url: '/select',
            reload: true,
            templateUrl: contextPath + '/consign/selectOrder',
            controller: "SelectOrderController",
            resolve: getDeps([contextPath + '/resources/pages/consign/select.js'])
        })
        .state('create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/consign/create',
            controller: "ConsignCreateController",
            resolve: getDeps([contextPath + '/resources/pages/consign/create.js'])
        })
        .state('detail/:consignOrderId', {
            url: '/detail/:consignOrderId',
            reload: true,
            templateUrl: contextPath + '/consign/detail',
            controller: "ConsignDetailController",
            resolve: getDeps([contextPath + '/resources/pages/consign/detail.js'])
        })
        .state('update', {
            url: '/update',
            reload: true,
            templateUrl: contextPath + '/consign/update',
            controller: "ConsignOrderUpdateController",
            resolve: getDeps([contextPath + '/resources/pages/consign/update.js'])
        })
        .state('consign', {
            url: '/consign',
            reload: true,
            templateUrl: contextPath + '/consign/consign',
            controller: "ConsignOrderConsignController",
            resolve: getDeps([contextPath + '/resources/pages/consign/consign.js'])
        })
        .state('arrive', {
            url: '/arrive',
            reload: true,
            templateUrl: contextPath + '/consign/arrive',
            controller: "ConsignOrderArriveController",
            resolve: getDeps([contextPath + '/resources/pages/consign/arrive.js'])
        })
        .state('finish', {
            url: '/finish',
            reload: true,
            templateUrl: contextPath + '/consign/finish',
            controller: "ConsignOrderFinishController",
            resolve: getDeps([contextPath + '/resources/pages/consign/finish.js'])
        })
        .state('cancel', {
            url: '/cancel',
            reload: true,
            templateUrl: contextPath + '/consign/cancel',
            controller: "ConsignOrderCancelController",
            resolve: getDeps([contextPath + '/resources/pages/consign/cancel.js'])
        })
        .state('updateOrderNo', {
            url: '/updateOrderNo',
            reload: true,
            templateUrl: contextPath + '/consign/updateOrderNo',
            controller: "ConsignOrderUpdateOrderNoController",
            resolve: getDeps([contextPath + '/resources/pages/consign/updateOrderNo.js'])
        })
        .state('batchConfirm', {
            url: '/batchConfirm',
            reload: true,
            templateUrl: contextPath + '/package/batchConfirm',
            controller: "BatchConfirmController",
            resolve: getDeps([contextPath + '/resources/pages/package/batchConfirm.js'])
        })
});

app.controller('ConsignController', ['$scope', '$rootScope', '$state', ConsignController]);
function ConsignController($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchConsign").serializeArray());
    };

    $rootScope.data = getDataSource("consignOrderId", contextPath + "/consign/search");

    $scope.dataBound = function () {
        $scope.grid = $("#gridConsign").data("kendoExGrid");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "consignOrderNo",
            title: "托运单号",
            template: function (dataItem) {
                return "<a href='#/detail/" + dataItem.consignOrderId + "' data-target='#consignOrderDetail' data-toggle='modal'>" + dataItem.consignOrderNo + "</a>";
            }
        }, {
            field: "carrierName",
            title: "承运商"
        }, {
            field: "status.text",
            title: "状态"
        }, {
            field: "transportType",
            title: "运输方式",
            values: transportTypes
        }, {
            field: "destCityName",
            title: "目的地"
        }, {
            field: "consignee",
            title: "收货人"
        }, {
            field: "totalVolume",
            title: "总体积"
        }, {
            field: "totalPackageQuantity",
            title: "总箱数"
        }, {
            field: "createUserName",
            title: "发货人"
        }]
    };

    $scope.select = function () {
        $state.go("select");
        $("#selectOrder").modal();
    };

    $scope.consign = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一个托运单", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (!status || status != 'NEW') {
            App.toastr("托运单已发运或已取消，不能操作发运！", "warning");
            return;
        }
        $rootScope.consignOrderId = rows[0].consignOrderId;
        $state.go("consign");
        $("#consignOrderConsign").modal();
    };

    $scope.update = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要修改的一个托运单", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (!status || status != 'NEW') {
            App.toastr("托运单已发运或已取消，不能操作修改！", "warning");
            return;
        }
        $rootScope.consignOrderId = rows[0].consignOrderId;
        $state.go("update");
        $("#consignOrderUpdate").modal();
    };

    $scope.arrive = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        var consignOrderIds = [];
        var statusExceptionOrderNo = '';
        $.each(rows, function (i, row) {
            var status = row.status.value;
            if (!status || status != 'IN_TRANSIT') {
                statusExceptionOrderNo += (row.consignOrderNo + ",");
            }
            consignOrderIds.push(row.consignOrderId);
        });
        if (statusExceptionOrderNo && statusExceptionOrderNo != '') {
            App.toastr("托运单" + statusExceptionOrderNo.substr(0, statusExceptionOrderNo.length - 1) + "状态有误，请选择在途的托运单！", "warning");
            return;
        }
        $rootScope.consignOrderIds = consignOrderIds;
        $state.go("arrive");
        $("#consignOrderArrive").modal();
    };

    $scope.finish = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        var consignOrderIds = [];
        var statusExceptionOrderNo = '';
        $.each(rows, function (i, row) {
            var status = row.status.value;
            if (!status || (status != 'IN_TRANSIT' && status != 'ARRIVED')) {
                statusExceptionOrderNo += (row.consignOrderNo + ",");
            }
            consignOrderIds.push(row.consignOrderId);
        });
        if (statusExceptionOrderNo && statusExceptionOrderNo != '') {
            App.toastr("托运单" + statusExceptionOrderNo.substr(0, statusExceptionOrderNo.length - 1) + "状态有误，请选择在途或到达的托运单！", "warning");
            return;
        }
        $rootScope.consignOrderIds = consignOrderIds;
        $state.go("finish");
        $("#consignOrderFinish").modal();
    };

    $scope.cancel = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要取消的一个托运单", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (!status || status != 'NEW') {
            App.toastr("托运单已发运或已取消，不能操作取消！", "warning");
            return;
        }
        $rootScope.consignOrderId = rows[0].consignOrderId;
        $state.go("cancel");
        $("#consignOrderCancel").modal();
    };

    $scope.updateOrderNo = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要替换临时单号的一个托运单", "warning");
            return;
        }
        var isTemporaryNo = rows[0].isTemporaryNo;
        if (!isTemporaryNo) {
            App.toastr("请选择单号为临时单号的托运单！", "warning");
            return;
        }
        $rootScope.consignOrderId = rows[0].consignOrderId;
        $state.go("updateOrderNo");
        $("#consignOrderUpdateOrderNo").modal();
    };
}

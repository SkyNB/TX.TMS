var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/dispatch/create',
            controller: "DispatchCreateController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/create.js'])
        })
        .state('detail/:dispatchId', {
            url: '/detail/:dispatchId',
            reload: true,
            templateUrl: contextPath + '/dispatch/detail',
            controller: "DispatchDetailController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/detail.js'])
        })
        .state('addOrder', {
            url: '/addOrder',
            reload: true,
            templateUrl: contextPath + '/dispatch/addOrder',
            controller: "DispatchAddOrderController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/addOrder.js'])
        })
        .state('removeOrder', {
            url: '/removeOrder',
            reload: true,
            templateUrl: contextPath + '/dispatch/removeOrder',
            controller: "DispatchRemoveOrderController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/removeOrder.js'])
        })
        .state('updateFee', {
            url: '/updateFee',
            reload: true,
            templateUrl: contextPath + '/dispatch/updateFee',
            controller: "DispatchUpdateFeeController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/updateFee.js'])
        })
        .state('assign', {
            url: '/assign',
            reload: true,
            templateUrl: contextPath + '/dispatch/assign',
            controller: "DispatchAssignController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/assign.js'])
        })
        .state('loading', {
            url: '/loading',
            reload: true,
            templateUrl: contextPath + '/dispatch/loading',
            controller: "DispatchLoadingController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/loading.js'])
        })
        .state('cancel', {
            url: '/cancel',
            reload: true,
            templateUrl: contextPath + '/dispatch/cancel',
            controller: "DispatchCancelController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/cancel.js'])
        })
        .state('finish', {
            url: '/finish',
            reload: true,
            templateUrl: contextPath + '/dispatch/finish',
            controller: "DispatchFinishController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/finish.js'])
        })
        .state('start', {
            url: '/start',
            reload: true,
            templateUrl: contextPath + '/dispatch/start',
            controller: "DispatchStartController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/start.js'])
        })
});

app.controller('DispatchController', ['$scope', '$rootScope', '$state', DispatchController]);
function DispatchController($scope, $rootScope, $state) {
    $scope.search = function () {
        $scope.data.filter($("#fromSearchDispatch").serializeArray());
    };

    $rootScope.data = getDataSource("dispatchId", contextPath + "/dispatch/search");

    $scope.getSelectIds = function () {
        var row = $scope.grid.selectedData();
        if (row.length <= 0) {
            App.toastr("请选择数据", "warning");
            return null;
        }
        var dispatchIds = [];
        var rows = $scope.grid.select();
        rows.each(function (i, row) {
            var dataItem = $scope.grid.dataItem(row);
            if (dataItem) {
                dispatchIds.push(dataItem.dispatchId);
            }
        });
        return dispatchIds;
    };

    $scope.dataBound = function () {
        $scope.grid = $("#gridDispatch").data("kendoExGrid");
    };

    $scope.addOrders = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status == 'CANCELED' || status == 'FINISHED') {
            App.toastr("派车单已完成或已取消，不能操作加单", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("addOrder");
        $("#dispatchAddOrder").modal("show");
    };

    $scope.updateFee = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status == 'CANCELED' || status == 'FINISHED') {
            App.toastr("派车单已完成或已取消，不能修改费用！", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("updateFee");
        $("#dispatchUpdate").modal("show");
    };

    $scope.dataBound = function () {
        $scope.grid = $("#gridDispatch").data("kendoExGrid");
    };

    $scope.removeOrders = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status == 'CANCELED' || status == 'FINISHED') {
            App.toastr("派车单已完成或已取消，不能操作减单", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("removeOrder");
        $("#dispatchRemoveOrder").modal("show");
    };

    $scope.create = function () {
        $state.go("create");
        $("#addDispatch").modal("show");
    };

    $scope.assign = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status != 'NEW' && status != 'ASSIGNED') {
            App.toastr("请选择未派车或已派车司机未接受的派车单", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("assign");
        $("#dispatchAssign").modal("show");
    };

    $scope.loading = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status != 'ASSIGNED' && status != 'ACCEPT' && status != 'LOADING') {
            App.toastr("请选择已派车、已接受或装车中的派车单", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("loading");
        $("#dispatchLoading").modal("show");
    };

    $scope.loaded = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status != 'LOADING') {
            App.toastr("请选择装车中的派车单", "warning");
            return;
        }
        var dispatchId = rows[0].dispatchId;
        $.ajax({
            url: contextPath + "/dispatch/get/" + dispatchId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            if (result.success) {
                var items = result.body.dispatch.items;
                var notLoadedOrderNos = '';
                $.each(items, function (index, item) {
                    if (!item.isLoaded) {
                        notLoadedOrderNos += item.orderNo + ",";
                    }
                });
                if (notLoadedOrderNos != '' && notLoadedOrderNos.length > 0) {
                    if (!confirm("单号" + notLoadedOrderNos.substr(0, notLoadedOrderNos.length - 1) + "未装车，是否确认完成装车？"))return;
                    $.ajax({
                        url: contextPath + "/dispatch/finishLoading/" + dispatchId,
                        type: "POST",
                        contentType: "application/json"
                    }).done(function (result) {
                        if (result.success) {
                            toastr.success("操作成功!");
                            $rootScope.data.query();
                        } else {
                            toastr.error("操作失败！" + result.message);
                        }
                    }).fail(function () {
                        App.toastr("数据提交失败!", "error");
                    });
                } else {
                    toastr.success("已完成装车!");
                    $rootScope.data.query();
                }
            }
        });
    };

    $scope.start = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status != 'ASSIGNED' && status != 'ACCEPT' && status != 'LOADING' && status != 'LOADED') {
            App.toastr("请选择完成已指派司机且未发车的派车单", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("start");
        $("#dispatchStart").modal();
    };

    $scope.finish = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status != 'INTRANSIT') {
            App.toastr("请选择在途中的派车单", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("finish");
        $("#dispatchFinish").modal("show");
    };

    $scope.cancel = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        if (rows.length > 1) {
            App.toastr("请选择要操作的一条数据", "warning");
            return;
        }
        var status = rows[0].status.value;
        if (status == 'FINISHED' || status == 'CANCELED') {
            App.toastr("派车单已完成或已取消，不能操作取消", "warning");
            return;
        }
        $rootScope.dispatchId = rows[0].dispatchId;
        $state.go("cancel");
        $("#dispatchCancel").modal("show");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        toolbar: ['excel'], excel: {
            fileName: "派车单.xlsx",
            allPages: true,
            filterable: true
        },
        columns: [{
            field: "dispatchNumber",
            title: "派车单号",
            template: function (dataItem) {
                return "<a href='#/detail/" + dataItem.dispatchId + "' data-target='#dispatchDetail' data-toggle='modal'>" + dataItem.dispatchNumber + "</a>";
            },
            width: 150
        }, {
            field: "vehicleNumber",
            title: "车牌号",
            width: 120
        }, {
            field: "driver",
            title: "司机",
            width: 120
        }, {
            field: "status.text",
            title: "状态",
            width: 120
        }, {
            field: "totalFee",
            title: "费用",
            width: 100
        }, {
            field: "totalPackageQuantity",
            title: "总箱数",
            width: 100
        }, {
            field: "totalVolume",
            title: "总体积",
            width: 100
        }, {
            field: "createUserName",
            title: "调度员",
            width: 150
        }, {
            field: "startAddress",
            title: "始发地",
            width: 150
        }, {
            field: "destAddress",
            title: "目的地",
            width: 200
        }]
    };
    $scope.trucking = function () {

        var rows = $scope.grid.select();
        if (rows.length == 0) {
            App.toastr("请选择派车单!", "warning");
            return;
        }
        var orderNos = $scope.grid.getSelectedId();
        var dataItem = $scope.grid.getSelectedData()[0];
        $scope.createPage(orderNos, dataItem);

    };

    $scope.createPage = function (selectedIds, dataItem) {
        var LODOP = getLodop();
        LODOP.PRINT_INIT("");
        LODOP.NewPage();
        if (LODOP && LODOP.VERSION) {
            $.ajax({
                type: "POST",
                contentType: "application/json",
                url: contextPath + "/dispatch/getPrintTemplate",
                data: JSON.stringify(selectedIds)
            }).done(function (tableHtml) {
                LODOP.SET_PRINT_PAGESIZE(1, "210mm", "148mm", 0);//纸张类型
                LODOP.ADD_PRINT_TEXT("11mm", "74mm", "60mm", "8mm", "新易泰物流作业单");//标题
                LODOP.SET_PRINT_STYLEA(0, "FontSize", 16);
                LODOP.ADD_PRINT_BARCODE("5mm", "150mm", "150", "10mm", "128Auto", dataItem.dispatchNumber);
                LODOP.ADD_PRINT_TEXT("20mm", "5mm", "150mm", "18mm", "司机:" + dataItem.driver);
                LODOP.SET_PRINT_STYLEA(0, "FontSize", 12);
                LODOP.ADD_PRINT_TEXT("20mm", "50mm", "150mm", "18mm", "车牌号:" + dataItem.vehicleNumber);
                LODOP.SET_PRINT_STYLEA(0, "FontSize", 12);
                LODOP.ADD_PRINT_TEXT("20mm", "95mm", "150mm", "18mm", "电话:" + dataItem.driverPhone);
                LODOP.SET_PRINT_STYLEA(0, "FontSize", 12);
                LODOP.ADD_PRINT_TEXT("20mm", "145mm", "150mm", "18mm", "派车单号:" + dataItem.dispatchNumber);
                LODOP.SET_PRINT_STYLEA(0, "FontSize", 12);
                LODOP.ADD_PRINT_HTM('30mm', 0, "100%", "100%", tableHtml);
                LODOP.PREVIEW();
            });
        }
    }
}

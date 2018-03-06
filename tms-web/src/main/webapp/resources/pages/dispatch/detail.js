angular.module('RoadnetApp').controller('DispatchDetailController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchDetailController]);
function DispatchDetailController($rootScope, $scope, $state, $stateParams) {

    $scope.dispatchItemsDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    orderNo: {type: "string", editable: false},
                    orderType: {editable: false},
                    customerName: {editable: false},
                    customerOrderNo: {editable: false},
                    totalPackageQty: {editable: false},
                    totalVolume: {editable: false},
                    totalWeight: {editable: false},
                    dispatchPackageQty: {editable: false},
                    packageQuantity: {type: "number", validation: {min: 0, required: true}},
                    volume: {type: "number", validation: {required: true}},
                    weight: {type: "number", validation: {required: true}},
                    orderDispatchType: {validation: {required: true}},
                    carrierCode: {type: "string"},
                    isLoaded: {type: "string"}
                }
            }
        }
    });

    $scope.itemsColumns = [{
        field: "orderNo",
        title: "单号",
        width: 150
    }, {
        field: "orderType.text",
        title: "订单/指令",
        width: 100
    }, {
        field: "customerName",
        title: "客户",
        width: 100
    }, {
        field: "customerOrderNo",
        title: "客户单号",
        width: 100
    }, {
        field: "totalPackageQty",
        title: "总箱数",
        width: 100
    }, {
        field: "totalVolume",
        title: "总体积",
        width: 100
    }, {
        field: "totalWeight",
        title: "总重量",
        width: 100
    }, {
        field: "dispatchPackageQty",
        title: "已派车箱数",
        width: 100
    }, {
        field: "packageQuantity",
        title: "派车箱数",
        width: 100
    }, {
        field: "volume",
        title: "派车体积",
        width: 100
    }, {
        field: "weight",
        title: "派车重量",
        width: 100
    }, {
        field: "orderDispatchType.text",
        title: "派车类型",
        width: 100
    }, {
        field: "carrierCode",
        values: carriers,
        title: "承运商",
        width: 150
    }, {
        field: "isLoaded",
        title: "是否已装车",
        width: 100,
        template: "#= isLoaded?'是':'否'#"
    }];

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

    $scope.dataBound = function () {
        $scope.grid = $("#dispatchItems").data("kendoExGrid");
    };
    /*$scope.dispatchPackagesDataSource = new kendo.data.DataSource({
     data: [],
     schema: {
     model: {
     fields: {
     orderNo: {type: "string"},
     packageNo: {type: "string"},
     volume: {type: "number"},
     weight: {type: "number"},
     goodsDesc: {type: "string"}
     }
     }
     }
     });

     $scope.PackagesColumns = [{
     field: "orderNo",
     title: "单号"
     }, {
     field: "packageNo",
     title: "箱号"
     }, {
     field: "volume",
     title: "体积"
     }, {
     field: "weight",
     title: "重量"
     }, {
     field: "goodsDesc",
     title: "货物描述"
     }];*/

    $scope.dispatchLogDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    operatorId: {type: "string"},
                    status: {type: "string"},
                    operationDate: {type: "date"},
                    operatorContent: {type: "string"}
                }
            }
        }
    });

    $scope.logColumns = [{
        field: "operator",
        title: "操作人"
    }, {
        field: "status.text",
        title: "派车单状态"
    }, {
        field: "operationTime",
        title: "操作时间"
    }, {
        field: "operationContent",
        title: "操作内容"
    }];

    $scope.init = function () {
        $.ajax({
            url: contextPath + "/dispatch/get/" + $stateParams.dispatchId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            if (!result.success) {
                return;
            }
            $scope.dispatch = result.body.dispatch;
            if (result.body.dispatchItemDtoList) {
                $scope.dispatchItemsDataSource.data(result.body.dispatchItemDtoList);
                /*if ($scope.dispatch.packages) {
                 var dataSource = new kendo.data.DataSource({
                 data: $scope.dispatch.packages
                 });
                 $("#dispatchPackages").data("kendoGrid").setDataSource(dataSource);
                 }*/
            }
            if ($scope.dispatch.logs) {
                $scope.dispatchLogDataSource.data($scope.dispatch.logs);
            }
            if (result.body.feeDetailDtos) {
                $scope.dispatchFeeDetailDataSource.data(result.body.feeDetailDtos);
            }
            if (result.body.dispatch.follows && result.body.dispatch.follows.length > 0) {
                var followUserIds = [];
                $.each(result.body.dispatch.follows, function (index, item) {
                    followUserIds.push(item.followUserId);
                });
                $scope.followUserIds = followUserIds;
            }
            $("#dispatchDetail").modal();
            $scope.$apply();
        });
    };

    $scope.addOrder = function () {
        var status = $scope.dispatch.status.value;
        if (status == 'CANCELED' || status == 'FINISHED') {
            App.toastr("派车单已完成或已取消，不能操作加单", "warning");
            return;
        }
        $rootScope.dispatchId = $stateParams.dispatchId;
        $state.go("addOrder");
        $("#dispatchDetail").modal("hide");
    };

    $scope.removeOrder = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        var isLoadedOrderNoStr = '';
        var orderNos = [];
        $.each(rows, function (i, item) {
            orderNos.push(item.orderNo);
            if (item.isLoaded) {
                isLoadedOrderNoStr += item.orderNo + ",";
            }
        });
        if (isLoadedOrderNoStr && isLoadedOrderNoStr != '') {
            toastr.error(isLoadedOrderNoStr.substring(0, isLoadedOrderNoStr.length - 1) + "已装车，不能从当前派车单中减掉");
            return;
        }
        $.ajax({
            url: contextPath + "/dispatch/removeOrders",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({dispatchId: $scope.dispatch.dispatchId, orderNos: orderNos})
        }).done(function (result) {
            if (result.success) {
                toastr.success("减单成功!");
                $scope.init();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
    $scope.init();
}
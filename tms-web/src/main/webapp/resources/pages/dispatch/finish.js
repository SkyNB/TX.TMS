angular.module('RoadnetApp').controller('DispatchFinishController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchFinishController]);
function DispatchFinishController($rootScope, $scope, $state, $stateParams) {
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
        title: "承运商",
        values: carriers,
        width: 150
    }, {
        field: "isLoaded",
        title: "是否已装车",
        width: 100,
        template: "#= isLoaded?'是':'否'#"
    }];

    if ($rootScope.dispatchId) {
        $("#dispatchFinish").modal();
    }
    $("#dispatchFinish").on("shown.bs.modal", function () {
        $scope.init();
    });

    $("#dispatchFinish").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.dataBound = function () {
        $scope.grid = $("#dispatchItems").data("kendoExGrid");
    };

    $scope.init = function () {
        $.ajax({
            url: contextPath + "/dispatch/get/" + $rootScope.dispatchId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            if (result.body.dispatchItemDtoList) {
                $scope.dispatchItemsDataSource.data(result.body.dispatchItemDtoList);
                $scope.grid.select("tr");
                $scope.$apply();
            }
        });
    };
    $scope.submit = function () {
        var allData = $scope.dispatchItemsDataSource.data();
        var selectedData = $scope.grid.select();
        var finishOrderNos = [];
        var notFinishOrderNos = [];
        if (selectedData.length == 0) {
            $.each(allData, function (i, row) {
                notFinishOrderNos.push(row.orderNo);
            });
        } else {
            $.each(selectedData, function (i, row) {
                var dataItem = $scope.grid.dataItem(row);
                finishOrderNos.push(dataItem.orderNo);
            });
            $.each(allData, function (i, row) {
                var isFinish = false;
                $.each(finishOrderNos, function (i, orderNo) {
                    if (orderNo === row.orderNo) {
                        isFinish = true;
                    }
                });
                if (!isFinish) {
                    notFinishOrderNos.push(row.orderNo);
                }
            });
        }
        $.ajax({
            url: contextPath + "/dispatch/finish",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                dispatchId: $rootScope.dispatchId,
                finishOrderNos: finishOrderNos,
                notFinishOrderNos: notFinishOrderNos
            })
        }).done(function (result) {
            if (result.success) {
                $("#dispatchFinish").modal("hide");
                toastr.success("操作成功!");
                $rootScope.data.query();
            } else {
                toastr.error("操作失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
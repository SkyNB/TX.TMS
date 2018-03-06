angular.module('RoadnetApp').controller('DispatchLoadingController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchLoadingController]);
function DispatchLoadingController($rootScope, $scope, $state, $stateParams) {
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
        $("#dispatchLoading").modal();
    }
    $("#dispatchLoading").on("shown.bs.modal", function () {
        $scope.init();
    });

    $("#dispatchLoading").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.dataBound = function () {
        $scope.grid = $("#dispatchItems").data("kendoGrid");
    };

    $scope.scan = function () {
        var orderNo = $("#orderNo").val();
        if (!orderNo) {
            toastr.error("请扫描单号！");
            return;
        }
        var data = $scope.dispatchItemsDataSource.data();
        var rowNo;
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            if (item.orderNo === orderNo) {
                rowNo = i;
            }
        }
        $scope.grid.select('tr:eq(' + rowNo + ')');
        $("#orderNo").val('');
    };

    $scope.init = function () {
        $("#orderNo").val('');
        $.ajax({
            url: contextPath + "/dispatch/get/" + $rootScope.dispatchId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            if (!result.success) {
                return;
            }
            if (result.body.dispatchItemDtoList) {
                $scope.dispatchItemsDataSource.data(result.body.dispatchItemDtoList);
            }
            $scope.$apply();
        });
    };
    $scope.submit = function () {
        var selectedData = $scope.grid.select();
        if(selectedData.length == 0 ){
            toastr.error("未扫描订单！");
            return;
        }
        var orderNos= [];
        selectedData.each(function (i, row) {
            var dataItem = $scope.grid.dataItem(row);
            if (dataItem) {
                orderNos.push(dataItem.orderNo);
            }
        });
        $.ajax({
            url: contextPath + "/dispatch/loading",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({dispatchId:$rootScope.dispatchId,orderNos:orderNos})
        }).done(function (result) {
            if (result.success) {
                $("#dispatchLoading").modal("hide");
                toastr.success("保存成功!");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
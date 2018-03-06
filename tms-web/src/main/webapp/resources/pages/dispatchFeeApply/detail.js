angular.module('RoadnetApp').controller('DispatchFeeApplyDetailController', ['$rootScope', '$scope', '$state', '$stateParams', DispatchFeeApplyDetailController]);
function DispatchFeeApplyDetailController($rootScope, $scope, $state, $stateParams) {

    $scope.feeApplyItemDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    accountName: {type: "string", editable: false},
                    applyAmount: {type: "number", editable: false},
                    applyRemark: {type: "string", editable: false},
                    approveAmount: {type: "number", validation: {min: 0, required: true}},
                    approveRemark: {type: "string"}
                }
            }
        }
    });

    $scope.feeApplyItemColumns = [{
        field: "accountName",
        title: "科目"
    }, {
        field: "applyAmount",
        title: "申报金额"
    }, {
        field: "applyRemark",
        title: "申报备注"
    }, {
        field: "approveAmount",
        title: "审批金额"
    }, {
        field: "approveRemark",
        title: "审批备注"
    }];

    $scope.feeApplyOrderDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    orderNo: {type: "string", editable: false},
                    orderType: {type: "number", editable: false}
                }
            }
        }
    });

    $scope.feeApplyOrderColumns = [{
        field: "orderNo",
        title: "单号"
    }, {
        field: "orderType",
        values:orderTypes,
        title: "订单/指令"
    }];

    $scope.dataBound = function () {
        $scope.feeApplyItemGrid = $("#feeApplyItemGrid").data("kendoExGrid");
    };

    $scope.dispatchFeeApply = {};
    $scope.approveUserName = '';
    $scope.picsInfo = [];
    $scope.init = function () {
        $.ajax({
            url: contextPath + "/dispatchFeeApply/get/",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($stateParams.feeApplyId)
        }).done(function (result) {
            if (!result.success) {
                return;
            }
            $scope.dispatchFeeApply = result.body.dispatchFeeApply;
            if (result.body.feeApplyItems) {
                $scope.feeApplyItemDataSource.data(result.body.feeApplyItems);
            }
            if($scope.dispatchFeeApply.orders){
                $scope.feeApplyOrderDataSource.data($scope.dispatchFeeApply.orders);
            }
            if($scope.dispatchFeeApply.pics)
                $scope.picsInfo = $scope.dispatchFeeApply.pics
            $scope.approveUserName = result.body.approveUserName;
            $scope.dispatch = result.body.dispatch;
            if (result.body.dispatchItemDtoList) {
                $scope.dispatchItemsDataSource.data(result.body.dispatchItemDtoList);
            }
            if(result.body.feeDetailDtos){
                $scope.dispatchFeeDetailDataSource.data(result.body.feeDetailDtos);
            }
            $("#dispatchFeeApplyDetail").modal();
            $scope.$apply();
        });
    };

    $("#dispatchFeeApplyDetail").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.init();

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
                    carrierId: {type: "string"},
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
        field: "carrierId",
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
                    amount: {type: "number", validation: {min: 0,required: true}},
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
}
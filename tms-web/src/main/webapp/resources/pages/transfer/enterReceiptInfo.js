angular.module('RoadnetApp').controller('TransferEnterReceiptInfoController', ['$rootScope', '$scope', '$state', '$stateParams', TransferEnterReceiptInfoController]);
function TransferEnterReceiptInfoController($rootScope, $scope, $state, $stateParams) {

    $scope.transferDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    orderNo: {type: "string", editable: false},
                    customerName: {type: "string", editable: false},
                    customerOrderNo: {type: "string", editable: false},
                    deliveryContacts: {type: "string", editable: false},
                    deliveryAddress: {type: "string", editable: false},
                    deliveryCompany: {type: "string", editable: false},
                    deliveryContactPhone: {type: "string", editable: false}
                }
            }
        }
    });

    $scope.transferColumns = [{
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
        field: "deliveryContacts",
        title: "收货人",
        width: 100
    }, {
        field: "deliveryAddress",
        title: "收货地址",
        width: 100
    }, {
        field: "deliveryCompany",
        title: "收货公司",
        width: 100
    }, {
        field: "deliveryContactPhone",
        title: "收货人电话",
        width: 100
    }];

    $scope.receiptPostTime = new Date().format("yyyy-MM-dd HH:mm:ss");

    $("#transferEnterReceiptInfo").modal();

    $("#transferEnterReceiptInfo").on("shown.bs.modal", function () {
        if ($rootScope.selectedTransfer && $rootScope.selectedTransfer.length > 0) {
            $scope.transferDataSource.data($rootScope.selectedTransfer);
            $rootScope.selectedTransfer = [];
            $scope.$apply();
        }else{
            $scope.transferDataSource.data([]);
        }
    });

    $("#transferEnterReceiptInfo").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.dataBound = function () {
        $scope.transferEnterReceiptInfoGrid = $("#transferGrid").data("kendoGrid");
    };


    $scope.submit = function () {

        if (!$scope.transferDataSource.data() || $scope.transferDataSource.data().length == 0) {
            App.toastr("请选择到货订单！", "warning");
            return;
        }

        var orderNos = [];
        $.each($scope.transferDataSource.data(), function (index, item) {
            orderNos.push(item.orderNo);
        });

        $.ajax({
            url: contextPath + "/transfer/enterReceiptInfo",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                receiptInfo: $scope.receiptInfo,
                receiptPostTime: $scope.receiptPostTime,
                orderNos: orderNos
            })
        }).done(function (result) {
            if (result.success) {
                $("#transferEnterReceiptInfo").modal("hide");
                toastr.success("保存成功!");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.searchOrder = function () {
        var orderNo = $("#orderNo").val();
        if (!orderNo || orderNo == '') {
            toastr.error("请输入单号！");
        }
        $.ajax({
            url: contextPath + "/transfer/findTransferByOrderNo",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(orderNo)
        }).done(function (result) {
            if (result.success) {
                if(result.body.status.value == 'NOT_ARRIVED'){
                    toastr.error("订单尚未到货确认！");
                }else{
                    $scope.transferDataSource.data().splice(0, 0, result.body);
                }
            } else {
                toastr.error(result.message);
            }
            $("#orderNo").val("");
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
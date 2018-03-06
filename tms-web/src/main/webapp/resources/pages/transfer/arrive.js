angular.module('RoadnetApp').controller('TransferArriveController', ['$rootScope', '$scope', '$state', '$stateParams', TransferArriveController]);
function TransferArriveController($rootScope, $scope, $state, $stateParams) {
    $scope.transferDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    orderNo: {type: "string", editable: false},
                    arriveRemark: {type: "string"},
                    customerName: {editable: false},
                    customerOrderNo: {editable: false},
                    deliveryContacts: {type: "string", editable: false},
                    deliveryAddress: {type: "string", editable: false},
                    deliveryCompany: {type: "string", editable: false},
                    deliveryContactPhone: {type: "string", editable: false},
                    volume: {type: "number", editable: false},
                    packageQuantity: {type: "number", editable: false}
                }
            }
        }
    });

    $scope.transferColumns = [{
        field: "orderNo",
        title: "单号",
        width: 150
    }, {
        field: "arriveRemark",
        title: "到货备注",
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
    }, {
        field: "volume",
        title: "体积",
        width: 100
    }, {
        field: "packageQuantity",
        title: "箱数",
        width: 100
    }];

    $scope.arriveTime = new Date().format("yyyy-MM-dd HH:mm:ss");

    $("#transferArrive").modal();

    $("#transferArrive").on("shown.bs.modal", function () {
        if ($rootScope.selectedTransfer && $rootScope.selectedTransfer.length > 0) {
            $scope.transferDataSource.data($rootScope.selectedTransfer);
            $scope.$apply();
            $rootScope.selectedTransfer = [];
        }else{
            $scope.transferDataSource.data([]);
        }
    });

    $("#transferArrive").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.dataBound = function () {
        $scope.transferArriveGrid = $("#transferGrid").data("kendoGrid");
    };


    $scope.submit = function () {

        if (!$scope.transferDataSource.data() || $scope.transferDataSource.data().length == 0) {
            App.toastr("请选择到货订单！", "warning");
            return;
        }

        var orderNos = [];
        var arriveRemarkList = [];
        $.each($scope.transferDataSource.data(), function (index, item) {
            orderNos.push(item.orderNo);
            arriveRemarkList.push(item.arriveRemark);
        });

        $.ajax({
            url: contextPath + "/transfer/arrive",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                arriveTime: $scope.arriveTime,
                orderNos: orderNos,
                arriveRemarkList: arriveRemarkList
            })
        }).done(function (result) {
            if (result.success) {
                $("#transferArrive").modal("hide");
                toastr.success("保存成功!");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.$watchCollection('transferDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalPackageQuantity = 0;
        var totalVolume = 0.0;
        var totalCount = 0;
        for (var i = 0; i < obj.length; i++) {
            totalPackageQuantity += parseInt(obj[i].packageQuantity);
            totalVolume += parseFloat(obj[i].volume);
            totalCount++;
        }
        $scope.totalPackageQuantity = totalPackageQuantity;
        $scope.totalVolume = totalVolume;
        $scope.totalCount = totalCount;
    });

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
                if(result.body.status.value != 'NOT_ARRIVED'){
                    toastr.error("订单已到货确认！");
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
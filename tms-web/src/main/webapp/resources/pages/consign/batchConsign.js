angular.module('RoadnetApp').controller('ConsignBatchConsignController', ['$rootScope', '$scope', ConsignBatchConsignController]);
function ConsignBatchConsignController($rootScope, $scope) {

    $("#batchConsign").modal();

    $scope.consignItemsDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    consignOrderNo: {type: "string"},
                    orderNo: {editable: false},
                    customerOrderNo: {editable: false},
                    customerName: {editable: false},
                    destCityName: {editable: false},
                    totalPackageQty: {editable: false},
                    totalVolume: {editable: false},
                    totalWeight: {editable: false},
                    consignPackageQty: {editable: false},
                    packageQuantity: {type: "number", validation: {min: 0, required: true}},
                    volume: {type: "number", validation: {required: true}},
                    weight: {type: "number", validation: {required: true}},
                    receiptPageNumber: {type: "number", validation: {min: 1, required: true}}
                }
            }
        }
    });

    $scope.itemsColumns = [{
        field: "consignOrderNo",
        title: "托运单号",
        width: 150
    }, {
        field: "orderNo",
        title: "单号",
        width: 150
    }, {
        field: "customerOrderNo",
        title: "客户单号",
        width: 100
    }, {
        field: "customerName",
        title: "客户",
        width: 100
    }, {
        field: "destCityName",
        title: "目的城市",
        width: 100
    }, {
        field: "totalPackageQty",
        title: "总箱数",
        width: 80
    }, {
        field: "totalVolume",
        title: "总体积",
        width: 80
    }, {
        field: "totalWeight",
        title: "总重量",
        width: 80
    }, {
        field: "consignPackageQty",
        title: "已开单箱数",
        width: 100
    }, {
        field: "packageQuantity",
        title: "发运箱数",
        width: 80
    }, {
        field: "volume",
        title: "发运体积",
        width: 80
    }, {
        field: "weight",
        title: "发运重量",
        width: 80
    }, {
        field: "receiptPageNumber",
        title: "回单页数",
        width: 80
    }, {
        command: "destroy",
        width: 80
    }];

    $scope.batchConsignModel = {};
    $scope.batchConsignModel.consignTime = new Date().format("yyyy-MM-dd HH:mm:ss");
    $scope.batchConsignModel.feedbackConsignTime = new Date().format("yyyy-MM-dd HH:mm:ss");
    $scope.batchConsignModel.transportType = 'HIGHWAY_LTL';
    $scope.totalOrderCount = 0;


    $scope.reset = function () {
        $scope.batchConsignModel = {};
    };

    $("#batchConsign").on("shown.bs.modal", function () {
        if (!$rootScope.selectOrders) return;
        var selectOrders = $rootScope.selectOrders;
        $rootScope.selectOrders = [];
        var orderNos = [];
        $.each(selectOrders, function (i, item) {
            orderNos.push(item.orderNo);
        });

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/consign/findItemDtoListByOrderNos",
            data: JSON.stringify(orderNos)
        }).done(function (result) {
            if (result && result.success) {
                $scope.consignItemsDataSource.data(result.body);
                $scope.$apply();
            }
        });
        $scope.$apply();
    });

    $scope.$watchCollection('consignItemsDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalOrderCount = 0;
        for (var i = 0; i < obj.length; i++) {
            totalOrderCount++;
        }
        $scope.totalOrderCount = totalOrderCount;
    });

    $("#batchConsign").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.validate = $("#batchConsignForm").validate();

    $scope.submit = function () {

        if (!$scope.validate.valid()) return;

        if ($scope.consignItemsDataSource.data().length < 1) {
            toastr.error("批量开单订单不能为空！");
            return;
        }
        //验证发运箱数是否大于总箱数和已开单箱数之差
        var exceptionOrderNos = '';
        $.each($scope.consignItemsDataSource.data(), function (index, item) {
            if ((item.totalPackageQty - item.consignPackageQty) < item.packageQuantity) {
                exceptionOrderNos += (item.orderNo + ",");
            }
        });
        if (exceptionOrderNos && exceptionOrderNos != '') {
            toastr.error("订单" + exceptionOrderNos.substr(0, exceptionOrderNos.length - 1) + "的发运箱数不能大于总箱数和已开单箱数之差");
            return;
        }
        $scope.batchConsignModel.items = $scope.consignItemsDataSource.data();

        $.ajax({
            url: contextPath + "/consign/batchConsign",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.batchConsignModel)
        }).done(function (result) {
            if (result.success) {
                $("#batchConsign").modal("hide");
                toastr.success("批量发运成功!");
                $scope.reset();
                $rootScope.data.query();
            } else {
                toastr.error("批量发运失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.mergeConsign = function () {

        if (!$scope.validate.valid()) return;

        if ($scope.consignItemsDataSource.data().length < 1) {
            toastr.error("批量开单订单不能为空！");
            return;
        }
        //验证发运箱数是否大于总箱数和已开单箱数之差
        var exceptionOrderNos = '';
        $.each($scope.consignItemsDataSource.data(), function (index, item) {
            if ((item.totalPackageQty - item.consignPackageQty) < item.packageQuantity) {
                exceptionOrderNos += (item.orderNo + ",");
            }
        });
        if (exceptionOrderNos && exceptionOrderNos != '') {
            toastr.error("订单" + exceptionOrderNos.substr(0, exceptionOrderNos.length - 1) + "的发运箱数不能大于总箱数和已开单箱数之差");
            return;
        }
        $scope.batchConsignModel.items = $scope.consignItemsDataSource.data();

        $.ajax({
            url: contextPath + "/consign/mergeConsign",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.batchConsignModel)
        }).done(function (result) {
            if (result.success) {
                $("#batchConsign").modal("hide");
                toastr.success("批量发运成功!");
                $scope.reset();
                $rootScope.data.query();
            } else {
                toastr.error("批量发运失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
}
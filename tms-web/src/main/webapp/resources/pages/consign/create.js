var app = angular.module('RoadnetApp');
app .config(function ($stateProvider) {
        $stateProvider
            .state('confirmUpdate', {
                url: '/confirmUpdate',
                reload: true,
                templateUrl: contextPath + '/payable/confirmUpdate',
                controller: "ConfirmUpdateController",
                resolve: getDeps([contextPath + '/resources/pages/payable/confirmUpdate.js'])
            })
    });
app.controller('ConsignCreateController', ['$rootScope', '$scope', '$state', ConsignCreateController]);
function ConsignCreateController($rootScope, $scope, $state) {

    $("#createConsign").modal();

    $scope.consignItemsDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
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

    $scope.consign = {};


    $scope.reset = function () {
        $scope.consign = {};
    };

    $("#createConsign").on("shown.bs.modal", function () {
        if (!$rootScope.selectOrders) return;
        var selectOrders = $rootScope.selectOrders;
        $rootScope.selectOrders = [];
        var orderNos = [];
        $.each(selectOrders, function (i, item) {
            orderNos.push(item.orderNo);
        });

        $scope.consign.whetherSignReceipt = true;
        $scope.consign.handoverType = 'DOOR_TO_DOOR';
        $scope.consign.operationTime = {};
        $scope.consign.operationTime.consignTime = new Date().format("yyyy-MM-dd HH:mm:ss");
        $scope.consign.operationTime.feedbackConsignTime = new Date().format("yyyy-MM-dd HH:mm:ss");
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/consign/findItemDtoListByOrderNos",
            data: JSON.stringify(orderNos)
        }).done(function (result) {
            if (result && result.success && result.body.length > 0) {
                var dataSource = result.body;
                $scope.consign.startCityCode = dataSource[0].orginCode;
                $scope.consign.destCityCode = dataSource[0].destinationCode;
                $scope.consign.consignee = dataSource[0].deliveryContacts;
                $scope.consign.consigneeAddress = dataSource[0].deliveryAddress;
                $scope.consign.consigneePhone = dataSource[0].deliveryContactPhone;
                var carrierCode;
                $.each(dataSource, function (index, item) {
                    if (item.carrierCode && item.carrierCode != '') {
                        carrierCode = item.carrierCode;
                    }
                });
                $scope.consign.carrierCode = carrierCode;
                if (carrierCode) {
                    $scope.carrierChange();
                }
                $scope.consignItemsDataSource.data(dataSource);
                $scope.$apply();
            }
        });
        $scope.$apply();
    });

    $scope.$watchCollection('consignItemsDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalPackageQuantity = 0;
        var totalVolume = 0.0;
        var totalWeight = 0.0;
        var receiptPageNumber = 0;
        for (var i = 0; i < obj.length; i++) {
            totalPackageQuantity += parseInt(obj[i].packageQuantity);
            totalVolume += parseFloat(obj[i].volume);
            totalWeight += parseFloat(obj[i].weight);
            receiptPageNumber += parseInt(obj[i].receiptPageNumber);
        }
        $scope.consign.totalPackageQuantity = totalPackageQuantity;
        $scope.consign.totalVolume = totalVolume;
        $scope.consign.totalWeight = totalWeight;
        $scope.consign.receiptPageNumber = receiptPageNumber;
    });

    $scope.carrierChange = function () {
        var carrierCode = $scope.consign.carrierCode;
        if (carrierCode && carrierCode != '') {
            $.ajax({
                type: "POST",
                contentType: "application/json",
                url: contextPath + "/carrier/getByCode/" + carrierCode
            }).done(function (result) {
                if (result && result.success) {
                    $scope.consign.settlementCycle = result.body.settleCycle.value;
                    $scope.consign.paymentType = result.body.paymentType.value;
                    $scope.consign.calculateType = result.body.calculateType.value;
                    $scope.consign.transportType = result.body.transportType.value;
                }
                $scope.$apply();
            });
        } else {
            $scope.consign.settlementCycle = '';
            $scope.consign.paymentType = '';
            $scope.consign.calculateType = '';
            $scope.consign.transportType = '';
        }
    };

    $("#createConsign").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.validate = $("#createConsignForm").validate();

    $scope.submit = function () {
        $("#consignTime").removeAttr("required");
        $("#feedbackConsignTime").removeAttr("required");
        $("#predictArriveTime").removeAttr("required");
        $scope.consign.operationTime.consignTime = null;
        $scope.consign.operationTime.feedbackConsignTime = null;
        $scope.consign.operationTime.predictArriveTime = null;//预计到货时间

        if (!$scope.validate.valid()) return;

        if ($scope.consign.transferOrganizationCode && $scope.consign.transferOrganizationCode != ''
            && (!$scope.consign.transferSiteCode || $scope.consign.transferSiteCode == '')) {
            toastr.error("请选择中转站点！");
            return;
        }

        if ($scope.consignItemsDataSource.data().length < 1) {
            toastr.error("托运单明细不能为空！");
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
        $scope.consign.items = $scope.consignItemsDataSource.data();
        $.ajax({
            url: contextPath + "/consign/create",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.consign)
        }).done(function (result) {
            if (result.success) {
                $("#createConsign").modal("hide");
                toastr.success("保存成功!");
                $scope.reset();
                if ($rootScope.isCreateByDriver) {
                    $rootScope.isCreateByDriver = false;
                    $state.go("createByDriver");
                    $("#createByDriver").modal("show");
                } else {
                    $rootScope.data.query();
                }
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.createAndConsign = function () {

        $("#feedbackConsignTime").attr("required", "required");
        $("#consignDate").attr("required", "required");
        $("#predictArriveTime").attr("required", "required");

        if (!$scope.validate.form()) return;

        if ($scope.consign.transferOrganizationCode && $scope.consign.transferOrganizationCode != ''
            && (!$scope.consign.transferSiteCode || $scope.consign.transferSiteCode == '')) {
            toastr.error("请选择中转站点！");
            return;
        }

        if ($scope.consignItemsDataSource.data().length < 1) {
            toastr.error("托运单明细不能为空！");
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
        $scope.consign.items = $scope.consignItemsDataSource.data();

        $.ajax({
            url: contextPath + "/consign/consign",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.consign)
        }).done(function (result) {
            if (result.success) {
                $("#createConsign").modal("hide");
                toastr.success("发运成功!");
                $scope.reset();
               /* if ($rootScope.isCreateByDriver) {
                    $rootScope.isCreateByDriver = false;
                    $state.go("createByDriver");
                    $("#createByDriver").modal("show");
                } else {
                    $rootScope.data.query();
                }*/
                if (result.body) {
                    var body = result.body;
                    $scope.toPayable(body.consignOrderNo, body.carrierCode);
                }
                $rootScope.data.query();
            } else {
                toastr.error("发运失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };
    //跳转到应付页面
    $scope.toPayable = function (consignOrderNo, carrierCode) {
        $("#consignOrderConsign").hide();
        if (!consignOrderNo || !carrierCode) {
            App.toastr("托运单号或承运商参数返回有问题！");
            return;
        }

        $.ajax({
            type: "GET",
            url: contextPath + "/payable/getBySourceNo/" + consignOrderNo + "/" + carrierCode
        }).done(function (result) {
            if (result.success) {
                if (result.body) {
                    $rootScope.status = "CREATED";
                    $rootScope.payableId = result.body.payableId;
                    $state.go("confirmUpdate");
                    $("#updateConfirm").modal("show");
                } else {
                    App.toastr("应付返回有问题！");
                }
            }
        }).fail(function () {
            App.toastr("数据提交失败!");
        });

    }
    $scope.searchOrder = function () {
        $("#searchOrder").modal("show");
    };

    $scope.search = function () {
        $scope.data.filter($("#searchOrderForm").serializeArray());
    };

    $scope.data = getDataSource("orderId", contextPath + "/consign/selectOrder");

    $scope.dataBound = function () {
        $scope.grid = $("#searchOrderGrid").data("kendoExGrid");
    };

    $scope.selectedRows = [];
    $scope.select = function () {
        var rows = $scope.grid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        $("#searchOrder").modal("hide");
        var orderNos = [];
        $.each(rows, function (i, item) {
            orderNos.push(item.orderNo);
        });

        $.ajax({
            url: contextPath + "/package/judgeOrdersIsHavePacked/",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(orderNos)
        }).done(function (result) {
            if (result.success) {
                var orderNos1 = result.body;
                //先打包
                if (orderNos1.length > 0) {
                    $scope.batchPackOrderNos = orderNos1;
                    $scope.selectedRows = rows;
                    $("#batchConfirm").modal("show");
                } else {
                    $scope.addConsignItems(orderNos);
                }
            } else {
                toastr.error("操作失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };


    $scope.addConsignItems = function (orderNos) {
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: contextPath + "/consign/findItemDtoListByOrderNos",
            data: JSON.stringify(orderNos)
        }).done(function (result) {
            if (result && result.success) {
                var addItems = result.body;
                $.each(addItems, function (i, item) {
                    if (!$scope.isExistOrder(item)) {
                        $scope.consignItemsDataSource.data().splice(0, 0, item);
                    }
                });
                $scope.$apply();
            }
        });
    };

    $scope.isExistOrder = function (item) {
        var flag = false;
        if ($scope.consignItemsDataSource.data().length == 0) {
            flag = false;
        }
        for (var i = 0; i < $scope.consignItemsDataSource.data().length; i++) {
            if (item.orderNo == $scope.consignItemsDataSource.data()[i].orderNo) {
                flag = true;
            }
        }
        return flag;
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "orderNo",
            title: "单号",
            width: 100
        }, {
            field: "customerOrderNo",
            title: "客户单号",
            width: 60
        }, {
            field: "customerName",
            title: "客户",
            width: 60
        }, {
            field: "transportType.text",
            title: "运输方式",
            width: 60
        }, {
            field: "deliveryContacts",
            title: "收货人",
            width: 60
        }, {
            field: "orderDate",
            title: "订单日期",
            width: 60
        }, {
            field: "shipCity",
            title: "始发城市",
            width: 60
        }, {
            field: "deliveryCity",
            title: "目的城市",
            width: 60
        }]
    };

    $scope.getSiteList = function () {
        $scope.consign.transferSiteCode = '';
        var transferOrganizationCode = $("#transferOrganizationCode").val();
        if (transferOrganizationCode && transferOrganizationCode != '') {
            var siteComboBox = $("#transferSiteCode").data("kendoComboBox");
            $.ajax({
                type: "GET",
                contentType: "application/json",
                url: contextPath + "/site/getByBranchCode/" + transferOrganizationCode
            }).done(function (result) {
                if (result) {
                    var dataSource = new kendo.data.DataSource({
                        data: result,
                        serverFiltering: true
                    });
                    siteComboBox.setDataSource(dataSource);
                } else {
                    var dataSource = new kendo.data.DataSource({
                        data: [],
                        serverFiltering: true
                    });
                    siteComboBox.setDataSource(dataSource);
                    App.toastr(result.message, "error");
                }
            }).fail(function () {
                App.toastr("提交失败", "error");
            });
        }
    };
    $scope.batchPackOrderNos = [];
    $("#batchConfirm").on("shown.bs.modal", function () {
        $.ajax({
            url: contextPath + "/package/findOrderSummary",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.batchPackOrderNos)
        }).done(function (result) {
            $scope.infoData = result;
            $scope.$apply();
        });
    });
    $scope.infoData = [];
    $scope.batchConfirmFormValidate = $("#batchConfirmForm").validate();
    $scope.batchPack = function () {
        if (!$scope.batchConfirmFormValidate.valid()) {
            return;
        }
        $scope.packingInfos = $scope.infoData;
        $.ajax({
            url: contextPath + "/package/batchConfirm",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.packingInfos)
        }).done(function (result) {
            if (result.success) {
                $scope.package = {};
                $("#batchConfirm").modal("hide");
                $scope.select($scope.selectedRows);
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function (r) {
            App.toastr("提交失败", "error");
        })
    }
}
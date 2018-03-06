angular.module('RoadnetApp').controller('DispatchAddOrderController', ['$rootScope', '$scope', '$state','$stateParams', DispatchAddOrderController]);
function DispatchAddOrderController($rootScope, $scope,$state, $stateParams) {
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
        values:carriers,
        title: "承运商",
        width: 150
    }];

    $scope.addItemsDataSource = new kendo.data.DataSource({
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
                    packageQuantity: {type: "number", validation: {min: 0,required: true}},
                    volume: {type: "number", validation: {required: true}},
                    weight: {type: "number", validation: {required: true}},
                    orderDispatchType: {type: "string",validation: {required: true}},
                    carrierCode: {type: "string"}
                }
            }
        }
    });

    $scope.addItemsColumns = [{
        field: "orderNo",
        title: "单号",
        width: 150
    }, {
        field: "orderType",
        title: "订单/指令",
        values: orderTypes,
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
        field: "orderDispatchType",
        title: "派车类型",
        values: orderDispatchTypes,
        width: 100
    }, {
        field: "carrierCode",
        values:carriers,
        title: "承运商",
        width: 150
    }, {
        command: "destroy",
        width: 100
    }];

    if($rootScope.dispatchId){
        $("#dispatchAddOrder").modal();
    }
    $("#dispatchAddOrder").on("shown.bs.modal", function () {
        $scope.init();
        $scope.addItemsDataSource.data([]);
        $("#orderNo").val("");
    });

    $scope.addOrderNos = [];
    $scope.addOrderQuery = function(orderNos) {
        if (orderNos.length > 0) {
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
                        $scope.addOrderNos = orderNos;
                        $("#batchConfirm").modal("show");
                    } else {
                        $scope.addDispatchItems(orderNos);
                    }
                } else {
                    toastr.error("操作失败！" + result.message);
                }
            }).fail(function () {
                App.toastr("数据提交失败!", "error");
            });
        }
    };

    $scope.addDispatchItems  = function(orderNos){
        if(orderNos.length>0){
            $.ajax({
                type: "POST",
                contentType: "application/json",
                url: contextPath + "/dispatch/findByOrderNos",
                data: JSON.stringify(orderNos)
            }).done(function (result) {
                if (!result || result.length == 0) {
                    toastr.error("单号不存在！");
                    return;
                }
                $.each(result, function (index, item) {
                    if (!$scope.isExistOrder(item)) {
                        item.orderType = item.orderType.value;
                        if (item.orderDispatchType && item.orderDispatchType != null) {
                            item.orderDispatchType = item.orderDispatchType.value;
                        }else{
                            item.orderDispatchType ='';
                        }
                        $scope.addItemsDataSource.data().splice(0, 0, item);
                    }
                });
            });
        }
    };

    $scope.addOrder = function () {
        var orderNo = $("#orderNo").val();
        if (!orderNo) {
            toastr.error("请输入单号！");
            return;
        }
        var orderNos =[];
        orderNos.push(orderNo);
        $scope.addOrderQuery(orderNos);
    };

    $scope.isExistOrder = function (item) {
        var flag = false;
        if ($scope.dispatchItemsDataSource.data().length == 0) {
            flag = false;
        }
        for (var i = 0; i < $scope.dispatchItemsDataSource.data().length; i++) {
            if (item.orderNo == $scope.dispatchItemsDataSource.data()[i].orderNo) {
                flag = true;
            }
        }
        if(!flag && $scope.addItemsDataSource.data().length>0){
            for (var i = 0; i < $scope.addItemsDataSource.data().length; i++) {
                if (item.orderNo == $scope.addItemsDataSource.data()[i].orderNo) {
                    flag = true;
                }
            }
        }
        return flag;
    };

    $("#dispatchAddOrder").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.addOrderSearch = function () {
        $("#addOrderSearch").modal("show");
    };

    $scope.init = function(){
        $.ajax({
            url: contextPath + "/dispatch/get/" + $rootScope.dispatchId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            if (!result.success) {
                return;
            }
            $scope.dispatch = result.body.dispatch;
            if (result.body.dispatchItemDtoList) {
                $scope.dispatchItemsDataSource.data(result.body.dispatchItemDtoList);
            }
            $("#dispatchDetail").modal();
            $scope.$apply();
        });
    };
    $scope.dispatchOperateModel = new Object();
    $scope.submit = function () {
        if ($scope.addItemsDataSource.data().length < 1) {
            toastr.error("新增明细不能为空！");
            return;
        }
        //验证发运箱数是否大于总箱数和已开单箱数之差
        var exceptionOrderNos = '';
        $.each($scope.addItemsDataSource.data(), function (index, item) {
            if ((item.totalPackageQty - item.dispatchPackageQty) < item.packageQuantity) {
                exceptionOrderNos += (item.orderNo + ",");
            }
        });
        if (exceptionOrderNos && exceptionOrderNos != '') {
            toastr.error("订单" + exceptionOrderNos.substr(0, exceptionOrderNos.length - 1) + "的派车箱数不能大于总箱数和已派车箱数之差");
            return;
        }

        var notDispatchTypeOrderStr = '';
        for (var i = 0; i < $scope.addItemsDataSource.data().length; i++) {
            var item = $scope.addItemsDataSource.data()[i];
            if (item.orderDispatchType==null || !item.orderDispatchType || item.orderDispatchType == '') {
                notDispatchTypeOrderStr += item.orderNo + ",";
            }
        }
        if (notDispatchTypeOrderStr && notDispatchTypeOrderStr != '') {
            toastr.error("请选择" + notDispatchTypeOrderStr.substring(0, notDispatchTypeOrderStr.length - 1) + "的派车类型！");
            return;
        }
        $scope.dispatchOperateModel.dispatchId = $rootScope.dispatchId;
        $scope.dispatchOperateModel.items = $scope.addItemsDataSource.data();
        $.ajax({
            url: contextPath + "/dispatch/addOrder",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.dispatchOperateModel)
        }).done(function (result) {
            if (result.success) {
                $("#dispatchAddOrder").modal("hide");
                toastr.success("保存成功!");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.search = function () {
        $scope.data.filter($("#fromSearchDispatchQuery").serializeArray());
    };

    $rootScope.dispatchQueryData = getDataSource("orderId", contextPath + "/dispatch/dispatchQuerySearch");

    $scope.dispatchQueryDataBound = function () {
        $scope.dispatchQueryGrid = $("#dispatchQueryGrid").data("kendoExGrid");
    };

    $scope.select = function () {
        var rows = $scope.dispatchQueryGrid.getSelectedData();
        if (rows.length == 0) {
            App.toastr("请选择数据", "warning");
            return;
        }
        $("#addOrderSearch").modal("hide");
        var orderNos = [];
        $.each(rows, function (i, item) {
            orderNos.push(item.orderNo);
        });
        $scope.addOrderQuery(orderNos);
    };

    $("#addOrderSearch").on("shown.bs.modal", function () {
        $rootScope.dispatchQueryData.query();
    });


    $scope.dispatchQueryGridOptions = {
        dataSource: $scope.dispatchQueryData,
        columns: [{
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
            field: "orderType.text",
            title: "订单/指令",
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
            field: "destinationName",
            title: "目的城市",
            width: 100
        }, {
            field: "address",
            title: "地址",
            width: 100
        }, {
            field: "deliveryContacts",
            title: "收货人",
            width: 100
        }]
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
                $scope.addOrderQuery($scope.addOrderNos);
            } else {
                App.toastr(result.message, "error");
            }
        }).fail(function (r) {
            App.toastr("提交失败", "error");
        })
    }
}
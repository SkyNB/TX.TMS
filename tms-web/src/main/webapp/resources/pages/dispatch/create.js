angular.module('RoadnetApp').controller('DispatchCreateController', ['$rootScope', '$scope', DispatchCreateController]);
function DispatchCreateController($rootScope, $scope) {

    $("#addDispatch").modal();

    $scope.dispatchItemsDataSource = new kendo.data.DataSource({
        data: [], schema: {
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
                    orderDispatchType: {type: "string", validation: {required: true}},
                    carrierCode: {type: "string",editable:false}
                }
            }
        }
    });

    $scope.itemsColumns = [{
        field: "orderNo", title: "单号", width: 150
    }, {
        field: "orderType", title: "订单/指令", values: orderTypes, width: 100
    }, {
        field: "customerName", title: "客户", width: 100
    }, {
        field: "customerOrderNo", title: "客户单号", width: 100
    }, {
        field: "totalPackageQty", title: "总箱数", width: 100
    }, {
        field: "totalVolume", title: "总体积", width: 100
    }, {
        field: "totalWeight", title: "总重量", width: 100
    }, {
        field: "dispatchPackageQty", title: "已派车箱数", width: 100
    }, {
        field: "packageQuantity", title: "派车箱数", width: 100
    }, {
        field: "volume", title: "派车体积", width: 100
    }, {
        field: "weight", title: "派车重量", width: 100
    }, {
        field: "orderDispatchType", title: "派车类型", values: orderDispatchTypes,width: 100
    }, {
        field: "carrierCode", title: "承运商",
        values: carriers, width: 150
    }, {
        command: "destroy", width: 100
    }];

    $scope.itemChange = function(e){
        if(e.values.orderDispatchType){
            var dispatchType = e.values.orderDispatchType;
            if(dispatchType!="COLLECTINGCONSIGN"&&dispatchType!="CONSIGN"){
                e.model.fields.carrierCode.editable = false;
                e.model.carrierCode="";
                $(e.container).next("td").text("");
            }else{
                e.model.fields.carrierCode.editable = true;
            }
        }
    };
    $scope.dispatchFeeDetailDataSource = new kendo.data.DataSource({
        data: feeDetailDtos, schema: {
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
        field: "feeAccountName", title: "科目"
    }, {
        field: "amount", title: "金额"
    }, {
        field: "remark", title: "备注"
    }];

    /*$scope.dispatchPackagesDataSource = new kendo.data.DataSource({
     data: [],
     schema: {
     model: {
     fields: {
     orderNo: {type: "string", editable: false},
     packageNo: {type: "string", editable: false},
     volume: {type: "number", editable: false},
     weight: {type: "number", editable: false},
     goodsDesc: {type: "string", editable: false}
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

    $scope.vehicleDataSource = new kendo.data.DataSource({
        serverFiltering: false, transport: {
            read: {
                dataType: "json", url: contextPath + "/vehicle/getAvailableForSelect"
            }
        }
    });

    $scope.vehicleOptions = {
        dataSource: $scope.vehicleDataSource,
        filter: "contains",
        dataTextField: "text",
        dataValueField: "value",
        change: function (e) {
            var value = this.value();
            var exists = $.grep($scope.vehicleDataSource.data(), function (v) {
                return v.value === value;
            });
            if (exists.length <= 0) {
                this.text('');
                this.value('');
            }
        }
    };

    $scope.dispatch = {};
    $scope.dispatch.startAddress = startAddress;
    $scope.reset = function () {
        $scope.dispatch = {};
        $scope.dispatch.startAddress = startAddress;
    };

    $scope.validate = $("#addDispatchForm").validate();

    $scope.isExistOrder = function (item) {
        if ($scope.dispatchItemsDataSource.data().length == 0) {
            return false;
        }
        var flag = false;
        for (var i = 0; i < $scope.dispatchItemsDataSource.data().length; i++) {
            if (item.orderNo == $scope.dispatchItemsDataSource.data()[i].orderNo) {
                flag = true;
            }
        }
        return flag;
    };

    $("#addDispatch").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $("#addDispatch").on("shown.bs.modal", function () {
        $scope.dispatchItemsDataSource.data([]);
        if (!$rootScope.selectedOrderNos || $rootScope.selectedOrderNos.length == 0) {
            return;
        }
        var selectedOrderNos = $rootScope.selectedOrderNos;
        $rootScope.selectedOrderNos = [];
        $scope.addOrderQuery(selectedOrderNos);

    });
    $scope.addOrderNos = [];
    $scope.addOrderQuery = function (orderNos) {
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

    $scope.addDispatchItems = function (orderNos) {
        if (orderNos.length > 0) {
            //派车
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
                        item.orderType = item.orderType.name;
                        item.carrierCode ="";
                        if (item.orderDispatchType && item.orderDispatchType != null) {
                            item.orderDispatchType = item.orderDispatchType.name;
                        } else {
                            item.orderDispatchType = '';
                        }
                        $scope.dispatchItemsDataSource.data().splice(0, 0, item);
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
        var orderNos = [];
        orderNos.push(orderNo);
        $scope.addOrderQuery(orderNos);
    };

    $scope.vehicleChange = function () {
        var vehicleId = $scope.dispatch.vehicleId;
        if (vehicleId && vehicleId != '') {
            $.ajax({
                type: "POST", contentType: "application/json", url: contextPath + "/vehicle/get/" + vehicleId
            }).done(function (result) {
                if (result && result.success) {
                    $scope.dispatch.vehicleNumber = result.body.vehicleNo;
                    $scope.dispatch.driver = result.body.driver;
                    $scope.dispatch.driverPhone = result.body.driverMobile;
                    $scope.dispatch.vehicleTypeId = result.body.vehicleTypeId;
                }
                $scope.$apply();
            });
        } else {
            $scope.dispatch.vehicleNumber = '';
            $scope.dispatch.driver = '';
            $scope.dispatch.driverPhone = '';
            $scope.dispatch.vehicleTypeId = '';
        }
    };

    $scope.$watchCollection('dispatchItemsDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalPackageQuantity = 0;
        var totalVolume = 0.0;
        var totalWeight = 0.0;
        for (var i = 0; i < obj.length; i++) {
            totalPackageQuantity += parseInt(obj[i].packageQuantity);
            totalVolume += parseFloat(obj[i].volume);
            totalWeight += parseFloat(obj[i].weight);
        }
        $scope.dispatch.totalPackageQuantity = totalPackageQuantity;
        $scope.dispatch.totalVolume = totalVolume;
        $scope.dispatch.totalWeight = totalWeight;
    });

    $scope.submit = function () {
        if (!$scope.validate.valid()) return;

        if ($scope.dispatchItemsDataSource.data().length < 1) {
            toastr.error("派车明细不能为空！");
            return;
        }
        var notDispatchTypeOrderStr = '';
        for (var i = 0; i < $scope.dispatchItemsDataSource.data().length; i++) {
            var item = $scope.dispatchItemsDataSource.data()[i];
            if (!item.orderDispatchType || item.orderDispatchType == null || item.orderDispatchType == '') {
                notDispatchTypeOrderStr += item.orderNo + ",";
            }
        }
        if (notDispatchTypeOrderStr && notDispatchTypeOrderStr != '') {
            toastr.error("请选择" + notDispatchTypeOrderStr.substring(0, notDispatchTypeOrderStr.length - 1) + "的派车类型！");
            return;
        }
        //验证发运箱数是否大于总箱数和已开单箱数之差
        var exceptionOrderNos = '';
        $.each($scope.dispatchItemsDataSource.data(), function (index, item) {
            if ((item.totalPackageQty - item.dispatchPackageQty) < item.packageQuantity) {
                exceptionOrderNos += (item.orderNo + ",");
            }
        });
        if (exceptionOrderNos && exceptionOrderNos != '') {
            toastr.error("订单" + exceptionOrderNos.substr(0, exceptionOrderNos.length - 1) + "的派车箱数不能大于总箱数和已派车箱数之差");
            return;
        }

        $scope.dispatch.items = $scope.dispatchItemsDataSource.data();
        $scope.dispatch.feeDetails = $scope.dispatchFeeDetailDataSource.data();
        /*if ($scope.dispatchPackagesDataSource.data().length > 0) {
         $scope.dispatch.packages = $scope.dispatchPackagesDataSource.data();
         }*/
        var followUserIds = $("#followUserIds").val();
        if (followUserIds && followUserIds.length > 0) {
            var follows = [];
            $.each(followUserIds, function (i, followUserId) {
                var follow = new Object();
                follow.followUserId = followUserId;
                follows.push(follow);
            });
            $scope.dispatch.follows = follows;
        }
        $.ajax({
            url: contextPath + "/dispatch/create",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.dispatch)
        }).done(function (result) {
            if (result.success) {
                $("#addDispatch").modal("hide");
                toastr.success("保存成功!");
                $scope.reset();
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function () {
            App.toastr("数据提交失败!", "error");
        });
    };

    $scope.addOrderSearch = function () {
        $("#addOrderSearch").modal("show");
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
        dataSource: $scope.dispatchQueryData, columns: [{
            field: "orderNo", title: "单号", width: 150
        }, {
            field: "customerOrderNo", title: "客户单号", width: 100
        }, {
            field: "customerName", title: "客户", width: 100
        }, {
            field: "orderType.text", title: "订单/指令", width: 100
        }, {
            field: "totalPackageQty", title: "总箱数", width: 100
        }, {
            field: "totalVolume", title: "总体积", width: 100
        }, {
            field: "totalWeight", title: "总重量", width: 100
        }, {
            field: "destinationName", title: "目的城市", width: 100
        }, {
            field: "address", title: "地址", width: 100
        }, {
            field: "deliveryContacts", title: "收货人", width: 100
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
    };
    $scope.showSubmitBtn = false;
    $('#rootwizard').bootstrapWizard({
        onTabClick: function (tab, navigation, index) {
            return false;
        }, 'tabClass': 'nav nav-pills', 'onNext': function (tab, navigation, index) {
            if (!$("#tab" + index).validate().form())return false;

            var totalTab = navigation.find('li').length;
            if (index == (totalTab - 1)) {
                $scope.showSubmitBtn = true;
            } else {
                $scope.showSubmitBtn = false;
            }
        }
    });
}
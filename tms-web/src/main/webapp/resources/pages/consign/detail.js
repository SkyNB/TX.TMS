angular.module('RoadnetApp').controller('ConsignDetailController', ['$rootScope', '$scope', '$state', '$stateParams', ConsignDetailController]);
function ConsignDetailController($rootScope, $scope, $state, $stateParams) {

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
                    packageQuantity: {type: "number", validation: {required: true}},
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
    }];

    $scope.consignLogDataSource = new kendo.data.DataSource({
        data: [],
        schema: {
            model: {
                fields: {
                    operatorId: {type: "string"},
                    status: {type: "string"},
                    operationTime: {type: "date"},
                    operationContent: {type: "string"}
                }
            }
        }
    });

    $scope.logColumns = [{
        field: "operatorId",
        title: "操作人",
        width: 50
    }, {
        field: "status.text",
        title: "托运单状态",
        width: 50
    }, {
        field: "operationTime",
        title: "操作时间",
        width: 50
    }, {
        field: "operationContent",
        title: "操作内容",
        width: 100
    }];

    $scope.init = function () {
        $.ajax({
            url: contextPath + "/consign/get/" + $stateParams.consignOrderId,
            type: "POST",
            contentType: "application/json"
        }).done(function (result) {
            $scope.consign = result.body.consignOrder;
            $scope.consignOrderNo =  $scope.consign.consignOrderNo;
            $scope.carrierCode = $scope.consign.carrierCode;
            if ($scope.consign) {
                if ($scope.consign.logs) {
                    $scope.consignLogDataSource.data($scope.consign.logs);
                }
            }
            if (result.body.items) {
                $.each(result.body.items, function (index, item) {
                    item.consignPackageQty = item.consignPackageQty - item.packageQuantity;
                });
                $scope.consignItemsDataSource.data(result.body.items);
            }
            if ($scope.consign.transferOrganizationCode && $scope.consign.transferOrganizationCode != '') {
                $.ajax({
                    type: "GET",
                    contentType: "application/json",
                    url: contextPath + "/site/getAvailableForSelect/" + $scope.consign.transferOrganizationCode
                }).done(function (result) {
                    $scope.siteDataSource.data(result);
                });
            }
            $("#consignDetail").modal();
            //$scope.$apply();
        });
    };

    $scope.init();

    $scope.siteDataSource = new kendo.data.DataSource([]);

    $scope.siteOptions = {
        dataSource: $scope.siteDataSource,
        filter: "contains",
        dataTextField: "text",
        dataValueField: "value",
        change: function (e) {
            var value = this.value();
            var exists = $.grep($scope.siteDataSource.data(), function (v) {
                return v.value === value;
            });
            if (exists.length <= 0) {
                this.text('');
                this.value('');
            }
        }
    };

    $scope.getSiteList = function(){
        $scope.consign.transferSiteCode = '';
        if($scope.consign.transferOrganizationCode && $scope.consign.transferOrganizationCode!='') {
            $.ajax({
                type: "GET",
                contentType: "application/json",
                url: contextPath + "/site/getAvailableForSelect/" + $scope.consign.transferOrganizationCode
            }).done(function (result) {
                $scope.siteDataSource.data(result);
            });
        }
    };

    $scope.information= function(){
            $.ajax({
                type: "GET",
                contentType: "application/json",
                url: contextPath + "/progressTrace/deliveryOrderProgressQuery/" + $scope.consignOrderNo + "/" + $scope.carrierCode
            }).done(function (result) {

                if (result.success) {
                    if (result.body && result.body.length > 0) {
                        $scope.traceInfo = result.body;
                        $("#isHide").hide();
                        $scope.$apply();
                    } else {
                        $("#isHide").show();
                        $scope.traceInfo = [];
                        $scope.$apply();
                    }
                } else {
                    $("#isHide").show();
                    $scope.traceInfo = [];
                    $scope.$apply();
                    App.toastr(result.message, "error");
                }
            }).fail(function () {
                App.toastr("提交失败", "error");
            });

        };
}
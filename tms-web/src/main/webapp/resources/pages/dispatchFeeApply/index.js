var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('detail/:feeApplyId', {
            url: '/detail/:feeApplyId',
            reload: true,
            templateUrl: contextPath + '/dispatchFeeApply/detail',
            controller: "DispatchFeeApplyDetailController",
            resolve: getDeps([contextPath + '/resources/pages/dispatchFeeApply/detail.js'])
        })
});

app.controller('DispatchFeeApplyController', ['$scope', '$rootScope', '$state', DispatchFeeApplyController]);
function DispatchFeeApplyController($scope, $rootScope, $state) {

    $scope.search = function () {
        $scope.data.filter($("#searchDispatchFeeApplyForm").serializeArray());
    };

    $rootScope.data = getDataSource("feeApplyId", contextPath + "/dispatchFeeApply/pageList");

    $scope.dataBound = function () {
        $scope.grid = $("#dispatchFeeApplyGrid").data("kendoGrid");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "dispatchNumber",
            title: "派车单号",
            template: function (dataItem) {
                return "<a href='#/detail/" + dataItem.feeApplyId + "' data-target='#dispatchFeeApplyDetail' data-toggle='modal'>" + dataItem.dispatchNumber + "</a>";
            },
            width: 200
        }, {
            field: "vehicleNo",
            title: "车牌号",
            width: 150
        }, {
            field: "driver",
            title: "司机",
            width: 150
        }, {
            field: "applyTotalAmount",
            title: "申报金额",
            width: 150
        }, {
            field: "applyTime",
            title: "申报时间",
            width: 200
        }, {
            field: "approveTotalAmount",
            title: "审批金额",
            width: 150
        }, {
            field: "approveTime",
            title: "审批时间",
            width: 200
        }]
    };
}

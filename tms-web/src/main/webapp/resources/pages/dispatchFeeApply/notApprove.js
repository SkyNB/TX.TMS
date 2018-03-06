var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('approve/:feeApplyId', {
            url: '/approve/:feeApplyId',
            reload: true,
            templateUrl: contextPath + '/dispatchFeeApply/approve',
            controller: "DispatchFeeApplyApproveController",
            resolve: getDeps([contextPath + '/resources/pages/dispatchFeeApply/approve.js'])
        })
});

app.controller('DispatchFeeApplyController', ['$scope', '$rootScope', '$state', DispatchFeeApplyController]);
function DispatchFeeApplyController($scope, $rootScope, $state) {

    $scope.search = function () {
        $scope.data.filter($("#searchDispatchFeeApplyForm").serializeArray());
    };

    $rootScope.data = getDataSource("feeApplyId", contextPath + "/dispatchFeeApply/notApprovePageList");

    $scope.dataBound = function () {
        $scope.grid = $("#dispatchFeeApplyGrid").data("kendoGrid");
    };

    $scope.gridOptions = {
        dataSource: $scope.data,
        columns: [{
            field: "dispatchNumber",
            title: "派车单号",
            template: function (dataItem) {
                return "<a href='#/approve/" + dataItem.feeApplyId + "' data-target='#dispatchFeeApplyApprove' data-toggle='modal'>" + dataItem.dispatchNumber + "</a>";
            }
        }, {
            field: "vehicleNo",
            title: "车牌号"
        }, {
            field: "driver",
            title: "司机"
        }, {
            field: "applyTotalAmount",
            title: "申报金额"
        }, {
            field: "applyTime",
            title: "申报时间"
        }]
    };
}

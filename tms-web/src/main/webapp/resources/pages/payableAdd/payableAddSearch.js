var app = angular.module('RoadnetApp');

app.config(function($stateProvider){
    $stateProvider
        .state('/detail/:payableAddId', {
            url: '/detail/:payableAddId',
            reload: true,
            templateUrl: contextPath + '/payableAdd/detailSearch',
            controller: "detailSearchController",
            resolve: getDeps([contextPath + '/resources/pages/payableAdd/detailSearch.js'])
        })
});

app.controller('payableAddSearchController', ['$scope', '$rootScope', '$state', payableAddSearchController]);

function payableAddSearchController($scope, $rootScope, $state) {

    $scope.search = function(){
        $scope.data.filter($("#payableAddSearch").serializeArray());
    }

    $rootScope.data = getDataSource("payableAddId", contextPath + "/payableAdd/searchAuditPayableAdd");

    $scope.gridOptions = {
        dataSource: $scope.data, dataBound: function () {
            $scope.grid = $("#payableAddGrid").data("kendoExGrid");
        }, columns: [{
            field: "carrierName", title: "承运商", width: 120
        }, {
            field: "consignOrderCode", title: "托运单", width: 160, template: function(dataItem){
                return "<a href='#/detail/" + dataItem.payableAddId + "' data-target='#detailSearch' data-toggle='modal'>" + dataItem.consignOrderCode + "</a>";
            }
        },{
            field: "totalAmount", title: "总金额", width:80
        }, {
            field: "status.text", title: "审核状态", width:80
        }, {
            field: "confirmAmount", title: "审核金额", width:120
        }, {
            field: "createUserName", title: "申请人", width:120
        }, {
            field: "createDate", title: "申请日期", width:120
        }, {
            field: "approvedUserName", title: "审批人", width:120
        }, {
            field: "approvedDate", title: "审批日期", width:120
        }, {
            field: "remark", title: "备注"
        }]
    };
}

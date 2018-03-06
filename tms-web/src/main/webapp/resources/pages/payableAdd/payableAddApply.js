var app = angular.module('RoadnetApp');

app.config(function($stateProvider){
    $stateProvider
        .state('/create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/payableAdd/createPayableAdd',
            controller: "createController",
            resolve: getDeps([contextPath + '/resources/pages/payableAdd/create.js'])
        })
        .state('/detailEdit/:payableAddId', {
            url: '/detailEdit/:payableAddId',
            reload: true,
            templateUrl: function($routeParams){
                return contextPath + '/payableAdd/detailEdit/' + $routeParams.payableAddId;
            },
            controller: "detailEditController",
            resolve: getDeps([contextPath + '/resources/pages/payableAdd/detailEdit.js'])
        })
});

app.controller('payableAddApplyController', ['$scope', '$rootScope', '$state', payableAddApplyController]);

function payableAddApplyController($scope, $rootScope, $state) {

    $scope.search = function(){
        $scope.data.filter($("#payableAddSearch").serializeArray());
    }

    $rootScope.data = getDataSource("payableAddId", contextPath + "/payableAdd/searchPayableAdd");

    $scope.gridOptions = {
        dataSource: $scope.data, dataBound: function () {
            $scope.grid = $("#payableAddGrid").data("kendoExGrid");
        }, columns: [{
            field: "carrierName", title: "承运商", width: 120
        }, {
            field: "consignOrderCode", title: "托运单", width: 160, template: function(dataItem){
                return "<a href='#/detailEdit/" + dataItem.payableAddId + "' data-target='#detailEdit' data-toggle='modal'>" + dataItem.consignOrderCode + "</a>";
            }
        },{
            field: "totalAmount", title: "总金额", width:80
        }, {
            field: "confirmAmount", title: "审核金额", width:120
        }, {
            field: "status.text", title: "审核状态", width:80
        }, {
            field: "createUserName", title: "申请人", width:120
        }, {
            field: "createDate", title: "申请日期", width:120
        }, {
            field: "approvedUserName", title: "审批人", width:120
        }, {
            field: "approvedDate", title: "审批日期", width:120
        }, {
            field: "rejectedNotes", title: "拒绝原因"
        }]
    };
}

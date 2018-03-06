var app = angular.module('RoadnetApp');

app.config(function($stateProvider){
    $stateProvider
        .state('/audit/:payableAddId/:isAudit', {
            url: '/audit/:payableAddId/:isAudit',
            reload: true,
            templateUrl: contextPath + '/payableAdd/detailAudit',
            controller: "detailController",
            resolve: getDeps([contextPath + '/resources/pages/payableAdd/detailAudit.js'])
        })
        .state('/reject/:payableAddId', {
            url: '/reject/:payableAddId',
            reload: true,
            templateUrl: contextPath + '/payableAdd/reject',
            controller: "rejectController",
            resolve: getDeps([contextPath + '/resources/pages/payableAdd/reject.js'])
        })
        .state('/audit/:payableAddId', {
            url: '/audit/:payableAddId',
            reload: true,
            templateUrl: contextPath + '/payableAdd/detailAudit',
            controller: "detailController",
            resolve: getDeps([contextPath + '/resources/pages/payableAdd/detailAudit.js'])
        })
});

app.controller('payableAddAuditController', ['$scope', '$rootScope', '$state', payableAddAuditController]);

function payableAddAuditController($scope, $rootScope, $state) {

    $scope.check = function(){
        var payableAddNos = $scope.grid.getSelectedId();
        if (payableAddNos.length <= 0){
            toastr.warning("请选择一条记录！");
        } else if(payableAddNos.length > 1){
            toastr.warning("只能选择一条记录！");
        }else{
            var selectedData = $scope.grid.getSelectedData();
            if(selectedData[0].status.name == "UNAUDITED"){
                return true;
            }
            toastr.warning("请选择未审核的记录！");
        }
        return false;
    }

    $scope.audit = function(){
        if($scope.check()){
            var payableAddNos = $scope.grid.getSelectedId();
            $state.go("/audit/:payableAddId/:isAudit", {payableAddId:payableAddNos[0], isAudit:true});
            $("#detailAudit").modal();
        }
    }

    $scope.reject = function(){
        if($scope.check()){
            var payableAddNos = $scope.grid.getSelectedId();
            $state.go("/reject/:payableAddId", {payableAddId:payableAddNos[0]});
        }
    }

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
                return "<a href='#/audit/" + dataItem.payableAddId + "' data-target='#detailAudit' data-toggle='modal'>" + dataItem.consignOrderCode + "</a>";
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
            field: "rejectedNotes", title: "拒绝原因", width:150
        }, {
            field: "remark", title: "备注"
        }]
    };
}

var app = angular.module('RoadnetApp');

app.controller('detailSearchController', ['$scope', '$rootScope', '$state', '$stateParams', detailSearchController]);

function detailSearchController($scope, $rootScope, $state, $stateParams) {
    $scope.payableAdd = {}

    $("#detailSearch").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.init = function(){
        $.ajax({
            url:contextPath+"/payableAdd/detailEdit/"+$stateParams.payableAddId,
            type:"POST",
            contentType:"application/json"
        }).done(function(result){
            if(result.success){
                $("#detailSearch").modal();
                $scope.itemDataSource.data(result.body.accounts);
                $scope.payableAdd = result.body;
                $scope.$apply();
            }
            else{
                App.toastr("查询应付变更失败", result.message);
            }
        }).fail(function(){
            App.toastr("查询应付变更失败","error");
        });
    }

    $scope.itemDataSource = new kendo.data.DataSource({data:[]});

    $scope.itemColumns = [{
        field: "orderNo", title: "单号", width:170,
    },  {
        field: "accountName", title: "应付科目",
    }, {
        field: "amount", title: "金额"
    }, {
        field: "confirmAmount", title: "审核金额"
    }, {
        field: "remark", title: "备注"
    }];

    $scope.init();
}
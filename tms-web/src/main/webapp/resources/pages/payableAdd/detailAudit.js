var app = angular.module('RoadnetApp');

app.controller('detailController', ['$scope', '$rootScope', '$state', '$stateParams', detailController]);

function detailController($scope, $rootScope, $state, $stateParams) {
    $scope.payableAdd = {}
    $scope.validate = $("#createForm").validate();

    $("#detailAudit").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.init = function(){
        $.ajax({
            url:contextPath+"/payableAdd/detailEdit/"+$stateParams.payableAddId,
            type:"POST",
            contentType:"application/json"
        }).done(function(result){
            if(result.success){
                $("#detailAudit").modal();
                $scope.itemDataSource.data(result.body.accounts);
                $scope.payableAdd = result.body;
                if($scope.payableAdd.confirmAmount == null){
                    $scope.payableAdd.confirmAmount = 0;
                }
                $scope.$apply();
                if(!(result.body.status.name == "UNAUDITED")){
                    $("#passBtn").attr('disabled', "true").addClass("disabled");
                    $("#rejectBtn").attr('disabled', "true").addClass("disabled");
                }
                //console.log($stateParams.isAudit);
                $scope.isAudit = $stateParams.isAudit;
            }
            else{
                App.toastr("查询应付变更失败", result.message);
            }
        }).fail(function(){
            App.toastr("查询应付变更失败","error");
        });
    }

    $scope.pass = function(){
        if (!$scope.validate.valid()){   return   }
        //console.dir($scope.payableAdd.status);
        if($scope.payableAdd.status.name){
            $scope.payableAdd.status = $scope.payableAdd.status.name;
        }
        $scope.payableAdd.accounts = $scope.itemDataSource.data();
        //console.dir($scope.payableAdd);
        if($scope.payableAdd.accounts.length == 0){
            toastr.error("请添加应付变更记录");
            return;
        }
        $.ajax({
            url: contextPath+'/payableAdd/passPayableAdd',
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.payableAdd)
        }).done(function (result) {
            if (result.success) {
                $("#detailAudit").modal("hide");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function(){
            App.toastr("提交失败","error");
        });
    }

    $scope.reject = function(){
        //$state.go("/reject/:payableAddId", {payableAddId:$stateParams.payableAddId});
        $("#reject").modal();
    }

    $scope.rejectSubmit = function(){
        $.ajax({
            url: contextPath+'/payableAdd/rejectPayableAdd',
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({"payableAddId":$stateParams.payableAddId, "rejectedNotes": $scope.rejectedNotes})
        }).done(function (result) {
            if (result.success) {
                $("#reject").modal("hide");
                $scope.rejectedNotes = "";
                $("#detailAudit").modal("hide");
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function(){
            App.toastr("提交失败","error");
        });
    }

    $scope.$watchCollection('itemDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalAmount = 0.0;
        for (var i = 0; i < obj.length; i++) {
            totalAmount += parseFloat(obj[i].confirmAmount);
        }
        $scope.payableAdd.confirmAmount = totalAmount;
    });

    $scope.itemDataSource = new kendo.data.DataSource({
        data:[],
        schema: {
            model: {
                fields: {
                    orderNo: {type:"string",editable: false, validation: {required: true}},
                    accountCode: {type:"string",editable: false, validation: {required: true}},
                    amount: {type: "number",  editable: false},
                    confirmAmount: {type: "number", validation: {min: 0, required: true}, editable: true},
                    remark: {editable: true}
                }
            }
        }
    });
    $scope.payableAdd.accounts = $scope.itemDataSource.data();

    $scope.itemColumns = [{
        field: "orderNo", title: "单号", width:170,
    },  {
        field: "accountName", title: "应付科目",
    }, {
        field: "amount", title: "金额"
    }, {
        field: "confirmAmount", title: "审核金额", template: function(dataItem){
            if(dataItem.confirmAmount == null){
                return 0;
            }
            return dataItem.confirmAmount;
        }
    }, {
        field: "remark", title: "备注"
    }];

    $scope.gripOptions = {
        editable:true
    }

    $scope.init();
}
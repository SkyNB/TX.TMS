var app = angular.module('RoadnetApp');

app.controller('detailEditController', ['$scope', '$rootScope', '$state', '$stateParams', detailEditController]);

function detailEditController($scope, $rootScope, $state, $stateParams) {
    $scope.payableAdd = {}
    $scope.validate = $("#createForm").validate();

    $("#detailEdit").on("hidden.bs.modal", function () {
        $(".modal-backdrop.fade.in").remove();
    });

    $scope.init = function(){
        $.ajax({
            url:contextPath+"/payableAdd/detailEdit/"+$stateParams.payableAddId,
            type:"POST",
            contentType:"application/json"
        }).done(function(result){
            if(result.success){
                $("#detailEdit").modal("show");
                $scope.itemDataSource.data(result.body.accounts);
                $scope.payableAdd = result.body;
                $scope.$apply();
                if(!(result.body.status.name == "UNAUDITED")){
                    $("#submitBtn").attr('disabled', "true").addClass("disabled");
                }
            }
            else{
                App.toastr("查询应付变更失败", result.message);
            }
        }).fail(function(){
            App.toastr("查询应付变更失败","error");
        });
    }

    $scope.submit = function(){
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
            url: contextPath+'/payableAdd/updatePayableAdd',
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.payableAdd)
        }).done(function (result) {
            if (result.success) {
                $("#detailEdit").modal("hide");
                $scope.reset();
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function(){
            App.toastr("提交失败","error");
        });
    }
    $scope.reset = function(){
        $scope.payableAdd = {};
        $scope.itemDataSource.data([]);
    }

    $scope.$watchCollection('itemDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalAmount = 0.0;
        for (var i = 0; i < obj.length; i++) {
            totalAmount += parseFloat(obj[i].amount);
        }
        $scope.payableAdd.totalAmount = totalAmount;
    });

    $scope.itemDataSource = new kendo.data.DataSource({
        data:[],
        schema: {
            model: {
                fields: {
                    orderNo: {type:"string",editable: true, validation: {required: true}},
                    accountCode: {type:"string",editable: true, validation: {required: true}},
                    amount: {type: "number", validation: {min: 1, required: true}, defaultValue: 1, editable: true},
                    confirmAmount: {type: "number",  editable: false},
                    remark: {editable: true}
                }
            }
        }
    });
    $scope.payableAdd.accounts = $scope.itemDataSource.data();

    $scope.itemColumns = [{
        field: "orderNo", title: "单号", width:170,  values:orderNos
    },  {
        field: "accountCode", title: "应付科目", values:receiveExaccts
    }, {
        field: "amount", title: "金额"
    }, {
        field: "confirmAmount", title: "审核金额"
    }, {
        field: "remark", title: "备注"
    },{
        command: "destroy", text: "删除"
    }];

    $scope.gripOptions = {
        toolbar:[
            {   name:"create", text:"新增"    }
        ],
        editable:true
    }

    $scope.init();
}
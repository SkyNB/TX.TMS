var app = angular.module('RoadnetApp');

app.controller('createController', ['$scope', '$rootScope', '$state', createController]);

function createController($scope, $rootScope, $state) {
    $("#create").modal();
    $scope.payableAdd = {}
    $scope.validate = $("#createForm").validate();

    $scope.submit = function(){
        if (!$scope.validate.valid()){   return;   }
        $scope.payableAdd.accounts = $scope.itemDataSource.data();
        //console.dir($scope.payableAdd);
        if($scope.payableAdd.accounts.length == 0){
            toastr.error("请添加应付变更记录");
            return;
        }
        $.ajax({
            url: contextPath+'/payableAdd/createPayableAdd',
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify($scope.payableAdd)
        }).done(function (result) {
            if (result.success) {
                $("#create").modal("hide");
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
        $scope.orderDataSource.data([]);
        $scope.consignOrderDataSource.data([]);
    }

    $scope.$watchCollection('itemDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var totalAmount = 0.0;
        for (var i = 0; i < obj.length; i++) {
            totalAmount += parseFloat(obj[i].amount);
        }
        $scope.payableAdd.totalAmount = totalAmount;
    });

    $scope.consignOrderDataSource = new kendo.data.DataSource({data: []});
    $scope.consignOrderOptions = {
        dataSource: $scope.consignOrderDataSource,
        filter:"contains",
        dataTextField:"text",
        dataValueField:"value",
        change: function(e){
            $.get(contextPath+"/payableAdd/getOrderByConsignOrderNo/" + $scope.payableAdd.carrierCode + "/" + this.value(), function(result){
                if(result != null){
                    $scope.orderDataSource.data(result);
                }
            });
            $scope.itemDataSource.data([]);
        }
    };
    $scope.carrierOptions = {
      change: function(e){
          $.get(contextPath+"/payableAdd/getConsignOrder/" + this.value(), function(result){
              if(result != null){
                  $scope.consignOrderDataSource.data(result);
              }
          });
          $scope.itemDataSource.data([]);
      }
    };
    $scope.orderDataSource = new kendo.data.DataSource({data:[]});

    $scope.itemDataSource = new kendo.data.DataSource({
        data:[],
        schema: {
            model: {
                fields: {
                    orderNo: {type:"string",editable: true},
                    accountCode: {type:"string",editable: true, validation: {required: true}},
                    amount: {type: "number", validation: {min: 1, required: true}, defaultValue: 1, editable: true},
                    remark: {editable: true, validation: {required: false} }
                }
            }
        }
    });

    $scope.itemColumns = [{
        field: "orderNo", title: "单号", width:170, editor: function(container, options){
            var input = $("<input required='required'/>");
            input.attr("name", options.field);
            input.appendTo(container);
            input.kendoComboBox({
                dataTextField: "text",
                dataValueField: "value",
                dataSource: $scope.orderDataSource
            });
        }
    },  {
        field: "accountCode", title: "应付科目", values:receiveExaccts
    }, {
        field: "amount", title: "金额"
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
}
angular.module('RoadnetApp').controller('MergePackageController', ['$rootScope', '$scope', '$filter',
    MergePackageController]);
function MergePackageController($rootScope, $scope, $filter) {
    $scope.package = {};
    if($rootScope.selectedData&&$rootScope.selectedData.length>0){
        $("#mergePackage").modal();
    }
    $("#mergePackage").on("shown.bs.modal",function(){
        $scope.package={};
        $scope.package.orderNos = [];
        var orderNos = "";
        $.each($rootScope.selectedData,function(i,item){
            $scope.package.orderNos.push(item.orderNo);
            if(i===0){
                orderNos+=item.orderNo;
            }else{
                orderNos+=","+ item.orderNo;
            }
        });
        $scope.package.orderNoStr = orderNos;
        $scope.$apply();
    });
    $scope.toolbar = [{name: "create"}];
    $scope.itemDataSource = new kendo.data.DataSource({
        data:[],
        schema: {
            model: {
                fields: {
                    goodsDesc: {type: "string",required:true},
                    packageSize: {type: "string",required:true},
                    wrapMaterial: {type: "string",required:true},
                    itemQty: {
                        type: "number",
                        validation: {pattern: "^\\d+$", min: 1, required: {message: "数量为必填项"}},
                        defaultValue: 1
                    },
                    weight: {
                        type: "string",
                        validation: {pattern: "^\\d+(\\.\\d{0,4})?$",min: 0, required: {message: "重量为必填项"}},
                        defaultValue: 0
                    },
                    volume: {
                        type: "string",
                        validation: {pattern: "^\\d+(\\.\\d{0,6})?$", required: {message: "体积为必填项"}, min: 0},
                        defaultValue: 0
                    }
                }
            }
        }
    });
    $scope.itemColumns = [{
        field: "goodsDesc",
        title: "货物描述"
    },{
        field: "packageSize",
        title: "箱型",
        values:packageSizes
    },{
        field: "wrapMaterial",
        title: "包装材料",
        values:wrapMaterials
    },{
        field: "itemQty",
        title: "商品数量"
    },{
        field: "weight",
        title: "重量"
    },{
        field: "volume",
        title: "体积"
    },{
        command: "destroy"
    }];
    $scope.$watchCollection('itemDataSource', function (newObj, oldObj) {
        var obj = newObj.data();
        var itemQty = 0;
        var totalVolume = 0.0;
        var totalWeight = 0.0;
        for (var i = 0; i < obj.length; i++) {
            itemQty += parseInt(obj[i].itemQty);
            totalVolume += parseFloat(obj[i].volume);
            totalWeight += parseFloat(obj[i].weight);
        }
        $scope.package.totalPackageQty = obj.length;
        $scope.package.totalItemQty = itemQty;
        $scope.package.totalVolume = totalVolume;
        $scope.package.totalWeight = totalWeight;
    });

    $scope.validate = $("#mergePackageForm").validate();
    $scope.submit = function(){
        if(!$scope.validate.valid()){
            return;
        }
        if($scope.package.orderNos.length>1&&$scope.itemDataSource.data().length!=1){
            App.toastr("多个订单只能打成一包","warning");
            return;
        }
        $scope.package.packages=$scope.itemDataSource.data();
        $.ajax({
            url:contextPath+"/package/mergePackage",
            type:"POST",
            contentType:"application/json",
            data:JSON.stringify($scope.package)
        }).done(function(result){
            if(result.success){
                App.toastr("打包成功","success");
                $scope.itemDataSource.data([]);
                $rootScope.selectedData=[];
                $rootScope.lastPageData=[];
                $rootScope.grid.clearSelection();
                $rootScope.data.query();
                // $rootScope.orderPackageData.query();
                $scope.package = {};
                $("#mergePackage").modal("hide");
            }else {
                App.toastr(result.message,"error");
            }
        }).fail(function(r){
            App.toastr("提交失败","error");
        })
    }
}

angular.module('RoadnetApp').controller('PackageDetailsController', ['$rootScope', '$scope', '$stateParams',
    PackageDetailsController]);
function PackageDetailsController($rootScope, $scope, $stateParams) {
    $scope.package = {};
    $scope.init = function(){
        $scope.package={};
        $scope.package.orderNos = [];
        var orderNos = "";
        $scope.package.orderNoStr = orderNos;
        $.ajax({
            url:contextPath+"/package/getByPackageId/"+$stateParams.packageId,
            type:"POST",
            contentType:"application/json"
        }).done(function(result){
            if(result.success){
                $("#detailsPackage").modal();
                $scope.itemDataSource.data(result.body.packages);
                var orderNos = "";
                $.each(result.body.orderNos,function(i,item){
                    if(i===0){
                        orderNos+=item;
                    }else{
                        orderNos+=","+ item;
                    }
                });
                $scope.package.orderNoStr = orderNos;
                $scope.package.packers =result.body.packers;
                $scope.total($scope.itemDataSource);
                $scope.$apply();
            }else {
                App.toastr("查询打包信息失败","error");
            }
        });
    }

    $scope.total =function(dataSource){
        var obj = dataSource.data();
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
    }
    $scope.itemDataSource = new kendo.data.DataSource({
        data:[]
    });
    $scope.itemColumns = [{
        field: "packageNo",
        title: "箱号",width:150
    },{
        field: "goodsDesc",
        title: "货物描述"
    },{
        field: "packageSize", title: "箱型", template: function (dataItem) {
            if (dataItem.packageSize) {
                return dataItem.packageSize.text;
            }
            return "";
        }
    }, {
        field: "wrapMaterial", title: "包装材料", template: function (dataItem) {
            if (dataItem.wrapMaterial) {
                return dataItem.wrapMaterial.text;
            }
            return "";
        }
    }, {
        field: "itemQty",
        title: "商品数量"
    },{
        field: "weight",
        title: "重量"
    },{
        field: "volume",
        title: "体积"
    }];

    $scope.init();
}

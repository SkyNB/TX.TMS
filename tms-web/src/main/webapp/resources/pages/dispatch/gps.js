var app = angular.module('RoadnetApp');

app.config(function ($stateProvider) {
    $stateProvider
        .state('create', {
            url: '/create',
            reload: true,
            templateUrl: contextPath + '/dispatch/create',
            controller: "DispatchCreateController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/create.js'])
        })
        .state('detail/:dispatchId', {
            url: '/detail/:dispatchId',
            reload: true,
            templateUrl: contextPath + '/dispatch/detail',
            controller: "DispatchDetailController",
            resolve: getDeps([contextPath + '/resources/pages/dispatch/detail.js'])
        })
});

app.controller('GpsController', ['$scope', '$rootScope', '$state', GpsController]);
function GpsController($scope, $rootScope, $state) {
    $scope.idleData = new kendo.data.DataSource({data:[]});
    $scope.busyData = new kendo.data.DataSource({data:[]});

    $scope.gridColumns = [{
        field: "driver",
        title: "司机"
    }, {
        field: "vehicleNo",
        title: "车牌号"
    }, {
        field: "driverMobile",
        title: "联系电话"
    }];

    $scope.count = {};
    $scope.init =function(){
        var icon = new BMap.Icon(contextPath+'/resources/layouts/marker.png', new BMap.Size(20, 32), {
            anchor: new BMap.Size(10, 30)
        });
        $.ajax({
            url:contextPath+"/dispatch/getGpsInfos",
            contentType:"application/json"
        }).done(function(result){
            if(result&&result.body){
                var maxX=0,minX=180,maxY=0,minY=90;
                var map = new BMap.Map('container', {minZoom: 6, maxZoom: 36});
                $.each(result.body,function(i,item){
                    if(item.longitude>maxX) maxX=item.longitude;
                    if(item.longitude<minX) minX=item.longitude;
                    if(item.latitude>maxY) maxY=item.latitude;
                    if(item.latitude<minY) minY=item.latitude;
                    var opts = {title: '<span style="font-size:14px;color:#0A8021">'+item.vehicleNo+'</span>'};
                    var infoWindow = new BMap.InfoWindow("<div style='line-height:1.8em;font-size:12px;'>" +
                        "<b>地址:</b>"+item.address +"<br>"+
                        "<a href='#/detail/" + item.dispatchNumber +
                        "' data-target='#dispatchDetail' data-toggle='modal'>详情>></a></div>", opts);
                    var mkr = new BMap.Marker(new BMap.Point(item.longitude, item.latitude), {
                        icon: icon, enableDragging: true, raiseOnDrag: true
                    });
                    mkr.addEventListener("mouseover", function () {
                        this.openInfoWindow(infoWindow);
                    });
                    mkr.openInfoWindow(infoWindow);
                    map.addOverlay(mkr);
                });
                map.centerAndZoom(new BMap.Point((maxX+minX)/2,(maxY+minY)/2), 15);
                map.enableScrollWheelZoom(true);
            }
        });
        $.ajax({
            url:contextPath+"/vehicle/getSiteAvailable",
            contentType:"application/json"
        }).done(function(result){
            if(result&&result.body){
                var idle = [];
                var busy = [];
                $.each(result.body,function(i,item){
                    if(item.status.name=="BUSY"){
                        busy.push(item);
                    }else{
                        idle.push(item);
                    }
                });

                $scope.idleData.data(idle);
                $scope.busyData.data(busy);
            }
        });
        $.get(contextPath+"/dispatch/getOrderCount").done(function(result){
           if(result.success&&result.body){
               $scope.count = result.body;
               $scope.$apply();
           }
        });
    };

    $scope.init();
}

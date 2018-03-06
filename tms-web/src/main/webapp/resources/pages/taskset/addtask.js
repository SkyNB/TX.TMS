'use strict';

var app = angular.module('RoadnetApp');

app.controller('AddtaskManagementController', ['$rootScope', '$scope', '$state', '$stateParams', AddtaskManagementController]);
function AddtaskManagementController($rootScope, $scope, $state, $stateParams) {

       $("#addTask").modal();

       $scope.taskTeam = {};

       $scope.vehicleOptions = {
              dataSource: getComboDatasource('/vehicle/getAvailableForSelect'),
              filter: "contains",
              dataTextField: "text",
              dataValueField: "value"
       };

       $scope.storeOptions = {
              dataSource: getComboDatasource('/store/getBranchAvailable'),
              filter: "contains",
              dataTextField: "name",
              dataValueField: "code"
       };

       $scope.idleData = new kendo.data.DataSource({data:[{"code":"1914 华强茂业ONLY"},{"code":"1914 华强茂业ONLY"}]});

       $scope.gridColumns = [{
              field: "code",
              title: "门店",
              width:"600"

       }, {
              field: "",
              title: "操作",
              width:"200",
              template: "<button type='button' class='k-button k-button-icontext k-grid-delete'  /> 删除</button>"


       } ];


       $scope.addition=function(){
              if( $('#searchStore').val() ==""){
                     App.toastr("门店不能为空！");
                     return;
              }
                     var combobox = $("#searchStore").data("kendoComboBox").text();
                     var Storeval = $('#searchStore').val();
                     var node = Storeval + "\t" + combobox;
                     $scope.idleData.data().push({"code": node});

              if(node !==""){
                     $('#searchStore').val("");
                     App.toastr("请重新选择门店！");
                     return ;
              }
       };

       $scope.validate = $("#addCustomerForm").validate();

       $scope.reset = function () {
              $scope.customer = {};
       };

       $scope.submit = function () {
              if (!$scope.validate.valid())
                     return;
              $scope.taskTeam.brands = $("#searchStore").data("kendoGrid").dataSource.data();
              $.ajax({
                     url: contextPath + "/taskset/addtask",
                     type: "POST",
                     contentType: "application/json",
                     data: JSON.stringify($scope.taskTeam)
              }).done(function (result) {
                     if (result.success) {
                            $("#addTask").modal("hide");
                            $scope.reset();
                            $rootScope.data.query();
                     } else {
                            toastr.error("保存失败！" + result.message);
                     }
              }).fail(function(){
                     App.toastr("提交失败","error");
              });
       };

}

var app = angular.module('RoadnetApp');

app.controller('rejectController', ['$scope', '$rootScope', '$state', '$stateParams', rejectController]);

function rejectController($scope, $rootScope, $state, $stateParams) {

    $scope.submit = function(){
        $.ajax({
            url: contextPath+'/payableAdd/rejectPayableAdd',
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({"payableAddId":$stateParams.payableAddId, "rejectedNotes": $scope.rejectedNotes})
        }).done(function (result) {
            if (result.success) {
                $("#reject").modal("hide");
                $scope.rejectedNotes = "";
                $rootScope.data.query();
            } else {
                toastr.error("保存失败！" + result.message);
            }
        }).fail(function(){
            App.toastr("提交失败","error");
        });
    }

    $("#reject").modal();
}
var isAjax = false;

var remindersApp = angular.module('RemindersApp', []);

remindersApp.controller('RemindersEditCtrl', function($scope, $http) {

    var origItem = {};
    
    var BASE_URL = "reminders";

    $scope.predicate = 'createdAt';

    $scope.editMode = true;

    $scope.fetchItem = function() {
        var itemId = QueryString['id'];
        if (itemId == null) {
            $scope.item = {};
            origItem = {};
        } else {
            $http.get(BASE_URL + "/" + QueryString['id']).success(function(item) {
                $scope.item = item;
                origItem = $.extend({}, item);
            });
        }
    };

    $scope.addItem = function(item) {
        $("#form").submit();
    };

    $scope.updateItem = function(item) {
        $("#form").submit();
    };

    $scope.editItem = function(item) {
        $scope.resetError();
        $scope.item = item;
        $scope.editMode = true;
    };

    $scope.resetForm = function() {
        $scope.resetError();
        $scope.item = $.extend({}, origItem);
        $scope.editMode = false;
    };

    $scope.resetError = function() {
        $scope.error = false;
        $scope.errorMessage = '';
    };

    $scope.setError = function(message) {
        $scope.error = true;
        $scope.errorMessage = message;
    };


    $scope.fetchItem();

});

// from stackoverflow #979975
var QueryString = function() {
    var query_string = {};
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (typeof query_string[pair[0]] === "undefined") {
            query_string[pair[0]] = pair[1];
        } else if (typeof query_string[pair[0]] === "string") {
            var arr = [query_string[pair[0]], pair[1]];
            query_string[pair[0]] = arr;
        } else {
            query_string[pair[0]].push(pair[1]);
        }
    }
    return query_string;
}();

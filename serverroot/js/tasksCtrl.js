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

angular.module('taskStatusFilter', []).filter('taskstatus', function() {
  return function(input) {
    if (input == "IN_PROGRESS") {
        return "in progress...";
    } else if (input == "COMPLETED") {
        return "completed";
    } else {
        return "time is due";
    }
  };
});

var isAjax = (QueryString['premium'] != undefined);

var tasksApp = angular.module('TasksApp', ['taskStatusFilter']);

tasksApp.controller('TasksCtrl', function($scope, $http) {
    
    var newTaskBtn = $("<button>").addClass("btn btn-primary").click(function(){window.location = '/task_editor.html'}).html("New task");
    var staticMenu = $("<div>").append(newTaskBtn);

    var BASE_URL = "tasks"
    var deletedItem = {}

    $scope.predicate = 'createdAt';

    $scope.item = {};
    $scope.deleteNotification = false;
    $scope.editMode = false;

    $scope.fetchList = function() {
        $http.get(BASE_URL).success(function(items) {
            $scope.itemsList = items;
        }).error(function(res) {
            $scope.setError('Error while fetching items. ' + res);
        });
    }

    $scope.addItem = function(item) {

        $scope.resetError();

        $http.post(BASE_URL, item).success(function() {
            $scope.fetchList();
            $scope.resetForm();
        }).error(function(res) {
            $scope.setError('Could not add a new item. ' + res);
        });
    }

    $scope.updateItem = function(item) {
        $scope.resetError();
        $http.put(BASE_URL, item).success(function() {
            $scope.resetForm();
            $scope.fetchList();
        }).error(function(res) {
            $scope.setError('Could not update the item ' + res);
            $scope.fetchList();
        });
    }

    $scope.remove = function() {
        $scope.resetError();

        $http.delete(BASE_URL + "/" + deletedItem['id']).success(function() {
            $scope.fetchList();
        }).error(function(res) {
            $scope.setError('Could not remove item ' + res);
        });
        $scope.showDeleteNotification(false);
    }

    $scope.resetForm = function() {
        $scope.resetError();
        $scope.item = {};
        $scope.editMode = false;
    }

    $scope.resetError = function() {
        $scope.error = false;
        $scope.errorMessage = '';
    }

    $scope.setError = function(message) {
        $scope.error = true;
        $scope.errorMessage = message;
    }

    $scope.showDeleteNotification = function(show, item) {
        if (show === true) {
            $scope.deleteNotification = true;
        } else {
            $scope.deleteNotification = false;
        }
        deletedItem = item;
    };

    $scope.fetchList();

    if (!isAjax) {
        $scope.editItem = function(item) {
            window.location = "/task_editor.html?id=" + item.id;
        };
        $("#editor").hide();
        $("#wrapper").append(staticMenu);
    }

});

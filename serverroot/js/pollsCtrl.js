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

var filters = angular.module('pollsFilters', []);
        
filters.filter('recipients_filter', function() {
  return function(input) {
    return input.replace(/:null/g, " hasn't voted").replace(/:/g, " voted - ");
  };
});

filters.filter('answers_filter', function() {
  return function(input) {
      var output = [];
      var splitted = input.split('\r\n');
      for (var i = 0; i < splitted.length; i++) {
          var line = splitted[i];
          if (line == "") {
              continue;
          }
          output.push(i + " - " + line);
      }
      return output.join("\r\n");
  };
});

var isAjax = (QueryString['premium'] != undefined);

var pollsApp = angular.module('PollsApp', ['pollsFilters']);

pollsApp.controller('PollsCtrl', function($scope, $http) {
    
    var newPollBtn = $("<button>").addClass("btn btn-primary").click(function(){window.location = '/poll_editor.html'}).html("New poll");
    var staticMenu = $("<div>").append(newPollBtn);

    var BASE_URL = "polls";
    var deletedItem = {};

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
    };

    $scope.addItem = function(item) {

        $scope.resetError();

        $http.post(BASE_URL, item).success(function() {
            $scope.fetchList();
            $scope.resetForm();
        }).error(function(res) {
            $scope.setError('Could not add a new item. ' + res);
        });
    };

    $scope.updateItem = function(item) {
        $scope.resetError();
        $http.put(BASE_URL, item).success(function() {
            $scope.resetForm();
            $scope.fetchList();
        }).error(function(res) {
            $scope.setError('Could not update the item ' + res);
            $scope.fetchList();
        });
    };

    $scope.remove = function() {
        $scope.resetError();

        $http.delete(BASE_URL + "/" + deletedItem['id']).success(function() {
            $scope.fetchList();
        }).error(function(res) {
            $scope.setError('Could not remove item ' + res);
        });
        $scope.showDeleteNotification(false);
    };

    $scope.resetForm = function() {
        $scope.resetError();
        $scope.item = {};
        $scope.editMode = false;
    };

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
            window.location = "/poll_editor.html?id=" + item.id;
        };
        $("#editor").hide();
        $("#wrapper").append(staticMenu);
    }

});

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Partecipant
 */

angular.module('flowableModeler').controller('FlowablePartecipantCtrl',
    ['$scope', '$modal', '$timeout', '$translate', function ($scope, $modal, $timeout, $translate) {

        // Config for the modal window
        var opts = {
            template: 'editor-app/configuration/properties/partecipant-popup.html?version=' + Date.now(),
            scope: $scope
        };

        // Open the dialog
        _internalCreateModal(opts, $modal, $scope);
    }]);

angular.module('flowableModeler').controller('FlowablePartecipantPopupCtrl',
    ['$rootScope', '$scope', '$http', 'editorManager', function ($rootScope, $scope, $http, editorManager) {

        $scope.model = {
            loading: false,
            process: {
                 name: '',
                 key: '',
                 description: '',
                    modelType: 0
            }
        };

        this.currentSelectedShape = $rootScope.currentSelectedShape;

        $scope.state = {'loadingProcesses' : true, 'error' : false};

        $scope.loadProcesses = function() {

            $http.get(FLOWABLE.APP_URL.getModelsUrl("?modelType=0"))
    		.success(
    			function(response) {
    				$scope.state.loadingProcesses = false;
    				$scope.state.error = false;
                    $scope.processModels = response.data;
    			})
    		.error(
    			function(data, status, headers, config) {
    				$scope.state.loadingProcesses = false;
    				$scope.state.error = true;
    			});
              
        };

         // Saving the selected value
        $scope.save = function() {
            var selectedShapes = editorManager.getSelection();
            if(selectedShapes){
                 var selectedShape = selectedShapes[0];
                 var labels = selectedShape.getLabels();
                for (var i = 0; i < labels.length; i++){
                    if(labels[i].id.endsWith("rec_part_name")){
                        labels[i]._text = $scope.property.value;
                        labels[i]._isChanged = true;
                    }
                }
                //if process doesn't exist we have to create a new one
                if($scope.processModels){
                    var createNewProcess = true;
                    for (var i = 0; i < $scope.processModels.length; i++){
                        if($scope.processModels[i].name == $scope.property.value){
                            createNewProcess = false; 
                        }
                    }
                }
                if(createNewProcess){
                    //create new process
                    $scope.createNewProcess();
                }
                selectedShape.labels = labels;
                editorManager.selectedShapes = [selectedShape];
            } 
            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        // Close button handler
        $scope.close = function () {
            $scope.$hide();
            $scope.property.mode = 'read';
        };

        //create new process
        $scope.createNewProcess = function(){
            $scope.model.process.name = $scope.property.value;
            $scope.model.process.key = $scope.property.value;

            $http({method: 'POST', url: FLOWABLE.APP_URL.getModelsUrl(), data: $scope.model.process}).
            success(function(data) {
                $scope.model.loading = false;
               
            }).
            error(function(data, status, headers, config) {
                $scope.model.loading = false;
                $scope.model.errorMessage = data.message;
            });
        }

        $scope.loadProcesses(); 

    }]);

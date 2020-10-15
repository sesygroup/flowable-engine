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
package org.flowable.ui.modeler.rest.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.idm.api.User;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.common.service.exception.ConflictingRequestException;
import org.flowable.ui.common.service.exception.InternalServerErrorException;
import org.flowable.ui.common.util.XmlUtil;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.model.ModelKeyRepresentation;
import org.flowable.ui.modeler.model.ModelRepresentation;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.service.ModelImageService;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector.Bpmn2ChoreographyProjector;
import eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector.Bpmn2ChoreographyProjectorException;
import eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector.Bpmn2ChoreographyProjectorRequest;
import eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector.Bpmn2ChoreographyProjectorResponse;


@RestController
@RequestMapping("/app")
public class ModelResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelResource.class);

    private static final String RESOLVE_ACTION_OVERWRITE = "overwrite";
    private static final String RESOLVE_ACTION_SAVE_AS = "saveAs";
    private static final String RESOLVE_ACTION_NEW_VERSION = "newVersion";

    @Autowired
    protected ModelService modelService;

    @Autowired
    protected ModelRepository modelRepository;

    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected ModelImageService modelImageService;

    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

    protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    /**
     * GET /rest/models/{modelId} -> Get process model
     */
    @RequestMapping(value = "/rest/models/{modelId}", method = RequestMethod.GET, produces = "application/json")
    public ModelRepresentation getModel(@PathVariable String modelId) {
        return modelService.getModelRepresentation(modelId);
    }

    /**
     * GET /rest/models/{modelId}/thumbnail -> Get process model thumbnail
     */
    @RequestMapping(value = "/rest/models/{modelId}/thumbnail", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getModelThumbnail(@PathVariable String modelId) {
        Model model = modelService.getModel(modelId);
        return model.getThumbnail();
    }

    /**
     * PUT /rest/models/{modelId} -> update process model properties
     */
    @RequestMapping(value = "/rest/models/{modelId}", method = RequestMethod.PUT)
    public ModelRepresentation updateModel(@PathVariable String modelId, @RequestBody ModelRepresentation updatedModel) {
        // Get model, write-permission required if not a favorite-update
        Model model = modelService.getModel(modelId);

        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), updatedModel.getKey());
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Model with provided key already exists " + updatedModel.getKey());
        }

        try {
            updatedModel.updateModel(model);
            
            if (model.getModelType() != null) {
                ObjectNode modelNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
                modelNode.put("name", model.getName());
                modelNode.put("key", model.getKey());

                if (Model.MODEL_TYPE_BPMN == model.getModelType()) {
                    ObjectNode propertiesNode = (ObjectNode) modelNode.get("properties");
                    propertiesNode.put("process_id", model.getKey());
                    propertiesNode.put("name", model.getName());
                    if (StringUtils.isNotEmpty(model.getDescription())) {
                        propertiesNode.put("documentation", model.getDescription());
                    }
                    modelNode.set("properties", propertiesNode);
                }
                model.setModelEditorJson(modelNode.toString());
            }
            
            modelRepository.save(model);

            ModelRepresentation result = new ModelRepresentation(model);
            return result;

        } catch (Exception e) {
            throw new BadRequestException("Model cannot be updated: " + modelId);
        }
    }

    /**
     * DELETE /rest/models/{modelId} -> delete process model or, as a non-owner, remove the share info link for that user specifically
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/rest/models/{modelId}", method = RequestMethod.DELETE)
    public void deleteModel(@PathVariable String modelId) {

        // Get model to check if it exists, read-permission required for delete
        Model model = modelService.getModel(modelId);

        try {
            modelService.deleteModel(model.getId());

        } catch (Exception e) {
            LOGGER.error("Error while deleting: ", e);
            throw new BadRequestException("Model cannot be deleted: " + modelId);
        }
    }

    /**
     * GET /rest/models/{modelId}/editor/json -> get the JSON model
     */
    @RequestMapping(value = "/rest/models/{modelId}/editor/json", method = RequestMethod.GET, produces = "application/json")
    public ObjectNode getModelJSON(@PathVariable String modelId) {
        Model model = modelService.getModel(modelId);
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put("modelId", model.getId());
        modelNode.put("name", model.getName());
        modelNode.put("key", model.getKey());
        modelNode.put("description", model.getDescription());
        modelNode.putPOJO("lastUpdated", model.getLastUpdated());
        modelNode.put("lastUpdatedBy", model.getLastUpdatedBy());
        if (StringUtils.isNotEmpty(model.getModelEditorJson())) {
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
                editorJsonNode.put("modelType", "model");
                modelNode.set("model", editorJsonNode);
            } catch (Exception e) {
                LOGGER.error("Error reading editor json {}", modelId, e);
                throw new InternalServerErrorException("Error reading editor json " + modelId);
            }

        } else {
            ObjectNode editorJsonNode = objectMapper.createObjectNode();
            editorJsonNode.put("id", "canvas");
            editorJsonNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorJsonNode.put("modelType", "model");
            modelNode.set("model", editorJsonNode);
        }
        return modelNode;
    }

    /**
     * POST /rest/models/{modelId}/editor/json -> save the JSON model
     */
    @RequestMapping(value = "/rest/models/{modelId}/editor/json", method = RequestMethod.POST)
    public ModelRepresentation saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {

        // Validation: see if there was another update in the meantime
        long lastUpdated = -1L;
        String lastUpdatedString = values.getFirst("lastUpdated");
        if (lastUpdatedString == null) {
            throw new BadRequestException("Missing lastUpdated date");
        }
        try {
            Date readValue = objectMapper.getDeserializationConfig().getDateFormat().parse(lastUpdatedString);
            lastUpdated = readValue.getTime();
        } catch (ParseException e) {
            throw new BadRequestException("Invalid lastUpdated date: '" + lastUpdatedString + "'");
        }

        Model model = modelService.getModel(modelId);
        User currentUser = SecurityUtils.getCurrentUserObject();
        boolean currentUserIsOwner = model.getLastUpdatedBy().equals(currentUser.getId());
        String resolveAction = values.getFirst("conflictResolveAction");

        // If timestamps differ, there is a conflict or a conflict has been resolved by the user (no for projection - coreography)
        if (model.getLastUpdated().getTime() != lastUpdated && 
        		!(model.getModelType() == AbstractModel.MODEL_TYPE_CHOREOGRAPHY || model.getModelType() == AbstractModel.MODEL_TYPE_PROJECTION)) {

            if (RESOLVE_ACTION_SAVE_AS.equals(resolveAction)) {

                String saveAs = values.getFirst("saveAs");
                String json = values.getFirst("json_xml");
                return createNewModel(saveAs, model.getDescription(), model.getModelType(), json);

            } else if (RESOLVE_ACTION_OVERWRITE.equals(resolveAction)) {
                return updateModel(model, values, false);
            } else if (RESOLVE_ACTION_NEW_VERSION.equals(resolveAction)) {
                return updateModel(model, values, true);
            } else {

                // Exception case: the user is the owner and selected to create a new version
                String isNewVersionString = values.getFirst("newversion");
                if (currentUserIsOwner && "true".equals(isNewVersionString)) {
                    return updateModel(model, values, true);
                } else {
                    // Tried everything, this is really a conflict, return 409
                    ConflictingRequestException exception = new ConflictingRequestException("Process model was updated in the meantime");
                    exception.addCustomData("userFullName", model.getLastUpdatedBy());
                    exception.addCustomData("newVersionAllowed", currentUserIsOwner);
                    throw exception;
                }
            }

        } else {

            // Actual, regular, update
            return updateModel(model, values, false);

        }
    }

    /**
     * POST /rest/models/{modelId}/editor/newversion -> create a new model version
     */
    @RequestMapping(value = "/rest/models/{modelId}/newversion", method = RequestMethod.POST)
    public ModelRepresentation importNewVersion(@PathVariable String modelId, @RequestParam("file") MultipartFile file) {
        InputStream modelStream = null;
        try {
            modelStream = file.getInputStream();
        } catch (Exception e) {
            throw new BadRequestException("Error reading file inputstream", e);
        }

        return modelService.importNewVersion(modelId, file.getOriginalFilename(), modelStream);
    }

    protected ModelRepresentation updateModel(Model model, MultiValueMap<String, String> values, boolean forceNewVersion) {

        String name = values.getFirst("name");
        String key = values.getFirst("key").replaceAll(" ", "");
        String description = values.getFirst("description");
        String isNewVersionString = values.getFirst("newversion");
        String newVersionComment = null;

        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), key);
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Model with provided key already exists " + key);
        }

        boolean newVersion = false;
        if (forceNewVersion) {
            newVersion = true;
            newVersionComment = values.getFirst("comment");
        } else {
            if (isNewVersionString != null) {
                newVersion = "true".equals(isNewVersionString);
                newVersionComment = values.getFirst("comment");
            }
        }

        String json = values.getFirst("json_xml");
        
        //-- logic about save or update projection - coreography --
        if(values.getFirst("participant") != null && !values.getFirst("participant").equals("undefined")) {
        	
        	Model projection = modelService.getProjection(values.getFirst("participant"), model.getId());
        	Bpmn2ChoreographyProjector bpmn2ChoreographyProjector = new Bpmn2ChoreographyProjector();
            Bpmn2ChoreographyProjectorRequest bpmn2ChoreographyProjectorRequest = new Bpmn2ChoreographyProjectorRequest();
            bpmn2ChoreographyProjectorRequest.setParticipantUsedToBpmn2Projection(values.getFirst("participant"));
            BpmnModel bpmnModel = modelService.getBpmnModel(model);
			byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
			//test
//			Bpmn2ChoreographyProjectorResponse response = null;
			try {
//			File BPMN2File = new File("test_01.bpmn20.xml");
//			bpmn2ChoreographyProjectorRequest.setBpmn2Content(FileUtils.readFileToByteArray(BPMN2File));
			FileUtils.writeByteArrayToFile(new File("test_01.xml"), xml);
			} catch (Exception e) {
				// TODO: handle exception
				String g;
			}
			//test end
			bpmn2ChoreographyProjectorRequest.setBpmn2Content(xml);
			Bpmn2ChoreographyProjectorResponse response = null;
			try {
				response = bpmn2ChoreographyProjector.project(bpmn2ChoreographyProjectorRequest);
			} catch (Bpmn2ChoreographyProjectorException e) {
				LOGGER.error("Error saving model {}", model.getId(), e);
                throw new BadRequestException("Process model could not be saved " + model.getId());
			}
			BpmnModel bpmnProjection = null;
			try {
				FileUtils.writeByteArrayToFile(new File("temporary.xml"), response.getBpmn2Content());
	            XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
	            InputStream xmlIn = new FileInputStream("temporary.xml");
	            XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
	            bpmnProjection = new BpmnXMLConverter().convertToChoreographyBpmnModel(xtr);
	            //test
	            //byte[] xmlt = new BpmnXMLConverter().convertToXML(bpmnProjection);
	            //FileUtils.writeByteArrayToFile(new File("temporary.xml"), xmlt);
	            //test end
			} catch (FileNotFoundException e) {
				LOGGER.error("Error saving model {}", model.getId(), e);
                throw new BadRequestException("Process model could not be saved " + model.getId());
			} catch (IOException e) {
				LOGGER.error("Error saving model {}", model.getId(), e);
                throw new BadRequestException("Process model could not be saved " + model.getId());
			} catch (XMLStreamException e) {
				LOGGER.error("Error saving model {}", model.getId(), e);
                throw new BadRequestException("Process model could not be saved " + model.getId());
			}
			
			Model modelProjection = new Model();
			
			//-- add graphic propoerties for gateway with different id --
			for (FlowElement elementGraphic : bpmnProjection.getProcesses().get(0).getFlowElements()) {
				if(bpmnModel.getGraphicInfo(elementGraphic.getId()) == null) {
					if(elementGraphic instanceof FlowNode) {
						FlowNode gatewayGraphic = (FlowNode) elementGraphic;
						if(!gatewayGraphic.getIncomingFlows().isEmpty() && gatewayGraphic.getIncomingFlows().get(0) != null) {
							for (FlowElement takeGraphic : bpmnModel.getProcesses().get(0).getFlowElements()) {
								if(takeGraphic instanceof FlowNode) {
									FlowNode takeGatewayGraphic = (FlowNode) takeGraphic;
									if(takeGatewayGraphic.getIncomingFlows() != null) {
										for (SequenceFlow sequenceTakeGraphic : takeGatewayGraphic.getIncomingFlows()) {
											for (SequenceFlow sequenceGraphic :  gatewayGraphic.getIncomingFlows()) {
												if(sequenceGraphic.getId().equals(sequenceTakeGraphic.getId())) {
													bpmnModel.getLocationMap().put(elementGraphic.getId(), bpmnModel.getGraphicInfo(takeGraphic.getId()));
													bpmnModel.getFlowLocationMap().put(elementGraphic.getId(), bpmnModel.getFlowLocationGraphicInfo(takeGraphic.getId()));
												}
											}
										}
									}
								}
							}
						}
						if(!gatewayGraphic.getOutgoingFlows().isEmpty() && gatewayGraphic.getOutgoingFlows().get(0) != null) {
							for (FlowElement takeGraphic : bpmnModel.getProcesses().get(0).getFlowElements()) {
								if(takeGraphic instanceof FlowNode) {
									FlowNode takeGatewayGraphic = (FlowNode) takeGraphic;
									if(takeGatewayGraphic.getOutgoingFlows() != null) {
										for (SequenceFlow sequenceTakeGraphic : takeGatewayGraphic.getOutgoingFlows()) {
											for (SequenceFlow sequenceGraphic :  gatewayGraphic.getOutgoingFlows()) {
												if(sequenceGraphic.getId().equals(sequenceTakeGraphic.getId())) {
													bpmnModel.getLocationMap().put(elementGraphic.getId(), bpmnModel.getGraphicInfo(takeGraphic.getId()));
													bpmnModel.getFlowLocationMap().put(elementGraphic.getId(), bpmnModel.getFlowLocationGraphicInfo(takeGraphic.getId()));
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(projection != null) {
        		modelProjection.setId(projection.getId());
        		BpmnModel projectionBpmnRead = modelService.getBpmnModel(projection);
//        		bpmnProjection.setLocationMap(projectionBpmnRead.getLocationMap());
//        		bpmnProjection.setFlowLocationMap(projectionBpmnRead.getFlowLocationMap());
        		//TODO rendere effettivi i commenti sopra e cancellare le due linee di codice sotto
        		bpmnProjection.setLocationMap(bpmnModel.getLocationMap());
        		bpmnProjection.setFlowLocationMap(bpmnModel.getFlowLocationMap());
        	} else {
        		bpmnProjection.setLocationMap(bpmnModel.getLocationMap());
        		bpmnProjection.setFlowLocationMap(bpmnModel.getFlowLocationMap());
        	}
			
			ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnProjection);

			String keyProjection = model.getKey() + "_" + values.getFirst("participant");
            String nameProjection = model.getName() + "_" + values.getFirst("participant");

            
            modelProjection.setKey(keyProjection);
            modelProjection.setName(nameProjection);
            modelProjection.setDescription(model.getDescription());
            modelProjection.setModelType(AbstractModel.MODEL_TYPE_PROJECTION);
            modelProjection.setParticipant(values.getFirst("participant"));
            modelProjection.setModelRef(model.getId());
            modelProjection.setVersion(1);
            modelProjection.setCreated(Calendar.getInstance().getTime());
            modelProjection.setCreatedBy(SecurityUtils.getCurrentUserObject().getId());
            modelProjection.setModelEditorJson(modelNode.toString());
            modelProjection.setLastUpdated(Calendar.getInstance().getTime());
            modelProjection.setLastUpdatedBy(SecurityUtils.getCurrentUserObject().getId());
            modelProjection.setTenantId(model.getTenantId());
        	
        	// Thumbnail
            byte[] thumbnail = modelImageService.generateThumbnailImage(model, modelNode);
            if (thumbnail != null) {
            	modelProjection.setThumbnail(thumbnail);
            }
        	modelRepository.save(modelProjection);
        	
        	return new ModelRepresentation(modelProjection);
   
        } else {
        	try {
    			ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(json);

    			ObjectNode propertiesNode = (ObjectNode) editorJsonNode.get("properties");
    			String processId = key;
    			propertiesNode.put("process_id", processId);
    			propertiesNode.put("name", name);
    			if (StringUtils.isNotEmpty(description)) {
    				propertiesNode.put("documentation", description);
    			}
    			editorJsonNode.set("properties", propertiesNode);
                model = modelService.saveModel(model.getId(), name, key, description, editorJsonNode.toString(), newVersion,
                        newVersionComment, SecurityUtils.getCurrentUserObject());
                return new ModelRepresentation(model);

            } catch (Exception e) {
                LOGGER.error("Error saving model {}", model.getId(), e);
                throw new BadRequestException("Process model could not be saved " + model.getId());
            }
        }
    }

    protected ModelRepresentation createNewModel(String name, String description, Integer modelType, String editorJson) {
        ModelRepresentation model = new ModelRepresentation();
        model.setName(name);
        model.setDescription(description);
        model.setModelType(modelType);
        Model newModel = modelService.createModel(model, editorJson, SecurityUtils.getCurrentUserObject());
        return new ModelRepresentation(newModel);
    }
}

package org.flowable.editor.language.json.converter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.ChoreographyTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChoreographyTaskJsonConverter extends BaseBpmnJsonConverter{

	 public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

	        fillJsonTypes(convertersToBpmnMap);
	        fillBpmnTypes(convertersToJsonMap);
	    }

	    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
	        convertersToBpmnMap.put(STENCIL_TASK_CHOREOGRAPHY, ChoreographyTaskJsonConverter.class);
	    }

	    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
	    	convertersToJsonMap.put(ChoreographyTask.class, ChoreographyTaskJsonConverter.class);
	    }

	    @Override
	    protected String getStencilId(BaseElement baseElement) {
	        return STENCIL_TASK_CHOREOGRAPHY;
	    }

		@Override
		protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
			ChoreographyTask choreographyTask = (ChoreographyTask) baseElement;
			
			if(StringUtils.isNotEmpty(choreographyTask.getInitiatingPartecipant())) {
				propertiesNode.put(PROPERTY_INITIATING_PARTECIPANT_REF, choreographyTask.getInitiatingPartecipant());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getPartecipant())) {
				propertiesNode.put(PROPERTY_PERTECIPANT_REF, choreographyTask.getPartecipant());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getInitiatingMessage())) {
				propertiesNode.put(PROPERTY_INITIATING_MESSAGE, choreographyTask.getInitiatingMessage());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getReturnMessage())) {
				propertiesNode.put(PROPERTY_RETURN_MESSAGE, choreographyTask.getReturnMessage());
			}
		}

		@Override
		protected BaseElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode,
				Map<String, JsonNode> shapeMap) {
			ChoreographyTask choreographyTask = new ChoreographyTask();
			
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_INITIATING_PARTECIPANT_REF, elementNode))) {
				 choreographyTask.setInitiatingPartecipant(getPropertyValueAsString(PROPERTY_INITIATING_PARTECIPANT_REF, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE, elementNode))) {
				 choreographyTask.setInitiatingMessage(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE, elementNode))) {
				 choreographyTask.setReturnMessage(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE, elementNode));
		     }
			
			return choreographyTask;
		}
}

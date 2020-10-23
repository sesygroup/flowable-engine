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
			if(StringUtils.isNotEmpty(choreographyTask.getInitiatingPartecipantId())) {
				propertiesNode.put(PROPERTY_INITIATING_PARTECIPANT_ID, choreographyTask.getInitiatingPartecipantId());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getPartecipantId())) {
				propertiesNode.put(PROPERTY_PERTECIPANT_ID, choreographyTask.getPartecipantId());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getInitiatingMessageId())) {
				propertiesNode.put(PROPERTY_INITIATING_MESSAGE_ID, choreographyTask.getInitiatingMessageId());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getReturnMessageId())) {
				propertiesNode.put(PROPERTY_RETURN_MESSAGE_ID, choreographyTask.getReturnMessageId());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getInitiatingMessageFlowRef())) {
				propertiesNode.put(PROPERTY_INITIATING_MESSAGE_FLOW_REF, choreographyTask.getInitiatingMessageFlowRef());
			}
			if(StringUtils.isNotEmpty(choreographyTask.getReturnMessageFlowRef())) {
				propertiesNode.put(PROPERTY_RETURN_MESSAGE_FLOW_REF, choreographyTask.getReturnMessageFlowRef());
			}
		}

		@Override
		protected BaseElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode,
				Map<String, JsonNode> shapeMap) {
			ChoreographyTask choreographyTask = new ChoreographyTask();
			
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_INITIATING_PARTECIPANT_REF, elementNode))) {
				 choreographyTask.setInitiatingPartecipant(getPropertyValueAsString(PROPERTY_INITIATING_PARTECIPANT_REF, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_PERTECIPANT_REF, elementNode))) {
				 choreographyTask.setPartecipant(getPropertyValueAsString(PROPERTY_PERTECIPANT_REF, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE, elementNode))) {
				 choreographyTask.setInitiatingMessage(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE, elementNode))) {
				 choreographyTask.setReturnMessage(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_INITIATING_PARTECIPANT_ID, elementNode))) {
				 choreographyTask.setInitiatingPartecipantId(getPropertyValueAsString(PROPERTY_INITIATING_PARTECIPANT_ID, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_PERTECIPANT_ID, elementNode))) {
				 choreographyTask.setPartecipantId(getPropertyValueAsString(PROPERTY_PERTECIPANT_ID, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE_ID, elementNode))) {
				 choreographyTask.setInitiatingMessageId(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE_ID, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE_ID, elementNode))) {
				 choreographyTask.setReturnMessageId(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE_ID, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE_FLOW_REF, elementNode))) {
				 choreographyTask.setInitiatingMessageFlowRef(getPropertyValueAsString(PROPERTY_INITIATING_MESSAGE_FLOW_REF, elementNode));
		     }
			 if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE_FLOW_REF, elementNode))) {
				 choreographyTask.setReturnMessageFlowRef(getPropertyValueAsString(PROPERTY_RETURN_MESSAGE_FLOW_REF, elementNode));
		     }
			
			return choreographyTask;
		}
}

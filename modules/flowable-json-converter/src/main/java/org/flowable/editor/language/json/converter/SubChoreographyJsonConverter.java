package org.flowable.editor.language.json.converter;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.SubChoreography;
import org.flowable.bpmn.model.SubProcess;
import org.flowable.bpmn.model.Transaction;
import org.flowable.bpmn.model.ValuedDataObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SubChoreographyJsonConverter extends BaseBpmnJsonConverter {

	public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
            Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_SUB_CHOREOGRAPHY, SubChoreographyJsonConverter.class);
        convertersToBpmnMap.put(STENCIL_COLLAPSED_SUB_CHOREOGRAPHY, SubChoreographyJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(SubChoreography.class, SubChoreographyJsonConverter.class);
        convertersToJsonMap.put(Transaction.class, SubChoreographyJsonConverter.class);
    }
    
    @Override
    protected String getStencilId(BaseElement baseElement) {
        //see http://forum.flowable.org/t/collapsed-subprocess-navigation-in-the-web-based-bpmn-modeler/138/19
        GraphicInfo graphicInfo = model.getGraphicInfo(baseElement.getId());
        Boolean isExpanded = graphicInfo.getExpanded();
        if (isExpanded != null && isExpanded == false) {
            return STENCIL_COLLAPSED_SUB_CHOREOGRAPHY;
        } else {
            return STENCIL_SUB_CHOREOGRAPHY;
        }
    }
    
	@Override
	protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
		SubChoreography subProcess = (SubChoreography) baseElement;

        propertiesNode.put("activitytype", getStencilId(baseElement));
        GraphicInfo gi = model.getGraphicInfo(baseElement.getId());
        Boolean isExpanded = gi.getExpanded();
        
        ArrayNode subProcessShapesArrayNode = objectMapper.createArrayNode();
        GraphicInfo graphicInfo = model.getGraphicInfo(subProcess.getId());
        
        if (isExpanded != null && isExpanded == false) {
            processor.processFlowElements(subProcess, model, subProcessShapesArrayNode, null, null, 0, 0);
        } else {
            processor.processFlowElements(subProcess, model, subProcessShapesArrayNode,null, null,
            		graphicInfo.getX(), graphicInfo.getY());
        }
        
        flowElementNode.set("childShapes", subProcessShapesArrayNode);

//        if (subProcess instanceof Transaction) {
//            propertiesNode.put("istransaction", true);
//        }
        
        if(StringUtils.isNotEmpty(subProcess.getInitiatingPartecipant())) {
			propertiesNode.put(PROPERTY_INITIATING_PARTECIPANT_REF, subProcess.getInitiatingPartecipant());
		}
		if(subProcess.getPartecipant() != null && !subProcess.getPartecipant().isEmpty()) {
			propertiesNode.put(PROPERTY_PERTECIPANT_REF, subProcess.getPartecipant());
		}
        BpmnJsonConverterUtil.convertDataPropertiesToJson(subProcess.getDataObjects(), propertiesNode);
		
	}

	@Override
	protected BaseElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode,
			Map<String, JsonNode> shapeMap) {
		 SubProcess subProcess = null;
	        if (getPropertyValueAsBoolean("istransaction", elementNode)) {
	            subProcess = new Transaction();

	        } else {
	            subProcess = new SubProcess();
	        }

	        JsonNode childShapesArray = elementNode.get(EDITOR_CHILD_SHAPES);
	        processor.processJsonElements(childShapesArray, modelNode, subProcess, shapeMap, null, null, model);

	        JsonNode processDataPropertiesNode = elementNode.get(EDITOR_SHAPE_PROPERTIES).get(PROPERTY_DATA_PROPERTIES);
	        if (processDataPropertiesNode != null) {
	            List<ValuedDataObject> dataObjects = BpmnJsonConverterUtil.convertJsonToDataProperties(processDataPropertiesNode, subProcess);
	            subProcess.setDataObjects(dataObjects);
	            subProcess.getFlowElements().addAll(dataObjects);
	        }
	        
	        //store correct convertion info...
	        if (STENCIL_COLLAPSED_SUB_CHOREOGRAPHY.equals(BpmnJsonConverterUtil.getStencilId(elementNode))) {
	            GraphicInfo graphicInfo = model.getGraphicInfo(BpmnJsonConverterUtil.getElementId(elementNode));
	            graphicInfo.setExpanded(false); //default is null!
	        }

	        return subProcess;
	}

}

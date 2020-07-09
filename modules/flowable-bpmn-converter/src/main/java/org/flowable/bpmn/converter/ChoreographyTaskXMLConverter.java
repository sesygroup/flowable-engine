package org.flowable.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.flowable.bpmn.converter.util.BpmnXMLUtil;
import org.flowable.bpmn.exceptions.XMLException;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ChoreographyTask;
import org.flowable.bpmn.model.Message;
import org.flowable.bpmn.model.MessageFlow;
import org.flowable.bpmn.model.Participant;

public class ChoreographyTaskXMLConverter extends BaseBpmnXMLConverter {

	@Override
	protected Class<? extends BaseElement> getBpmnElementType() {
		return ChoreographyTask.class;
	}

	@Override
	protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
		ChoreographyTask choreographyTask = new ChoreographyTask();
		BpmnXMLUtil.addXMLLocation(choreographyTask, xtr);
		String initiatingId = (xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_PARTECIPANT));
		while (xtr.hasNext()) {
            try {
            	xtr.next();
            } catch (Exception e) {
                LOGGER.debug("Error reading XML document", e);
                throw new XMLException("Error reading XML", e);
            }
            if (xtr.isEndElement() && xtr.getLocalName() == "choreographyTask") {
            	break;
            }
            
            if (!xtr.isStartElement()) {
                continue;
            }
            if(ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT.equals(xtr.getLocalName())) {
            	String participantImport = xtr.getElementText();
            	if(participantImport != null) {
            		for (Participant participant : model.getParticipants()) {
            			if(participantImport.equals(participant.getId())) {
            				if(initiatingId != null && initiatingId.equals(participant.getId())) {
            					choreographyTask.setInitiatingPartecipant(participant.getName());
            				} else {
            					choreographyTask.setPartecipant(participant.getName());
            				}
            			}
					}
            	}
            }
            if(ELEMENT_MESSAGEFLOW_REF.equals(xtr.getLocalName())) {
            	String messageFlowImport = xtr.getElementText();
            	if(model.getMessageFlows() != null && !model.getMessageFlows().isEmpty()) {
            		for (MessageFlow messageFlow : model.getMessageFlows().values()) {
    					if(messageFlow != null && messageFlow.getId().equals(messageFlowImport)) {
    						Message messageImport = model.getMessage(messageFlow.getMessageRef());
    						if(messageImport != null) {
    							if(initiatingId != null && initiatingId.equals(messageFlow.getSourceRef())) {
        							choreographyTask.setInitiatingMessage(messageImport.getName());
        						} else {
        							choreographyTask.setReturnMessage(messageImport.getName());
        						}
    						}
    					}
    				}
            	}
            }
		}
		
//		choreographyTask.setPartecipant(xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT));
//		choreographyTask.setInitiatingMessage(xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_MESSAGE));
//		choreographyTask.setReturnMessage(xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_RETURN_MESSAGE));
		
		return choreographyTask;
	}

	 @Override
	 protected String getXMLElementName() {
		 return ELEMENT_TASK_CHOREOGRAPHY;
	 }

	@Override
	protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw)
			throws Exception {
		ChoreographyTask choreographyTask = (ChoreographyTask) element;
		writeDefaultAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_PARTECIPANT,choreographyTask.getInitiatingPartecipant() ,xtw);
		if(choreographyTask.getInitiatingPartecipant() != null) {
			xtw.writeStartElement(ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT);
			xtw.writeCharacters(choreographyTask.getInitiatingPartecipant().replace(" ", "_"));
			xtw.writeEndElement();
		}
		if(choreographyTask.getPartecipant() != null) {
			xtw.writeStartElement(ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT);
			xtw.writeCharacters(choreographyTask.getPartecipant().replace(" ", "_"));
			xtw.writeEndElement();
		}
		if(choreographyTask.getInitiatingMessage() != null) {
			xtw.writeStartElement(ELEMENT_MESSAGEFLOW_REF);
			xtw.writeCharacters("initMessageFlow" + choreographyTask.getId());
			xtw.writeEndElement();
		}
		if(choreographyTask.getReturnMessage() != null) {
			xtw.writeStartElement(ELEMENT_MESSAGEFLOW_REF);
			xtw.writeCharacters("retMessageFlow" + choreographyTask.getId());
			xtw.writeEndElement();
		}
//		writeQualifiedAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT,choreographyTask.getPartecipant() ,xtw);
//		writeQualifiedAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_MESSAGE,choreographyTask.getInitiatingMessage() ,xtw);
//		writeQualifiedAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_RETURN_MESSAGE,choreographyTask.getReturnMessage() ,xtw);
		
	}

	@Override
	protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw)
			throws Exception {
	}

}

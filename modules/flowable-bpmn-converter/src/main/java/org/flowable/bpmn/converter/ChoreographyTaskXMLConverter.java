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
		if(initiatingId != null) {
			initiatingId = initiatingId.replace("_0:", "");
		}
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
            		participantImport = participantImport.replace("_0:", "");
            		for (Participant participant : model.getParticipants()) {
            			if(participantImport.equals(participant.getId())) {
            				if(initiatingId != null && initiatingId.equals(participant.getId())) {
            					choreographyTask.setInitiatingPartecipant(participant.getName());
            					choreographyTask.setInitiatingPartecipantId(participant.getId());
            				} else {
            					choreographyTask.setPartecipant(participant.getName());
            					choreographyTask.setPartecipantId(participant.getId());
            				}
            			}
					}
            	}
            }
            if(ELEMENT_MESSAGEFLOW_REF.equals(xtr.getLocalName())) {
            	String messageFlowImport = xtr.getElementText();
            	if(messageFlowImport != null) {
            		messageFlowImport = messageFlowImport.replace("_0:", "");
            	}
            	if(model.getMessageFlows() != null && !model.getMessageFlows().isEmpty()) {
            		for (MessageFlow messageFlow : model.getMessageFlows().values()) {
    					if(messageFlow != null && messageFlow.getId().equals(messageFlowImport)) {
    						Message messageImport = model.getMessage(messageFlow.getMessageRef());
    						if(messageImport != null) {
    							if(initiatingId != null && initiatingId.equals(messageFlow.getSourceRef())) {
        							choreographyTask.setInitiatingMessage(messageImport.getName());
        							choreographyTask.setInitiatingMessageId(messageImport.getId());
        							choreographyTask.setInitiatingMessageFlowRef(messageFlow.getId());
        						} else {
        							choreographyTask.setReturnMessage(messageImport.getName());
        							choreographyTask.setReturnMessageId(messageImport.getId());
        							choreographyTask.setReturnMessageFlowRef(messageFlow.getId());
        						}
    						}
    					}
    				}
            	}
            }
		}
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
		if(choreographyTask.getInitiatingPartecipantId() != null) {
			writeDefaultAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_PARTECIPANT,choreographyTask.getInitiatingPartecipantId() ,xtw);
		} else {
			writeDefaultAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_PARTECIPANT,choreographyTask.getInitiatingPartecipant().replace(" ", "_") ,xtw);
		}
//		writeDefaultAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_PARTECIPANT,choreographyTask.getInitiatingPartecipant().replace(" ", "_") ,xtw);
		if(choreographyTask.getInitiatingPartecipant() != null) {
			xtw.writeStartElement(ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT);
			if(choreographyTask.getInitiatingPartecipantId() != null) {
				xtw.writeCharacters(choreographyTask.getInitiatingPartecipantId());
			} else {
				xtw.writeCharacters(choreographyTask.getInitiatingPartecipant().replace(" ", "_"));
			}
//			xtw.writeCharacters(choreographyTask.getInitiatingPartecipant().replace(" ", "_"));
			xtw.writeEndElement();
		}
		if(choreographyTask.getPartecipant() != null) {
			xtw.writeStartElement(ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT);
			if(choreographyTask.getPartecipantId() != null) {
				xtw.writeCharacters(choreographyTask.getPartecipantId());
			} else {
				xtw.writeCharacters(choreographyTask.getPartecipant().replace(" ", "_"));
			}
//			xtw.writeCharacters(choreographyTask.getPartecipant().replace(" ", "_"));
			xtw.writeEndElement();
		}
		if(choreographyTask.getInitiatingMessage() != null) {
			xtw.writeStartElement(ELEMENT_MESSAGEFLOW_REF);
			if(choreographyTask.getInitiatingMessageFlowRef() != null) {
				xtw.writeCharacters(choreographyTask.getInitiatingMessageFlowRef());
			} else {
				xtw.writeCharacters("initMessageFlow" + choreographyTask.getId());
			}
			xtw.writeEndElement();
		}
		if(choreographyTask.getReturnMessage() != null) {
			xtw.writeStartElement(ELEMENT_MESSAGEFLOW_REF);
			if(choreographyTask.getReturnMessageFlowRef() != null ) {
				xtw.writeCharacters(choreographyTask.getReturnMessageFlowRef());
			} else {
				xtw.writeCharacters("retMessageFlow" + choreographyTask.getId());
			}
			xtw.writeEndElement();
		}
	}

	@Override
	protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw)
			throws Exception {
	}

}

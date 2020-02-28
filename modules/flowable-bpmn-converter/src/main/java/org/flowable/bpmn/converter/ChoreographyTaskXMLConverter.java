package org.flowable.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.flowable.bpmn.converter.util.BpmnXMLUtil;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ChoreographyTask;

public class ChoreographyTaskXMLConverter extends BaseBpmnXMLConverter {

	@Override
	protected Class<? extends BaseElement> getBpmnElementType() {
		return ChoreographyTask.class;
	}

	@Override
	protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
		ChoreographyTask choreographyTask = new ChoreographyTask();
		BpmnXMLUtil.addXMLLocation(choreographyTask, xtr);
		choreographyTask.setInitiatingPartecipant(xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_PARTECIPANT));
		choreographyTask.setPartecipant(xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT));
		choreographyTask.setInitiatingMessage(xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_MESSAGE));
		choreographyTask.setReturnMessage(xtr.getAttributeValue(null, ATTRIBUTE_TASK_CHOREOGRAPHY_RETURN_MESSAGE));
		
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
		writeQualifiedAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_PARTECIPANT,choreographyTask.getInitiatingPartecipant() ,xtw);
		writeQualifiedAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_PARTECIPANT,choreographyTask.getPartecipant() ,xtw);
		writeQualifiedAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_INIT_MESSAGE,choreographyTask.getInitiatingMessage() ,xtw);
		writeQualifiedAttribute(ATTRIBUTE_TASK_CHOREOGRAPHY_RETURN_MESSAGE,choreographyTask.getReturnMessage() ,xtw);
		
	}

	@Override
	protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw)
			throws Exception {
	}

}

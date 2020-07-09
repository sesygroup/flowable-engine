package org.flowable.validation.validator.impl;

import java.util.List;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ChoreographyTask;
import org.flowable.bpmn.model.EventGateway;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.InclusiveGateway;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.validation.ValidationError;
import org.flowable.validation.validator.Problems;
import org.flowable.validation.validator.ProcessLevelValidator;

public class ChoreographyTaskValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		//check initiating partecipant, regarding BPMN2.0.2 documentation
		List<ChoreographyTask> choreographyTasks = process.findFlowElementsOfType(ChoreographyTask.class);
		for (ChoreographyTask choreographyTask : choreographyTasks) {
			checkChoreographyTask(choreographyTask, bpmnModel, process, errors);
		}
	}

	private void checkChoreographyTask(ChoreographyTask choreographyTask, BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		for (SequenceFlow sequenceFlow : choreographyTask.getIncomingFlows()) {
			checkInitiatingPartecipant(sequenceFlow, choreographyTask, bpmnModel, process, errors);
		}
	}

	private void checkInitiatingPartecipant(SequenceFlow sequenceFlow, ChoreographyTask choreographyTask,
			BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		String sourceRef = sequenceFlow.getSourceRef();
		FlowElement source = process.getFlowElement(sourceRef, true);
		if(source != null && source instanceof ChoreographyTask) {
			ChoreographyTask preChoreographyTask = (ChoreographyTask) source;
			if(choreographyTask.getInitiatingPartecipant() != null) {
				//check: the initiating partecipant must be part of previous choreography task
				if((!choreographyTask.getInitiatingPartecipant().equals(preChoreographyTask.getInitiatingPartecipant())
						&& !choreographyTask.getInitiatingPartecipant().equals(preChoreographyTask.getPartecipant()))){
					addError(errors, Problems.INVALID_INIT_PART, process, choreographyTask, "Invalid initiating participant, must be part of previous choreography task");
				}
			} 
		} else  if(source != null && (source instanceof ExclusiveGateway || source instanceof ParallelGateway 
				|| source instanceof InclusiveGateway || source instanceof EventGateway)){
			FlowNode node = (FlowNode) source;
			// if we find a gatweway we have to check all the incoming flow and find a choreography task if exists
			for (SequenceFlow sequenceFlowRec : node.getIncomingFlows()) {
				checkInitiatingPartecipant(sequenceFlowRec, choreographyTask, bpmnModel, process, errors);
			}
		}
	}
}

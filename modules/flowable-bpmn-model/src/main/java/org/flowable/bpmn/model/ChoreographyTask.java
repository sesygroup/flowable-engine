package org.flowable.bpmn.model;

public class ChoreographyTask extends Task{

	protected String initiatingPartecipant;
	protected String partecipant;
	protected String initiatingMessage;
	protected String returnMessage;
	protected String initiatingPartecipantId;
	protected String partecipantId;
	protected String initiatingMessageId;
	protected String returnMessageId;
	protected String initiatingMessageFlowRef;
	protected String returnMessageFlowRef;

	public String getInitiatingMessageFlowRef() {
		return initiatingMessageFlowRef;
	}

	public void setInitiatingMessageFlowRef(String initiatingMessageFlowRef) {
		this.initiatingMessageFlowRef = initiatingMessageFlowRef;
	}

	public String getReturnMessageFlowRef() {
		return returnMessageFlowRef;
	}

	public void setReturnMessageFlowRef(String returnMessageFlowRef) {
		this.returnMessageFlowRef = returnMessageFlowRef;
	}

	public String getInitiatingPartecipantId() {
		return initiatingPartecipantId;
	}

	public void setInitiatingPartecipantId(String initiatingPartecipantId) {
		this.initiatingPartecipantId = initiatingPartecipantId;
	}

	public String getPartecipantId() {
		return partecipantId;
	}

	public void setPartecipantId(String partecipantId) {
		this.partecipantId = partecipantId;
	}

	public String getInitiatingMessageId() {
		return initiatingMessageId;
	}

	public void setInitiatingMessageId(String initiatingMessageId) {
		this.initiatingMessageId = initiatingMessageId;
	}

	public String getReturnMessageId() {
		return returnMessageId;
	}

	public void setReturnMessageId(String returnMessageId) {
		this.returnMessageId = returnMessageId;
	}

	public String getInitiatingPartecipant() {
		return initiatingPartecipant;
	}

	public void setInitiatingPartecipant(String initiatingPartecipant) {
		this.initiatingPartecipant = initiatingPartecipant;
	}

	public String getPartecipant() {
		return partecipant;
	}

	public void setPartecipant(String partecipant) {
		this.partecipant = partecipant;
	}

	public String getInitiatingMessage() {
		return initiatingMessage;
	}

	public void setInitiatingMessage(String initiatingMessage) {
		this.initiatingMessage = initiatingMessage;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}

	@Override
	public FlowElement clone() {
		ChoreographyTask clone = new ChoreographyTask();
        clone.setValues(this);
        return clone;
	}
	
	public void setValues(ChoreographyTask otherElement) {
        super.setValues(otherElement);
        setInitiatingPartecipant(otherElement.getInitiatingPartecipant());
        setPartecipant(otherElement.getPartecipant());
        setInitiatingMessage(otherElement.getInitiatingMessage());
        setReturnMessage(otherElement.getReturnMessage());
    }

}

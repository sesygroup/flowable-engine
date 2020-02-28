package org.flowable.bpmn.model;

public class ChoreographyTask extends Task{

	protected String initiatingPartecipant;
	protected String partecipant;
	protected String initiatingMessage;
	protected String returnMessage;
	
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

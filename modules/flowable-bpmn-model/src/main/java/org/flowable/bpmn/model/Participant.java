package org.flowable.bpmn.model;

public class Participant extends BaseElement{

	protected String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public BaseElement clone() {
		Participant clone = new Participant();
        clone.setValues(this);
        return clone;
	}
	
	public void setValues(Participant otherElement) {
        super.setValues(otherElement);
        setName(otherElement.getName());
    }

}

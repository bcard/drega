package org.bcard.command;

public class CreateSignal implements ICommand {
	
	private final long initialValue;
	
	private final String id;
	
	public CreateSignal(String identifier, long initialValue) {
		this.id = identifier;
		this.initialValue = initialValue;
	}

	@Override
	public void execute() {
		
	}

	public long getInitialValue(){
		return initialValue;
	}
	
	public String getId() {
		return id;
	}
}

package com.IntraConnect.dispatcher.dbListner;

public class TableChangeEvent {
	
	private final String table;
	private final String operation;
	private final long id;
	
	public TableChangeEvent(String table, String operation, long id) {
		this.table = table;
		this.operation = operation;
		this.id = id;
	}
	
	public String getTable() { return table; }
	public String getOperation() { return operation; }
	public long getId() { return id; }
}

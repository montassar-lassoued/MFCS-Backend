package com.IntraConnect.dispatcher.dbListner;

public interface TableChangeListener {
	void onAfterCommit(TableChangeEvent event);
}
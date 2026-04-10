package com.IntraConnect.messageEngine;

import com.IntraConnect.controller.Connectable;

public interface ConnectionEngine {
	
	/** Connect zur Schnittstelle. */
	void doConnect(Connectable target) throws Exception;
	
	/** Disconnect Schnittstelle.*/
	void doDisconnect(Connectable target);

	/** Sendet die Nachricht. */
	boolean sendMessage(Connectable connectable, byte[] content);
	
	/** Prüft, ob diese Engine für den Controller (Protokoll + Modus) zuständig ist */
	boolean supports(Connectable connectable);
	
}

package com.IntraConnect.messageEngine;

import com.IntraConnect.controller.Controller;

public interface MessageEngine {
	/** Sendet die Nachricht. */
	boolean sendMessage(Controller controller, byte[] content);
	
	/** Prüft, ob diese Engine für den Controller (Protokoll + Modus) zuständig ist */
	boolean supports(Controller controller);
	
}

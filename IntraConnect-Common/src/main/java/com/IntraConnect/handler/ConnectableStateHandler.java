package com.IntraConnect.handler;

import com.IntraConnect.handler.records.ConnectableIntfState;
import com.IntraConnect.intf.Handler;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class ConnectableStateHandler implements Handler<ConnectableIntfState> {
	private static final Logger log = LoggerFactory.getLogger(ConnectableStateHandler.class);
	private static Set<Class<? extends ConnectableStateHandler>> subClasses;
	
	@Override
	public void handle(ConnectableIntfState payload) {
		
		// Einmaliges Scannen des Classpaths nach Unterklassen
		if (subClasses == null) {
			Reflections reflections = new Reflections("com.IntraConnect");
			subClasses = reflections.getSubTypesOf(ConnectableStateHandler.class);
		}
		
		for (Class<? extends ConnectableStateHandler> clazz : subClasses) {
			try {
				// Erstellt eine neue Instanz der Unterklasse und führt sie aus
				ConnectableStateHandler instance = clazz.getDeclaredConstructor().newInstance();
				instance.handle(payload);
			} catch (Exception e) {
				// Fehler beim Erstellen der Unterklasse (z.B. kein Standard-Konstruktor)
				log.error(e.getMessage());
			}
		}
	}
}

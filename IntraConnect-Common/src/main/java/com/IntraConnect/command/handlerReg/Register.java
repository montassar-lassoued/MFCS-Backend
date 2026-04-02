package com.IntraConnect.command.handlerReg;

import com.IntraConnect.command.handlerReg.time.NoTimeTrigger;
import com.IntraConnect.command.handlerReg.time.TriggerTime;
import com.IntraConnect.intf.Handler;
import com.IntraConnect.listener.DeleteEventListener;
import com.IntraConnect.listener.EventListener;
import com.IntraConnect.listener.InsertEventListener;
import com.IntraConnect.listener.UpdateEventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Register save Singleton*/
@Component
public class Register {
	
	private final Map<Class<? extends Handler<?>>, HandlerConfig> handlers =
			new ConcurrentHashMap<>();
	private final Map<String, List<EventListener>> listeners = new HashMap<>();
	
	public <T> void registerHandler(Class<? extends Handler<T>> clazz, TriggerTime triggerTime) {
		regHandler(clazz, triggerTime);
	}
	public <T> void registerHandler(Class<? extends Handler<T>> clazz) {
		regHandler(clazz, new NoTimeTrigger());
	}
	private <T> void regHandler(Class<? extends Handler<T>> clazz, TriggerTime trigger){
		if (handlers.containsKey(clazz)) {
			throw new IllegalStateException(
					"Handler bereits registriert: " + clazz.getSimpleName());
		}
		
		try {
			
			Handler<T> instance = clazz.getDeclaredConstructor().newInstance();
			handlers.put(clazz, new HandlerConfig(clazz, trigger));
			
		} catch (Exception e) {
			throw new RuntimeException(
					"Handler konnte nicht instanziiert werden: " + clazz.getSimpleName(), e);
		}
	}
	
	/**
	 * Listener register
	 * @param table: Database Table name
	 * @param listener: Listener class */
	public <T> void addListener(String table, EventListener listener) {
		if (listeners.containsKey(table)) {
			List<EventListener> tableListeners = listeners.get(table);
			if (tableListeners.contains(listener)) {
				throw new IllegalStateException("listener bereits registriert: " + table);
			}
			tableListeners.add(listener);
			listeners.put(table, tableListeners);
		}
		else {
			List<EventListener> tableListeners =new ArrayList<>();
			tableListeners.add(listener);
			listeners.put(table, tableListeners);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> Handler<T> getHandler(Class<? extends Handler<?>>clazz) {
		
		HandlerConfig config = handlers.get(clazz);
		
		if (config == null) {
			throw new IllegalArgumentException(
					"Handler nicht registriert: " + clazz.getSimpleName());
		}
		
		try {
			return (Handler<T>)
					config.handler().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Handler Instanziierung fehlgeschlagen", e);
		}
	}
	
	public Map<Class<? extends Handler<?>>, HandlerConfig> getHandlers() {
		return Map.copyOf(handlers);
	}
	
	public List<EventListener> getListeners(String name) {
		List<EventListener> listener = listeners.get(name);
		if (listener == null) {
			throw new IllegalArgumentException("listener nicht registriert: " + name);
		}
		return listener;
	}
	
}

package com.IntraConnect.dispatcher.dbListner;

import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.listener.DeleteEventListener;
import com.IntraConnect.listener.EventListener;
import com.IntraConnect.listener.InsertEventListener;
import com.IntraConnect.listener.UpdateEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TableEventRegistry {
	@Autowired
	private Register register;
	private final Map<String, List<TableChangeListener>> listeners = new HashMap<>();
	
	public void fire(TableChangeEvent event) {
		List<EventListener> tableListeners = register.getListeners(event.getTable());
		
		if (tableListeners != null) {
			for (EventListener l : tableListeners) {
				if(l instanceof InsertEventListener){
					((InsertEventListener) l).afterInsert(event.getId());
				}
				else if (l instanceof UpdateEventListener){
					((UpdateEventListener) l).afterUpdate(event.getId());
				}
				else if (l instanceof DeleteEventListener){
					((DeleteEventListener) l).afterDelete(event.getId());
				}
			}
		}
	}
}

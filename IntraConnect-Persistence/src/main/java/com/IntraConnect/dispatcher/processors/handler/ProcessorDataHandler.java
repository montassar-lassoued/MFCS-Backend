package com.IntraConnect.dispatcher.processors.handler;

import com.IntraConnect.intf.Handler;
import com.IntraConnect.intf.Processor;
import com.IntraConnect.processors.IntraConnectProcessors;
import com.IntraConnect.processors.ProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessorDataHandler implements Handler<ProcessorData> {
	
	@Autowired
	private ProcessorFactory processorFactory;
	
	@Override
	public void handle(ProcessorData payload) {
		if(payload != null && !payload.ControllerName().isBlank()){
			Processor processor =
					processorFactory.getProcessor(payload.ControllerName());
			
			processor.process(payload.Data());
		}
	}
}

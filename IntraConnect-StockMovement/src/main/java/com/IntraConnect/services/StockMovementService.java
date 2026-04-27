package com.IntraConnect.services;


import com.IntraConnect.handler.*;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.intf.IntraConnectApplicationServices;
import com.IntraConnect.storageConfig.PathConfig;
import com.IntraConnect.storageConfig.StorageSystemConfig;
import org.jdom2.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class StockMovementService extends IntraConnectApplicationServices {
	
	StorageSystemConfig storageSystemConfig;
	
	PathConfig pathConfig;
	boolean enabled;
	
	public StockMovementService(Register register){
		super(register);
		
	}
	@Override
	public String getName() {
		return "StockMovement";
	}
	
	@Override
	public void configuration(Element module) {
		
		if(module != null) {
			enabled = Boolean.parseBoolean(module.getAttributeValue("enabled"));
			if (!enabled){
				return;
			}
			
			Element elNodes = module.getChild("Nodes");
			pathConfig = new PathConfig();
			pathConfig.load(elNodes);
			
			
			
			/** Storage Systeme**/
			Element elStorage = module.getChild("StorageSystem");
			storageSystemConfig = new StorageSystemConfig();
			storageSystemConfig.load(elStorage);
		}
		// Node: entrypoint -edges: SR01*
//
 		//NextStep step = LoadUnitPath.calculateNextStep(Graph.get(), "A", "K");
//
 //		System.out.println(step.getController());  // SPS1 / SPS2 / ...
//		System.out.println(step.getDirection());   // Links / Rechts / Gerade
//		System.out.println(step.getNextTarget());    // nächstes Ziel
	}
	
	@Override
	public void validate() {
		pathConfig.validate();
		storageSystemConfig.validate();
	}
	
	@Override
	public void register() {
		//Handlers
		register.registerHandler(LoadUnitCreateHandler.class);
		register.registerHandler(LoadUnitDeleteHandler.class);
		register.registerHandler(LoadUnitSetLocationHandler.class);
		register.registerHandler(LoadUnitSetDestinationHandler.class);
		register.registerHandler(LoadUnitSetLocationDestinationHandler.class);
		// DB-Listeners
		// register.addListener("LoadUnit", new LoadUnitCreateListener());
		//Konfiguration
		// Path: A->B->C;B->D->F
		pathConfig.register();
		storageSystemConfig.register();
	}
	
	@Override
	public void run() {
	
	}
	
	@Override
	public void stop() {
	
	}
}

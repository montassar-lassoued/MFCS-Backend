package com.IntraConnect.services;

import com.IntraConnect.command.handlerReg.time.NoTimeTrigger;
import com.IntraConnect.command.trigger.Trigger;
import com.IntraConnect.handler.*;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.command.handlerReg.time.FixedRateTrigger;
import com.IntraConnect.intf.PilotApplicationServices;
import com.IntraConnect.listener.InsertEventListener;
import com.IntraConnect.listner.LoadUnitCreateListener;
import com.IntraConnect.nodes.*;
import com.IntraConnect.record.LoadUnitCreate;
import com.IntraConnect.utils.LoadUnit;
import org.jdom2.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StockMovementService extends PilotApplicationServices {
	boolean enabled;
	
	public StockMovementService(Register register){
		super(register);
		
	}
	@Override
	public String getName() {
		return "StockMovement";
	}
	
	@Override
	public void configuration(Element module, ApplicationContext context) {
		
		Map<String, Node> graph = new HashMap<>();
		if(module != null) {
			enabled = Boolean.parseBoolean(module.getAttributeValue("enabled"));
			if (!enabled){
				return;
			}
			
			Element elNodes = module.getChild("Nodes");
			List<Element> nodes = elNodes.getChildren("Node");
			for (Element point : nodes) {
				
				//***** Attribute
				String name = point.getAttributeValue("point");
				String controller = point.getAttributeValue("controller");
				
				Node node = new Node(name, controller);
				
				//******* Ziele
				List<Edge> edges = new ArrayList<>();
				List<Element> targets = point.getChildren("Ziel");
				for (Element target: targets){
					
					String tName = target.getAttributeValue("point");
					String tDirection = target.getAttributeValue("direction");
					int tCost = Integer.parseInt(target.getAttributeValue("cost", "100"));
					
					edges.add(new Edge(tName,tDirection, tCost));
				}
				node.setEdges(edges);
				
				graph.put(node.getPoint(), node);
			}
		}
		
		Graph.register(graph);
//
//		NextStep step = LoadUnitPath.calculateNextStep(Graph.get(), "A", "Z");
//
//		System.out.println(step.getController());  // SPS1 / SPS2 / ...
//		System.out.println(step.getDirection());   // Links / Rechts / Gerade
//		System.out.println(step.getNextTarget());    // nächstes Ziel
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
	}
	
	@Override
	public void validate() {
	
	}
	
	@Override
	public void run() {
	
	}
	
	@Override
	public void stop() {
	
	}
}

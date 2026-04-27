package com.IntraConnect.dataService;

import com.IntraConnect.visualization.VisuConverterService;
import com.IntraConnect.visualization.VisuData;
import com.IntraConnect.xml.SystemConfig;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisualizationService {
	
	private static final Logger log = LoggerFactory.getLogger(VisualizationService.class);
	private final Element systemConfig;
	private Map<String, Element> visual = new LinkedHashMap<>();
	
	public VisualizationService(Element systemConfig) {
		this.systemConfig = systemConfig;
		
		loadVisualization();
	}
	
	private void loadVisualization() {
		try {
		// 1. Zugriff auf <Modules>
		Element modules = systemConfig.getChild("Modules");
		
		// 2. Das richtige <Module> finden (name="UI")
		List<Element> moduleList = modules.getChildren("Module");
		Element uiModule = null;
		
		for (Element mod : moduleList) {
			if ("UI".equals(mod.getAttributeValue("name"))) {
				uiModule = mod;
				break;
			}
		}
		
		if (uiModule != null) {
			// 3. Direkt auf <Visualization> zugreifen
			Element visualization = uiModule.getChild("Visualization");
			List<Element> elVisuals = visualization.getChildren("Visu");
			
			for (Element elVisu : elVisuals) {
				String id = elVisu.getAttributeValue("id");
				if(id.isBlank()){
					throw new RuntimeException("Visualization has no name");
				}
				visual.put(id, elVisu);
			}
		}
		
	} catch (Exception e) {
		log.error(e.getMessage());
	}
	}
	
	public VisuData getVisuElement(String id) {
		if (!id.isBlank()){
			Element elVisu = visual.get(id);
			if(elVisu != null){
				XMLOutputter outputter = new XMLOutputter();
				String visualString = outputter.outputString(elVisu);
				
				try {
					return new VisuConverterService().convertXmlToJson(visualString);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return new VisuData();
	}
}

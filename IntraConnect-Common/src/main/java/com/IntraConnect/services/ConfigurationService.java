package com.IntraConnect.services;

import org.jdom2.Element;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationService {
	
		private final Element systemConfig;
		
		public ConfigurationService(Element systemConfig) {
			this.systemConfig = systemConfig;
		}
}

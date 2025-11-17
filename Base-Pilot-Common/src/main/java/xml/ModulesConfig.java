package xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

public class ModulesConfig {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Module")
    private List<ModuleConfig> modules;

    public List<ModuleConfig> getModules() { return modules; }
    public void setModules(List<ModuleConfig> modules) { this.modules = modules; }
}


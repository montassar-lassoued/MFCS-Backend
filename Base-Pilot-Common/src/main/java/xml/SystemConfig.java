package xml;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

public class SystemConfig {

    @JacksonXmlProperty(localName = "Modules")
    private ModulesConfig modules;

    /*@JacksonXmlProperty(localName = "Logging")
    private LoggingConfig logging;*/

    public ModulesConfig getModules() { return modules; }
    public void setModules(ModulesConfig modules) { this.modules = modules; }

   /* public LoggingConfig getLogging() { return logging; }
    public void setLogging(LoggingConfig logging) { this.logging = logging; }*/
}

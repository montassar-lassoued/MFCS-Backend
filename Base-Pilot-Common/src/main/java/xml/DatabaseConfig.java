package xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class DatabaseConfig {
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;

    @JacksonXmlProperty(localName = "Host")
    private String host;

    @JacksonXmlProperty(localName = "Port")
    private int port;

    @JacksonXmlProperty(localName = "Username")
    private String username;

    @JacksonXmlProperty(localName = "Password")
    private String password;

    // Getter & Setter
    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}

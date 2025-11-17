package xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ConnectionConfig {

    @JacksonXmlProperty(localName = "Host")
    private String host;

    @JacksonXmlProperty(localName = "Port")
    private int port;

    @JacksonXmlProperty(localName = "Timeout")
    private int timeout;

    // Getter & Setter

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

package gov.ic.geoint.bulleit.config;


import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "proxyConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProxyConfig implements Serializable {

    @XmlElementWrapper(name = "destinations", required = true)
    @XmlElement(name = "destination")
    private List<Destination> destinations;
    @XmlElement(name = "maxThreads")
    private String maxThreads;
    @XmlElement(name = "maxConnections")
    private String maxConnections;
    @XmlElement(name = "idleTimeout")
    private String idleTimeout;
    @XmlElement(name = "timeout")
    private String timeout;
    @XmlElement(name = "requestBufferSize")
    private String requestBufferSize;
    @XmlElement(name = "responseBufferSize")
    private String responseBufferSize;
    @XmlElement(name = "hostURL")
    private String hostURL;
    @XmlElement(name = "hostPort")
    private String hostPort;
    @XmlElement(name = "secureHostPort")
    private String secureHostPort;

    

    public ProxyConfig() {
    }

    /**
     * 
     * @return String hostURL
     */
    public String getHostURL(){
        return hostURL;
    }    
    
    /**
     * 
     * @param hostURL 
     */
    public void setHostURL(String hostURL){
        this.hostURL = hostURL;
    }
    
    /**
     * 
     * @return String secureHostPort
     */
    public String getSecureHostPort() {
        return secureHostPort;
    }

    /**
     * 
     * @param secureHostPort 
     */
    public void setSecureHostPort(String secureHostPort) {
        this.secureHostPort = secureHostPort;
    }

    /**
     *
     * @return String host port
     */
    public String getHostPort() {
        return hostPort;
    }

    /**
     *
     * @param hostPort
     */
    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    /**
     *
     * @return List<Destination> destination URLs to be proxied
     */
    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     *
     * @param destinations URLs to be proxied
     */
    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    /**
     *
     * @return String maxThreads
     */
    public String getMaxThreads() {
        return maxThreads;
    }

    /**
     *
     * @param maxThreads
     */
    public void setMaxThreads(String maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     *
     * @return String maxConnections
     */
    public String getMaxConnections() {
        return maxConnections;
    }

    /**
     *
     * @param maxConnections
     */
    public void setMaxConnections(String maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     *
     * @return String idleTimeout
     */
    public String getIdleTimeout() {
        return idleTimeout;
    }

    /**
     *
     * @param idleTimeout
     */
    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     *
     * @return timeout
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     *
     * @param timeout
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     *
     * @return String requestBufferSize
     */
    public String getRequestBufferSize() {
        return requestBufferSize;
    }

    /**
     *
     * @param requestBufferSize
     */
    public void setRequestBufferSize(String requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
    }

    /**
     *
     * @return String responseBufferSize
     */
    public String getResponseBufferSize() {
        return responseBufferSize;
    }

    /**
     *
     * @param responseBufferSize
     */
    public void setResponseBufferSize(String responseBufferSize) {
        this.responseBufferSize = responseBufferSize;
    }


}

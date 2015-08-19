package gov.ic.geoint.bulleit.config;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "destination")
@XmlAccessorType(XmlAccessType.FIELD)
public class Destination implements Serializable {

    @XmlElement(name = "name", required = true)
    private String name;
    @XmlElement(name = "destinationUrl", required = true)
    private String destinationUrl;
    @XmlElement(name = "destinationPort", required = true)
    private String destinationPort;
    @XmlElement(name = "prefix", required = true)
    private String prefix;
    @XmlElement(name = "asyncSupport", required = true)
    private String asyncSupport;
    @XmlElement(name = "scheme", required = true)
    private String scheme;

    public Destination() {
    }

    /**
     * Removes the specified mapping directory from the URI.
     *
     * @param uri
     * @return
     */
    public String process(String uri) {
        return uri.substring(prefix.length() - 1);
    }

    /**
     * Reverses the process() method. Adds the prefix specified to the start of
     * the incoming URI.
     *
     *
     * @param uri
     * @return String
     */
    public String revert(String uri) {
        if (uri.startsWith(prefix)) {
            return uri;
        } else if (uri.startsWith("/")) {
            return prefix + uri.substring(1);
        } else {
            return uri;
        }
    }

    public boolean matches(String uri) {
        
        return (uri.startsWith(prefix.substring(0, prefix.length() - 1)));
    }

    /**
     *
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name identifier for the prefix / destination pairing
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return String destinationUrl
     */
    public String getDestinationUrl() {
        return destinationUrl;
    }

    /**
     *
     * @param destinationUrl that the proxy will reroute requests to
     */
    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

    /**
     *
     * @return String prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     *
     * @param prefix specify relative path prefix for the proxy to match
     */
    public void setPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException(
                    "the destination prefix cannot be null");
        } else {
            if (!prefix.startsWith("/")) {
                prefix = "/" + prefix;
            }
            if (!prefix.endsWith("/")) {
                prefix += "/";
            }
            this.prefix = prefix;
        }
    }

    /**
     *
     * @return AsyncSupport specify asynchronous support
     */
    public String getAsyncSupport() {
        return asyncSupport;
    }

    /**
     *
     * @param asyncSupport specify asynchronous support
     */
    public void setAsyncSupport(String asyncSupport) {
        this.asyncSupport = asyncSupport;
    }

    /**
     *
     * @return ProxyScheme scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     *
     * @param scheme specify http or https protocol scheme
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * @return the destinationPort
     */
    public String getDestinationPort() {
        return destinationPort;
    }

    /**
     * @param destinationPort the destinationPort to set
     */
    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

}

package gov.ic.geoint.bulleit.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * reads in the proxy config and keeps proxy domain information
 *
 */
public class Domains {

    private static final String DEFAULT_CONFIG = "/proxyConfig.xml";
    private List<Destination> destinations = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(Domains.class.getName());
    private static Domains domainsInstance = null;
    private ProxyConfig proxyConfig = null;

    /**
     *
     */
    private Domains() {
        this.readConfig();
    }

    public static Domains newInstance() {
        if (domainsInstance == null) {
            return domainsInstance = new Domains();
        } else {
            return domainsInstance;
        }
    }

    /**
     *
     */
    private void readConfig() {
        //read in & unmarshal config file 
        URL configURL = null;
        try {

            JAXBContext jbc = JAXBContext.newInstance(ProxyConfig.class);
            Unmarshaller unmarshaller = jbc.createUnmarshaller();

            //attempt to load config file from classpath, if necessary
            configURL = Domains.class.getResource(DEFAULT_CONFIG);
            InputStream is = configURL.openStream();
            proxyConfig = (ProxyConfig) unmarshaller.unmarshal(is);
            this.destinations = proxyConfig.getDestinations();
            is.close();

        } catch (JAXBException | IOException e) {
            logger.log(Level.WARNING, "Unable to read the configuration "
                    + "file. The Proxy will shut down. {0}", e);
            System.exit(0);
        }
    }

    /**
     *
     * @param location
     * @return Destination
     */
    public Destination selectMappedDestination(String location) {
        for (Destination d : this.destinations) {
            if (location.startsWith(d.getDestinationUrl())) {
                return d;
            }
        }
        return null;  //@todo should the proxy fail gracefully here? or send a default page stating that the target web resource isn't available?? 
    }

    /**
     *
     * @param remoteAddress
     * @return Destination
     */
    public Destination matchRemoteToTarget(InetAddress remoteAddress) {

        for (Destination d : this.destinations) {
            if (remoteAddress.getHostName().contains(d.getDestinationUrl())) {
                return d;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     *
     * @param destination
     */
    public void addDestination(Destination destination) {
        if (!destinations.contains(destination)) {
            destinations.add(destination);
        }
    }

    /**
     *
     * @param destinations
     */
    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    /**
     *
     * @return
     */
    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }
}

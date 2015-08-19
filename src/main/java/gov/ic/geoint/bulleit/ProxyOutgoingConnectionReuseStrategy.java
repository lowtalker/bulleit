package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.NHttpConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
//import org.apache.http.nio.NHttpConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

/**
 *
 */
public class ProxyOutgoingConnectionReuseStrategy extends DefaultConnectionReuseStrategy {

    private static final Logger logger = Logger.getLogger(ProxyOutgoingConnectionReuseStrategy.class.getName());

    @Override
    public boolean keepAlive(final HttpResponse response, final HttpContext context) {
        logger.log(Level.FINE, "ProxyOutgoingConnectionReuseStrategy#keepAlive");
        NHttpConnection conn = (NHttpConnection) context.getAttribute(
                HttpCoreContext.HTTP_CONNECTION);
        boolean keepAlive = super.keepAlive(response, context);
        if (keepAlive) {
            logger.log(Level.INFO, "[proxy->origin] connection kept alive {0}", conn);
        }
        return keepAlive;
    }

}

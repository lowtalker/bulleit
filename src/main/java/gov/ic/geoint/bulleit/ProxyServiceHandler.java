package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.HttpAsyncRequestHandlerMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.ConnectionReuseStrategy;
//import org.apache.http.nio.NHttpServerConnection;
import gov.ic.geoint.bulleit.apache.NHttpServerConnection;
//import org.apache.http.nio.protocol.HttpAsyncRequestHandlerMapper;
import gov.ic.geoint.bulleit.apache.HttpAsyncService;
import org.apache.http.protocol.HttpProcessor;

/**
 *
 */
public class ProxyServiceHandler extends HttpAsyncService {

    private static final Logger logger = Logger.getLogger(ProxyServiceHandler.class.getName());

    public ProxyServiceHandler(
            final HttpProcessor httpProcessor,
            final ConnectionReuseStrategy reuseStrategy,
            final HttpAsyncRequestHandlerMapper handlerResolver) {
        super(httpProcessor, reuseStrategy, null, handlerResolver, null);
    }

    @Override
    protected void log(final Exception ex) {
        logger.log(Level.INFO, "ProxyServiceHandler#log {0}", ex);
    }

    @Override
    public void connected(final NHttpServerConnection conn) {
        logger.log(Level.INFO, "[client->proxy] connection open {0}", conn);
        super.connected(conn);
    }

    @Override
    public void closed(final NHttpServerConnection conn) {
        logger.log(Level.INFO, "[client->proxy] connection closed {0}", conn);
        super.closed(conn);
    }

}

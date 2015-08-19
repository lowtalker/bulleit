package gov.ic.geoint.bulleit;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpException;
//import org.apache.http.nio.NHttpClientConnection;
//import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import gov.ic.geoint.bulleit.apache.HttpAsyncRequestExecutor;
import gov.ic.geoint.bulleit.apache.NHttpClientConnection;

/**
 *
 */
public class ProxyClientProtocolHandler extends HttpAsyncRequestExecutor {

    private static final Logger logger = Logger.getLogger(ProxyClientProtocolHandler.class.getName());

    public ProxyClientProtocolHandler() {
        super();
        logger.log(Level.INFO, "ProxyClientProtocolHandler() initialized");
    }

    @Override
    protected void log(final Exception ex) {
        logger.log(Level.SEVERE, "ProxyClientProtocolHandler failed {0}", ex);
    }

    @Override
    public void connected(final NHttpClientConnection conn,
            final Object attachment) throws IOException, HttpException {            
        super.connected(conn, attachment);
        logger.log(Level.INFO, "[proxy->origin] connection open {0}", conn);
    }

    @Override
    public void closed(final NHttpClientConnection conn) {        
        super.closed(conn);
        logger.log(Level.INFO, "[proxy->origin] connection closed ", conn);
    }

}

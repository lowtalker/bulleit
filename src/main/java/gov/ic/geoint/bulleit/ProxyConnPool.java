package gov.ic.geoint.bulleit;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.config.ConnectionConfig;
import gov.ic.geoint.bulleit.apache.BasicNIOConnPool;
import gov.ic.geoint.bulleit.apache.BasicNIOPoolEntry;
import org.apache.http.nio.pool.NIOConnFactory;
import gov.ic.geoint.bulleit.apache.ConnectingIOReactor;
import org.apache.http.pool.PoolStats;

/**
 *
 */
public class ProxyConnPool extends BasicNIOConnPool {

    private static final Logger logger = Logger.getLogger(ProxyConnPool.class.getName());

    public ProxyConnPool(
            final ConnectingIOReactor ioreactor,
            final ConnectionConfig config) {        
        super(ioreactor, config);
        logger.log(Level.INFO, "ProxyConnPool initialized");
    }

    public ProxyConnPool(
            final ConnectingIOReactor ioreactor,
            final NIOConnFactory connFactory,
            final int connectTimeout) {       
        super(ioreactor, connFactory, connectTimeout);
        logger.log(Level.INFO, "ProxyConnPool initialized");
    }

    @Override
    public void release(final BasicNIOPoolEntry entry, boolean reusable) {
        logger.log(Level.INFO, "[proxy->origin] connection released {0}", entry.getConnection());
        super.release(entry, reusable);
        StringBuilder buf = new StringBuilder();
        PoolStats totals = getTotalStats();
        buf.append("[total kept alive: ").append(totals.getAvailable()).append("; ");
        buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
        buf.append(" of ").append(totals.getMax()).append("]");
        logger.log(Level.INFO, "[proxy->origin] {0}", buf.toString());
    }

}

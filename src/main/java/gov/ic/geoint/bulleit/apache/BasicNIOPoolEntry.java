package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
//import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.pool.PoolEntry;

/**
 * A basic {@link PoolEntry} implementation that represents an entry
 * in a pool of non-blocking {@link NHttpClientConnection}s identified by
 * an {@link HttpHost} instance.
 *
 * @see HttpHost
 * @since 4.2
 */
@ThreadSafe
public class BasicNIOPoolEntry extends PoolEntry<HttpHost, NHttpClientConnection> {

    private volatile int socketTimeout;

    public BasicNIOPoolEntry(final String id, final HttpHost route, final NHttpClientConnection conn) {
        super(id, route, conn);
    }

    @Override
    public void close() {
        try {
            getConnection().close();
        } catch (final IOException ignore) {
        }
    }

    @Override
    public boolean isClosed() {
        return !getConnection().isOpen();
    }

    int getSocketTimeout() {
        return socketTimeout;
    }

    void setSocketTimeout(final int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

}
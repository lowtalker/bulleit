package gov.ic.geoint.bulleit.apache;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
//import org.apache.http.nio.NHttpClientConnection;
//import org.apache.http.nio.pool.AbstractNIOConnPool;
import org.apache.http.nio.pool.NIOConnFactory;
import org.apache.http.nio.pool.SocketAddressResolver;
//import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/**
 * A very basic {@link org.apache.http.pool.ConnPool} implementation that
 * represents a pool of non-blocking {@link NHttpClientConnection} connections
 * identified by an {@link HttpHost} instance. Please note this pool
 * implementation does not support complex routes via a proxy cannot
 * differentiate between direct and proxied connections.
 *
 * @see HttpHost
 * @since 4.2
 */
@SuppressWarnings("deprecation")
@ThreadSafe
public class BasicNIOConnPool extends AbstractNIOConnPool<HttpHost, NHttpClientConnection, BasicNIOPoolEntry> {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final int connectTimeout;

    static class BasicAddressResolver implements SocketAddressResolver<HttpHost> {

        @Override
        public SocketAddress resolveLocalAddress(final HttpHost host) {

//            System.out.println("inetaddy: " + host.getAddress());
//            System.out.println("inetaddy hostaddress: " + host.getAddress().getHostAddress());

//            return new InetSocketAddress(host.getHostName(), host.getPort());

            return null;
        }

        @Override
        public SocketAddress resolveRemoteAddress(final HttpHost host) {
            final String hostname = host.getHostName();
            int port = host.getPort();
            if (port == -1) {
                if (host.getSchemeName().equalsIgnoreCase("http")) {
                    port = 80;
                } else if (host.getSchemeName().equalsIgnoreCase("https")) {
                    port = 443;
                }
            }
            return new InetSocketAddress(hostname, port);
        }
    }

    /**
     * @deprecated (4.3) use
     * {@link BasicNIOConnPool#BasicNIOConnPool(ConnectingIOReactor, NIOConnFactory, int)}
     */
    @Deprecated
    public BasicNIOConnPool(
            final ConnectingIOReactor ioreactor,
            final NIOConnFactory<HttpHost, NHttpClientConnection> connFactory,
            final HttpParams params) {
        super(ioreactor, connFactory, 2, 20);
        Args.notNull(params, "HTTP parameters");
        this.connectTimeout = params.getIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 0);
    }

    /**
     * @deprecated (4.3) use {@link BasicNIOConnPool#BasicNIOConnPool(ConnectingIOReactor,
     *   ConnectionConfig)}
     */
    @Deprecated
    public BasicNIOConnPool(
            final ConnectingIOReactor ioreactor, final HttpParams params) {
        this(ioreactor, new BasicNIOConnFactory(params), params);
    }

    /**
     * @since 4.3
     */
    public BasicNIOConnPool(
            final ConnectingIOReactor ioreactor,
            final NIOConnFactory<HttpHost, NHttpClientConnection> connFactory,
            final int connectTimeout) {
        super(ioreactor, connFactory, new BasicAddressResolver(), 2, 20);
        this.connectTimeout = connectTimeout;
    }

    /**
     * @since 4.3
     */
    public BasicNIOConnPool(
            final ConnectingIOReactor ioreactor,
            final int connectTimeout,
            final ConnectionConfig config) {
        this(ioreactor, new BasicNIOConnFactory(config), connectTimeout);
    }

    /**
     * @since 4.3
     */
    public BasicNIOConnPool(
            final ConnectingIOReactor ioreactor,
            final ConnectionConfig config) {
        this(ioreactor, new BasicNIOConnFactory(config), 0);
    }

    /**
     * @since 4.3
     */
    public BasicNIOConnPool(final ConnectingIOReactor ioreactor) {
        this(ioreactor, new BasicNIOConnFactory(ConnectionConfig.DEFAULT), 0);
    }

    /**
     * @deprecated (4.3) use {@link SocketAddressResolver}
     */
    @Deprecated
    @Override
    protected SocketAddress resolveRemoteAddress(final HttpHost host) {
        return new InetSocketAddress(host.getHostName(), host.getPort());
    }

    /**
     * @deprecated (4.3) use {@link SocketAddressResolver}
     */
    @Deprecated
    @Override
    protected SocketAddress resolveLocalAddress(final HttpHost host) {
        return null;
    }

    @Override
    protected BasicNIOPoolEntry createEntry(final HttpHost host, final NHttpClientConnection conn) {
        final BasicNIOPoolEntry entry = new BasicNIOPoolEntry(
                Long.toString(COUNTER.getAndIncrement()), host, conn);
        entry.setSocketTimeout(conn.getSocketTimeout());
        return entry;
    }

    @Override
    public Future<BasicNIOPoolEntry> lease(
            final HttpHost route,
            final Object state,
            final FutureCallback<BasicNIOPoolEntry> callback) {
        return super.lease(route, state,
                this.connectTimeout, TimeUnit.MILLISECONDS, callback);
    }

    @Override
    public Future<BasicNIOPoolEntry> lease(
            final HttpHost route,
            final Object state) {
        return super.lease(route, state,
                this.connectTimeout, TimeUnit.MILLISECONDS, null);
    }

    @Override
    protected void onLease(final BasicNIOPoolEntry entry) {
        final NHttpClientConnection conn = entry.getConnection();
        conn.setSocketTimeout(entry.getSocketTimeout());
    }

    @Override
    protected void onRelease(final BasicNIOPoolEntry entry) {
        final NHttpClientConnection conn = entry.getConnection();
        entry.setSocketTimeout(conn.getSocketTimeout());
        conn.setSocketTimeout(0);
    }

}

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

//    //@todo this is wrong...
//    public void createConnectionManager() {
//        logger.log(Level.INFO, "ProxyConnPool#createConnectionManager()");
//        /**
//         * use custom message parser / writer to customize the way HTTP messages
//         * are parsed from and written out to the data stream.
//         */
//        HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();
//
//        HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {
//            @Override
//            public HttpMessageParser<HttpResponse> create(
//                    SessionInputBuffer buffer, MessageConstraints constraints) {
//                LineParser lineParser = new BasicLineParser() {
//                    @Override
//                    public Header parseHeader(final CharArrayBuffer buffer) {
//                        try {
//                            return super.parseHeader(buffer);
//                        } catch (ParseException e) {
//                            return new BasicHeader(buffer.toString(), null);
//                        }
//                    }
//                };
//                return new DefaultHttpResponseParser(buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {
//                    @Override
//                    protected boolean reject(final CharArrayBuffer line, int count) {
//                        //try to ignore all garbage preceding a status line infinitely
//                        return false;
//                    }
//                };
//            }
//        };
//
//        /**
//         * use a custom connection factory to customize the process of
//         * initialization of outgoing HTTP connections. Beside standard
//         * connection configuration parameters, HTTP connnection can define
//         * message parser/writer routines to be employed by individual
//         * connections. ..
//         *
//         * Client HTTPConnection objects when fully initialized can be bound to
//         * an arbitrary network socket. The process of network socket
//         * initialization, its connection to a remote address and binding to a
//         * local one is controlled by a connection socket factory.
//         *
//         *
//         */
//        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connectionFactory 
//                = new ManagedHttpClientConnectionFactory();
//
//        SSLContext sslContext = SecureContextFactory.createSSLContext();
//
//        SSLNHttpClientConnectionFactory sslConnectionFactory 
//                = new SSLNHttpClientConnectionFactory(sslContext, null, null, null, ConnectionConfig.DEFAULT);
//
//        /**
//         * create a registry of custom connection socket factories for supported
//         * protocol schemes.
//         *
//         */
//        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
//                .register("http", PlainConnectionSocketFactory.INSTANCE)
//                .register("https", new SSLConnectionSocketFactory(sslContext))
//                .build();
//
//        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
//            @Override
//            //@todo this needs to be set dynamically
//            public InetAddress[] resolve(final String host) throws UnknownHostException {
//                if (host.equalsIgnoreCase("localhost")) {
//                    return new InetAddress[]{InetAddress.getByAddress(new byte[]{127, 0, 0, 1})};
//                } else {
//                    return super.resolve(host);
//                }
//            }
//        };
//
//        PoolingHttpClientConnectionManager connectionManager 
//                = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connectionFactory, dnsResolver);
//
//        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
//
//        connectionManager.setDefaultSocketConfig(socketConfig);
//        //@todo this needs to be set dynamically
//        connectionManager.setSocketConfig(new HttpHost("localhost", 80), socketConfig);
}

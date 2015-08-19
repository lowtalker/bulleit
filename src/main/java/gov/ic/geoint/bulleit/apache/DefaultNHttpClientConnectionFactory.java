package gov.ic.geoint.bulleit.apache;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.annotation.Immutable;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.ConnSupport;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParserFactory;
//import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpMessageParserFactory;
import org.apache.http.nio.NHttpMessageWriterFactory;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpParamConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/**
 * Default factory for plain (non-encrypted), non-blocking
 * {@link org.apache.http.nio.NHttpClientConnection}s.
 *
 * @since 4.2
 */
@SuppressWarnings("deprecation")
@Immutable
public class DefaultNHttpClientConnectionFactory
    implements NHttpConnectionFactory<DefaultNHttpClientConnection> {

    public static final DefaultNHttpClientConnectionFactory INSTANCE = new DefaultNHttpClientConnectionFactory();

    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final NHttpMessageParserFactory<HttpResponse> responseParserFactory;
    private final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory;
    private final ByteBufferAllocator allocator;
    private final ConnectionConfig cconfig;

    /**
     * @deprecated (4.3) use {@link
     *   DefaultNHttpClientConnectionFactory#DefaultNHttpClientConnectionFactory(
     *      NHttpMessageParserFactory, NHttpMessageWriterFactory, ByteBufferAllocator,
     *      ConnectionConfig)}
     */
    @Deprecated
    public DefaultNHttpClientConnectionFactory(
            final HttpResponseFactory responseFactory,
            final ByteBufferAllocator allocator,
            final HttpParams params) {
        super();
        Args.notNull(responseFactory, "HTTP response factory");
        Args.notNull(allocator, "Byte buffer allocator");
        Args.notNull(params, "HTTP parameters");
        this.allocator = allocator;
        this.incomingContentStrategy = null;
        this.outgoingContentStrategy = null;
        this.responseParserFactory = new DefaultHttpResponseParserFactory(null, responseFactory);
        this.requestWriterFactory = null;
        this.cconfig = HttpParamConfig.getConnectionConfig(params);
    }

    /**
     * @deprecated (4.3) use {@link
     *   DefaultNHttpClientConnectionFactory#DefaultNHttpClientConnectionFactory(
     *     ConnectionConfig)}
     */
    @Deprecated
    public DefaultNHttpClientConnectionFactory(final HttpParams params) {
        this(DefaultHttpResponseFactory.INSTANCE, HeapByteBufferAllocator.INSTANCE, params);
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(
            final ContentLengthStrategy incomingContentStrategy,
            final ContentLengthStrategy outgoingContentStrategy,
            final NHttpMessageParserFactory<HttpResponse> responseParserFactory,
            final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final ByteBufferAllocator allocator,
            final ConnectionConfig cconfig) {
        super();
        this.incomingContentStrategy = incomingContentStrategy;
        this.outgoingContentStrategy = outgoingContentStrategy;
        this.responseParserFactory = responseParserFactory;
        this.requestWriterFactory = requestWriterFactory;
        this.allocator = allocator;
        this.cconfig = cconfig != null ? cconfig : ConnectionConfig.DEFAULT;
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(
            final NHttpMessageParserFactory<HttpResponse> responseParserFactory,
            final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final ByteBufferAllocator allocator,
            final ConnectionConfig cconfig) {
        this(null, null, responseParserFactory, requestWriterFactory, allocator, cconfig);
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(
            final NHttpMessageParserFactory<HttpResponse> responseParserFactory,
            final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final ConnectionConfig cconfig) {
        this(null, null, responseParserFactory, requestWriterFactory, null, cconfig);
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(final ConnectionConfig cconfig) {
        this(null, null, null, null, null, cconfig);
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory() {
        this(null, null, null, null, null, null);
    }

    /**
     * @deprecated (4.3) no longer used.
     */
    @Deprecated
    protected DefaultNHttpClientConnection createConnection(
            final IOSession session,
            final HttpResponseFactory responseFactory,
            final ByteBufferAllocator allocator,
            final HttpParams params) {
        return new DefaultNHttpClientConnection(session, responseFactory, allocator, params);
    }

    @Override
    public DefaultNHttpClientConnection createConnection(final IOSession session) {
        return new DefaultNHttpClientConnection(
                session,
                this.cconfig.getBufferSize(),
                this.cconfig.getFragmentSizeHint(),
                this.allocator,
                ConnSupport.createDecoder(this.cconfig),
                ConnSupport.createEncoder(this.cconfig),
                this.cconfig.getMessageConstraints(),
                this.incomingContentStrategy,
                this.outgoingContentStrategy,
                this.requestWriterFactory,
                this.responseParserFactory);
    }

}
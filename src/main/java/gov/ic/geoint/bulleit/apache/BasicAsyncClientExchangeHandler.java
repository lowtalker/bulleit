package gov.ic.geoint.bulleit.apache;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.ConnectionClosedException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;
//import org.apache.http.nio.NHttpClientConnection;
//import org.apache.http.nio.protocol.HttpAsyncClientExchangeHandler;
//import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
//import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;

/**
 * Basic implementation of {@link HttpAsyncClientExchangeHandler} that executes
 * a single HTTP request / response exchange.
 *
 * @param <T> the result type of request execution.
 * @since 4.3
 */
public class BasicAsyncClientExchangeHandler<T> implements HttpAsyncClientExchangeHandler {

    private final HttpAsyncRequestProducer requestProducer;
    private final HttpAsyncResponseConsumer<T> responseConsumer;
    private final BasicFuture<T> future;
    private final HttpContext localContext;
    private final NHttpClientConnection conn;
    private final HttpProcessor httppocessor;
    private final ConnectionReuseStrategy connReuseStrategy;
    private final AtomicBoolean requestSent;
    private final AtomicBoolean keepAlive;
    private final AtomicBoolean closed;
    private static final Logger logger = Logger.getLogger(BasicAsyncClientExchangeHandler.class.getName());

    /**
     * Creates new instance of BasicAsyncRequestExecutionHandler.
     *
     * @param requestProducer the request producer.
     * @param responseConsumer the response consumer.
     * @param callback the future callback invoked when the operation is completed.
     * @param localContext the local execution context.
     * @param conn the actual connection.
     * @param httppocessor the HTTP protocol processor.
     * @param connReuseStrategy the connection re-use strategy.
     */
    public BasicAsyncClientExchangeHandler(
            final HttpAsyncRequestProducer requestProducer,
            final HttpAsyncResponseConsumer<T> responseConsumer,
            final FutureCallback<T> callback,
            final HttpContext localContext,
            final NHttpClientConnection conn,
            final HttpProcessor httppocessor,
            final ConnectionReuseStrategy connReuseStrategy) {
        super();
        this.requestProducer = Args.notNull(requestProducer, "Request producer");
        this.responseConsumer = Args.notNull(responseConsumer, "Response consumer");
        this.future = new BasicFuture<>(callback);
        this.localContext = Args.notNull(localContext, "HTTP context");
        this.conn = Args.notNull(conn, "HTTP connection");
        this.httppocessor = Args.notNull(httppocessor, "HTTP processor");
        this.connReuseStrategy = connReuseStrategy != null ? connReuseStrategy :
            DefaultConnectionReuseStrategy.INSTANCE;
        this.requestSent = new AtomicBoolean(false);
        this.keepAlive = new AtomicBoolean(false);
        this.closed = new AtomicBoolean(false);
    }

    /**
     * Creates new instance of BasicAsyncRequestExecutionHandler.
     *
     * @param requestProducer the request producer.
     * @param responseConsumer the response consumer.
     * @param localContext the local execution context.
     * @param conn the actual connection.
     * @param httppocessor the HTTP protocol processor.
     */
    public BasicAsyncClientExchangeHandler(
            final HttpAsyncRequestProducer requestProducer,
            final HttpAsyncResponseConsumer<T> responseConsumer,
            final HttpContext localContext,
            final NHttpClientConnection conn,
            final HttpProcessor httppocessor) {
        this(requestProducer, responseConsumer, null, localContext, conn, httppocessor, null);
    }

    public Future<T> getFuture() {
        return this.future;
    }

    private void releaseResources() {
        try {
            this.responseConsumer.close();
        } catch (final IOException ex) {
            logger.log(Level.WARNING, "ProxyResponse Consumer unable to release resources: {0}", ex);
        }
        try {
            this.requestProducer.close();
        } catch (final IOException ex) {
            logger.log(Level.WARNING, "ProxyRequestProducer unable to close connection {0}", ex);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.closed.compareAndSet(false, true)) {
            releaseResources();
            if (!this.future.isDone()) {
                this.future.cancel();
            }
        }
    }

    @Override
    public HttpRequest generateRequest() throws IOException, HttpException {
        if (isDone()) {
            return null;
        }
        final HttpRequest request = this.requestProducer.generateRequest();
        this.localContext.setAttribute(HttpCoreContext.HTTP_REQUEST, request);
        this.localContext.setAttribute(HttpCoreContext.HTTP_CONNECTION, this.conn);
        this.httppocessor.process(request, this.localContext);
        return request;
    }

    @Override
    public void produceContent(
            final ContentEncoder encoder, final IOControl ioctrl) throws IOException {
        this.requestProducer.produceContent(encoder, ioctrl);
    }

    @Override
    public void requestCompleted() {
        this.requestProducer.requestCompleted(this.localContext);
        this.requestSent.set(true);
    }

    @Override
    public void responseReceived(final HttpResponse response) throws IOException, HttpException {
        this.localContext.setAttribute(HttpCoreContext.HTTP_RESPONSE, response);
        this.httppocessor.process(response, this.localContext);
        this.responseConsumer.responseReceived(response);
        this.keepAlive.set(this.connReuseStrategy.keepAlive(response, this.localContext));
    }

    @Override
    public void consumeContent(
            final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
        this.responseConsumer.consumeContent(decoder, ioctrl);
    }

    @Override
    public void responseCompleted() throws IOException {
        try {
            if (!this.keepAlive.get()) {
                this.conn.close();
            }
            this.responseConsumer.responseCompleted(this.localContext);
            final T result = this.responseConsumer.getResult();
            final Exception ex = this.responseConsumer.getException();
            if (result != null) {
                this.future.completed(result);
            } else {
                this.future.failed(ex);
            }
            if (this.closed.compareAndSet(false, true)) {
                releaseResources();
            }
        } catch (final RuntimeException ex) {
            failed(ex);
            throw ex;
        }
    }

    @Override
    public void inputTerminated() {
        failed(new ConnectionClosedException("Connection closed"));
    }

    @Override
    public void failed(final Exception ex) {
        if (this.closed.compareAndSet(false, true)) {
            try {
                if (!this.requestSent.get()) {
                    this.requestProducer.failed(ex);
                }
                this.responseConsumer.failed(ex);
            } finally {
                try {
                    this.future.failed(ex);
                } finally {
                    releaseResources();
                }
            }
        }
    }

    @Override
    public boolean cancel() {
        if (this.closed.compareAndSet(false, true)) {
            try {
                try {
                    return this.responseConsumer.cancel();
                } finally {
                    this.future.cancel();
                }
            } finally {
                releaseResources();
            }
        }
        return false;
    }

    @Override
    public boolean isDone() {
        return this.responseConsumer.isDone();
    }

}
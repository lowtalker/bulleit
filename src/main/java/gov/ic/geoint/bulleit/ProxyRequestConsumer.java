package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.BasicNIOConnPool;
import gov.ic.geoint.bulleit.apache.HttpAsyncRequestConsumer;
import gov.ic.geoint.bulleit.apache.HttpAsyncRequester;
import gov.ic.geoint.bulleit.apache.IOControl;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
//import gov.ic.geoint.bulleit.apache.BasicNIOConnPool;
//import gov.ic.geoint.bulleit.apache.HttpAsyncRequestConsumer;
import org.apache.http.nio.ContentDecoder;
//import org.apache.http.nio.IOControl;
//import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
//import gov.ic.geoint.bulleit.apache.HttpAsyncRequester;
//import gov.ic.geoint.bulleit.apache.IOControl;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
class ProxyRequestConsumer implements HttpAsyncRequestConsumer<ProxyHttpExchange> {

    private final ProxyHttpExchange httpExchange;
    private final HttpAsyncRequester executor;
    private final BasicNIOConnPool connPool;
    private static final Logger logger = Logger.getLogger(ProxyRequestConsumer.class.getName());
    private volatile boolean completed;

    public ProxyRequestConsumer(
            final ProxyHttpExchange httpExchange,
            final HttpAsyncRequester executor,
            final BasicNIOConnPool connPool) {
        super();
        logger.log(Level.INFO, "ProxyRequestConsumer initialized");
        this.httpExchange = httpExchange;
        this.executor = executor;
        this.connPool = connPool;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void requestReceived(final HttpRequest request) {
        logger.log(Level.INFO, "ProxyRequestConsumer#requestReceived");
        synchronized (this.httpExchange) {
            logger.log(Level.INFO, "[client->proxy] ", 
                    new Object[]{this.httpExchange.getId(), request.getRequestLine()});
            this.httpExchange.setRequest(request);
            this.executor.execute(
                    new ProxyRequestProducer(this.httpExchange),
                    new ProxyResponseConsumer(this.httpExchange),
                    this.connPool);
        }
    }

    @Override
    public void consumeContent(final ContentDecoder decoder,
            final IOControl ioctrl) throws IOException {
        logger.log(Level.INFO, "ProxyReqeustConsumer#consumeContent");
        synchronized (this.httpExchange) {

            this.httpExchange.setClientIOControl(ioctrl);
            // Receive data from the client
            ByteBuffer buf = this.httpExchange.getInBuffer();
            int n = decoder.read(buf);
            System.out.println("[client->proxy] " + this.httpExchange.getId() + " " + n + " bytes read");
            if (decoder.isCompleted()) {
                System.out.println("[client->proxy] " + this.httpExchange.getId() + " content fully read");
            }
            // If the buffer is full, suspend client input until there is free
            // space in the buffer
            if (!buf.hasRemaining()) {
                ioctrl.suspendInput();
                System.out.println("[client->proxy] " + this.httpExchange.getId() + " suspend client input");
            }
            // If there is some content in the input buffer make sure origin
            // output is active
            if (buf.position() > 0) {
                if (this.httpExchange.getOriginIOControl() != null) {
                    this.httpExchange.getOriginIOControl().requestOutput();
                    System.out.println("[client->proxy] " + this.httpExchange.getId() + " request origin output");
                }
            }
        }
    }

    @Override
    public void requestCompleted(final HttpContext context) {
        logger.log(Level.INFO, "ProxyRequestConsumer#requestCompleted");
        synchronized (this.httpExchange) {
            this.completed = true;
            logger.log(Level.INFO, "[client->proxy] {0} request completed", this.httpExchange.getId());
            this.httpExchange.setRequestReceived();
            
            if (this.httpExchange.getOriginIOControl() != null) {
                this.httpExchange.getOriginIOControl().requestOutput();
                logger.log(Level.INFO, "[client->proxy] {0} request origin output", this.httpExchange.getId());
            }
        }
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public ProxyHttpExchange getResult() {
        return this.httpExchange;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }

    @Override
    public void failed(final Exception ex) {
        logger.log(Level.INFO, "[client->proxy] ", ex.toString());
    }

}

package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.HttpAsyncRequestProducer;
import gov.ic.geoint.bulleit.apache.IOControl;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;
//import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class ProxyRequestProducer implements HttpAsyncRequestProducer {

    private final ProxyHttpExchange httpExchange;
    private static final Logger logger = Logger.getLogger(ProxyRequestProducer.class.getName());

    public ProxyRequestProducer(final ProxyHttpExchange httpExchange) {
        super();
        this.httpExchange = httpExchange;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HttpHost getTarget() {
        synchronized (this.httpExchange) {
            return this.httpExchange.getTarget();
        }
    }

    @Override
    public HttpRequest generateRequest() {
        synchronized (this.httpExchange) {
            HttpRequest request = this.httpExchange.getRequest();
            logger.log(Level.INFO, "[proxy->origin] {0} {1}", new Object[]{this.httpExchange.getId(), request.getRequestLine()});
            //@todo  Rewrite request!!!!
            if (request instanceof HttpEntityEnclosingRequest) {
                BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest(
                        request.getRequestLine());
                r.setEntity(((HttpEntityEnclosingRequest) request).getEntity());
                return r;
            } else {
                return new BasicHttpRequest(request.getRequestLine());
            }
        }
    }

    @Override
    public void produceContent(
            final ContentEncoder encoder, final IOControl ioctrl) throws IOException {
        synchronized (this.httpExchange) {
            this.httpExchange.setOriginIOControl(ioctrl);
            // Send data to the origin server
            ByteBuffer buf = this.httpExchange.getInBuffer();
            buf.flip();
            int n = encoder.write(buf);
            buf.compact();
            logger.log(Level.INFO, "[proxy->origin] {0} {1} bytes written", new Object[]{this.httpExchange.getId(), n});
                // If there is space in the buffer and the message has not been
            // transferred, make sure the client is sending more data
            if (buf.hasRemaining() && !this.httpExchange.isRequestReceived()) {
                if (this.httpExchange.getClientIOControl() != null) {
                    this.httpExchange.getClientIOControl().requestInput();
                    logger.log(Level.INFO, "[proxy->origin] {0} request client input", this.httpExchange.getId());
                }
            }
            if (buf.position() == 0) {
                if (this.httpExchange.isRequestReceived()) {
                    encoder.complete();
                    logger.log(Level.INFO, "[proxy->origin] {0} content fully written", this.httpExchange.getId());
                } else {
                        // Input buffer is empty. Wait until the client fills up
                    // the buffer
                    ioctrl.suspendOutput();
                    logger.log(Level.INFO, "[proxy->origin] {0} suspend origin output", this.httpExchange.getId());
                }
            }
        }
    }

    @Override
    public void requestCompleted(final HttpContext context) {
        synchronized (this.httpExchange) {
            logger.log(Level.INFO, "[proxy->origin] {0} request completed", this.httpExchange.getId());
        }
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public void resetRequest() {
    }

    @Override
    public void failed(final Exception ex) {
        logger.log(Level.INFO, "[proxy->origin] ", ex.toString());
    }

}

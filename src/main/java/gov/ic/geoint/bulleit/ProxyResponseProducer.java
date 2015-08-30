package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.HttpAsyncResponseProducer;
import gov.ic.geoint.bulleit.apache.IOControl;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;
//import org.apache.http.nio.protocol.HttpAsyncResponseProducer;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
class ProxyResponseProducer implements HttpAsyncResponseProducer {

    private final ProxyHttpExchange httpExchange;
    private static final Logger logger = Logger.getLogger(ProxyResponseProducer.class.getName());

    public ProxyResponseProducer(final ProxyHttpExchange httpExchange) {
        super();
        this.httpExchange = httpExchange;
    }

    @Override
    public void close() throws IOException {
        this.httpExchange.reset();
    }

    @Override
    public HttpResponse generateResponse() {
        synchronized (this.httpExchange) {
            HttpResponse response = this.httpExchange.getResponse();
            logger.log(Level.INFO, "[client<-proxy] {0} {1}", new Object[]{this.httpExchange.getId(), response.getStatusLine()});

            BasicHttpResponse r = new BasicHttpResponse(response.getStatusLine());
            r.setEntity(response.getEntity());           
            return r;
        }
    }

    @Override
    public void produceContent(
            final ContentEncoder encoder, final IOControl ioctrl) throws IOException {
        synchronized (this.httpExchange) {
            this.httpExchange.setClientIOControl(ioctrl);
            // Send data to the client
            ByteBuffer buf = this.httpExchange.getOutBuffer();
            System.out.println("here?here?here?here?here?here? " + buf);
            buf.flip();
            int n = encoder.write(buf);
            buf.compact();
            logger.log(Level.INFO, "[client<-proxy] {0} {1} bytes written", new Object[]{this.httpExchange.getId(), n});
            // If there is space in the buffer and the message has not been
            // transferred, make sure the origin is sending more data
            if (buf.hasRemaining() && !this.httpExchange.isResponseReceived()) {
                if (this.httpExchange.getOriginIOControl() != null) {
                    this.httpExchange.getOriginIOControl().requestInput();
                    logger.log(Level.INFO, "[client<-proxy] {0} request origin input", this.httpExchange.getId());
                }
            }
            if (buf.position() == 0) {
                if (this.httpExchange.isResponseReceived()) {
                    encoder.complete();
                    logger.log(Level.INFO, "[client<-proxy] {0} content fully written", this.httpExchange.getId());
                } else {
                    // Input buffer is empty. Wait until the origin fills up
                    // the buffer
                    ioctrl.suspendOutput();
                    logger.log(Level.INFO, "[client<-proxy] {0} suspend client output", this.httpExchange.getId());
                }
            }
        }
    }

    @Override
    public void responseCompleted(final HttpContext context) {
        synchronized (this.httpExchange) {
            logger.log(Level.INFO, "[client<-proxy] {0} response completed", this.httpExchange.getId());
        }
    }

    @Override
    public void failed(final Exception ex) {
        logger.log(Level.INFO, "[client<-proxy] {0}", ex.toString());
    }

}

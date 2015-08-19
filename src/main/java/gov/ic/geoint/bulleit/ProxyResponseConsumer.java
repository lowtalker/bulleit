package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.BasicAsyncResponseProducer;
import gov.ic.geoint.bulleit.apache.HttpAsyncExchange;
import gov.ic.geoint.bulleit.apache.HttpAsyncResponseConsumer;
import gov.ic.geoint.bulleit.apache.IOControl;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.ContentDecoder;
//import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.NStringEntity;
//import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
//import org.apache.http.nio.protocol.HttpAsyncExchange;
//import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class ProxyResponseConsumer implements HttpAsyncResponseConsumer<ProxyHttpExchange> {

        private final ProxyHttpExchange httpExchange;
        private static final Logger logger = Logger.getLogger(ProxyResponseConsumer.class.getName());
        private volatile boolean completed;

        public ProxyResponseConsumer(final ProxyHttpExchange httpExchange) {
            super();
            this.httpExchange = httpExchange;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void responseReceived(final HttpResponse response) {
            synchronized (this.httpExchange) {
                logger.log(Level.INFO, "[proxy<-origin] {0} {1}", new Object[]{this.httpExchange.getId(), response.getStatusLine()});
                this.httpExchange.setResponse(response);
                HttpAsyncExchange responseTrigger = this.httpExchange.getResponseTrigger();
                if (responseTrigger != null && !responseTrigger.isCompleted()) {
                    logger.log(Level.INFO, "[client<-proxy] {0} response triggered", this.httpExchange.getId());
                    responseTrigger.submitResponse(new ProxyResponseProducer(this.httpExchange));
                }
            }
        }

        @Override
        public void consumeContent(
                final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
            synchronized (this.httpExchange) {
                this.httpExchange.setOriginIOControl(ioctrl);
                // Receive data from the origin
                ByteBuffer buf = this.httpExchange.getOutBuffer();
                int n = decoder.read(buf);
                System.out.println("[proxy<-origin] " + this.httpExchange.getId() + " " + n + " bytes read");
                if (decoder.isCompleted()) {
                    logger.log(Level.INFO, "[proxy<-origin] {0} content fully read", this.httpExchange.getId());
                }
                // If the buffer is full, suspend origin input until there is free
                // space in the buffer
                if (!buf.hasRemaining()) {
                    ioctrl.suspendInput();
                    logger.log(Level.INFO, "[proxy<-origin] {0} suspend origin input", this.httpExchange.getId());
                }
                // If there is some content in the input buffer make sure client
                // output is active
                if (buf.position() > 0) {
                    if (this.httpExchange.getClientIOControl() != null) {
                        this.httpExchange.getClientIOControl().requestOutput();
                        logger.log(Level.INFO, "[proxy<-origin] {0} request client output", this.httpExchange.getId());
                    }
                }
            }
        }

        @Override
        public void responseCompleted(final HttpContext context) {
            synchronized (this.httpExchange) {
                if (this.completed) {
                    return;
                }
                this.completed = true;
                logger.log(Level.INFO, "[proxy<-origin] {0} response completed", this.httpExchange.getId());
                this.httpExchange.setResponseReceived();
                if (this.httpExchange.getClientIOControl() != null) {
                    this.httpExchange.getClientIOControl().requestOutput();
                    logger.log(Level.INFO, "[proxy<-origin] {0} request client output", this.httpExchange.getId());
                }
            }
        }

        @Override
        public void failed(final Exception ex) {
            synchronized (this.httpExchange) {
                if (this.completed) {
                    return;
                }
                this.completed = true;
                this.httpExchange.setException(ex);
                HttpAsyncExchange responseTrigger = this.httpExchange.getResponseTrigger();
                if (responseTrigger != null && !responseTrigger.isCompleted()) {
                    logger.log(Level.INFO, "[client<-proxy] ", new Object[]{this.httpExchange.getId(), ex});
                    int status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                    HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_0, status,
                            EnglishReasonPhraseCatalog.INSTANCE.getReason(status, Locale.US));
                    String message = ex.getMessage();
                    if (message == null) {
                        message = "Unexpected error";
                    }
                    response.setEntity(new NStringEntity(message, ContentType.DEFAULT_TEXT));
                    responseTrigger.submitResponse(new BasicAsyncResponseProducer(response));
                }
            }
        }

        @Override
        public boolean cancel() {
            synchronized (this.httpExchange) {
                if (this.completed) {
                    return false;
                }
                failed(new InterruptedIOException("Cancelled"));
                return true;
            }
        }

        @Override
        public ProxyHttpExchange getResult() {
            return this.httpExchange;
        }

        @Override
        public Exception getException() {
            return null;
        }

        @Override
        public boolean isDone() {
            return this.completed;
        }

}

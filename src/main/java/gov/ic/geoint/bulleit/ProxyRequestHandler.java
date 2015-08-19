package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.BasicAsyncResponseProducer;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import gov.ic.geoint.bulleit.apache.BasicNIOConnPool;
import gov.ic.geoint.bulleit.apache.HttpAsyncExchange;
import gov.ic.geoint.bulleit.apache.HttpAsyncRequestConsumer;
import gov.ic.geoint.bulleit.apache.HttpAsyncRequestHandler;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.NStringEntity;
//import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
//import org.apache.http.nio.protocol.HttpAsyncExchange;
//import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
//import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import gov.ic.geoint.bulleit.apache.HttpAsyncRequester;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class ProxyRequestHandler implements HttpAsyncRequestHandler<ProxyHttpExchange> {

    private final HttpHost target;
    private final HttpAsyncRequester executor;
    private final BasicNIOConnPool connPool;
    private final AtomicLong counter;
    private static final Logger logger = Logger.getLogger(ProxyRequestHandler.class.getName());

    public ProxyRequestHandler(
            final HttpHost target,
            final HttpAsyncRequester executor,
            final BasicNIOConnPool connPool) {
        super();
        logger.log(Level.INFO, "ProxyRequestHandler initialized");
        this.target = target;
        this.executor = executor;
        this.connPool = connPool;
        this.counter = new AtomicLong(1);
    }

    @Override
    public HttpAsyncRequestConsumer<ProxyHttpExchange> processRequest(
            final HttpRequest request,
            final HttpContext context) {
        ProxyHttpExchange httpExchange = (ProxyHttpExchange) context.getAttribute("http-exchange");
        if (httpExchange == null) {
            httpExchange = new ProxyHttpExchange();
            context.setAttribute("http-exchange", httpExchange);
        }
        synchronized (httpExchange) {
            httpExchange.reset();
            String id = String.format("%08X", this.counter.getAndIncrement());
            httpExchange.setId(id);
            httpExchange.setTarget(this.target);
            return new ProxyRequestConsumer(httpExchange, this.executor, this.connPool);
        }
    }

    @Override
    public void handle(
            final ProxyHttpExchange httpExchange,
            final HttpAsyncExchange responseTrigger,
            final HttpContext context) throws HttpException, IOException {
        synchronized (httpExchange) {
            Exception ex = httpExchange.getException();
            if (ex != null) {
                logger.log(Level.INFO, "[client<-proxy] {0} {1}", new Object[]{httpExchange.getId(), ex});
                int status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_0, status,
                        EnglishReasonPhraseCatalog.INSTANCE.getReason(status, Locale.US));
                String message = ex.getMessage();
                if (message == null) {
                    message = "Unexpected error";
                }
                response.setEntity(new NStringEntity(message, ContentType.DEFAULT_TEXT));
                responseTrigger.submitResponse(new BasicAsyncResponseProducer(response));
                logger.log(Level.INFO, "[client<-proxy] {0} error response triggered", httpExchange.getId());
            }
            HttpResponse response = httpExchange.getResponse();
            if (response != null) {
                responseTrigger.submitResponse(new ProxyResponseProducer(httpExchange));
                logger.log(Level.INFO, "[client<-proxy] {0} response triggered", httpExchange.getId());
            }
            // No response yet.
            httpExchange.setResponseTrigger(responseTrigger);
        }
    }
}

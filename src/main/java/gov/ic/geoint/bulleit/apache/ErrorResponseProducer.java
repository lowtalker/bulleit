package gov.ic.geoint.bulleit.apache;


import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;
//import org.apache.http.nio.entity.EntityAsyncContentProducer;
//import org.apache.http.nio.entity.HttpAsyncContentProducer;
//import org.apache.http.nio.protocol.HttpAsyncResponseProducer;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

class ErrorResponseProducer implements HttpAsyncResponseProducer {

    private final HttpResponse response;
    private final HttpEntity entity;
    private final HttpAsyncContentProducer contentProducer;
    private final boolean keepAlive;

    ErrorResponseProducer(
            final HttpResponse response,
            final HttpEntity entity,
            final boolean keepAlive) {
        super();
        this.response = response;
        this.entity = entity;
        if (entity instanceof HttpAsyncContentProducer) {
            this.contentProducer = (HttpAsyncContentProducer) entity;
        } else {
            this.contentProducer = new EntityAsyncContentProducer(entity);
        }
        this.keepAlive = keepAlive;
    }

    @Override
    public HttpResponse generateResponse() {
        if (this.keepAlive) {
            response.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        } else {
            response.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
        }
        response.setEntity(this.entity);
        return response;
    }

    @Override
    public void produceContent(
            final ContentEncoder encoder, final IOControl ioctrl) throws IOException {
        this.contentProducer.produceContent(encoder, ioctrl);
    }

    @Override
    public void responseCompleted(final HttpContext context) {
    }

    @Override
    public void failed(final Exception ex) {
    }

    @Override
    public void close() throws IOException {
        this.contentProducer.close();
    }

}


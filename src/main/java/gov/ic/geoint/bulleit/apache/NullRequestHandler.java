package gov.ic.geoint.bulleit.apache;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
//import org.apache.http.nio.protocol.HttpAsyncExchange;
//import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
//import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

class NullRequestHandler implements HttpAsyncRequestHandler<Object> {

    public NullRequestHandler() {
        super();
    }

    @Override
    public HttpAsyncRequestConsumer<Object> processRequest(
            final HttpRequest request, final HttpContext context) {
        return new NullRequestConsumer();
    }

    @Override
    public void handle(
            final Object obj,
            final HttpAsyncExchange httpexchange,
            final HttpContext context) {
        final HttpResponse response = httpexchange.getResponse();
        response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        httpexchange.submitResponse(new ErrorResponseProducer(
                response, new NStringEntity("Service not implemented", ContentType.TEXT_PLAIN), true));
    }

}
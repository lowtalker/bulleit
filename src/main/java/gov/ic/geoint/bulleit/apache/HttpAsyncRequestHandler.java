package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;

/**
 * {@code HttpAsyncRequestHandler} represents a routine for asynchronous
 * processing of a specific group of non-blocking HTTP requests. Protocol
 * handlers are designed to take care of protocol specific aspects, whereas
 * individual request handlers are expected to take care of application
 * specific HTTP processing. The main purpose of a request handler is to
 * generate a response object with a content entity to be sent back to
 * the client in response to the given request.
 *
 * @since 4.2
 */
public interface HttpAsyncRequestHandler<T> {

    /**
     * Triggered when an incoming request is received. This method should
     * return a {@link HttpAsyncRequestConsumer} that will be used to process
     * the request and consume message content if enclosed. The consumer
     * can optionally parse or transform the message content into a structured
     * object which is then passed onto
     * the {@link #handle(Object, HttpAsyncExchange, HttpContext)}
     * method for further processing.
     *
     * @param request the entity enclosing request.
     * @param context the execution context.
     * @return request consumer.
     * @throws IOException in case of an I/O error.
     * @throws HttpException in case of HTTP protocol violation or a processing
     *   problem.
     */
    HttpAsyncRequestConsumer<T> processRequest(
            HttpRequest request,
            HttpContext context) throws HttpException, IOException;

    /**
     * Triggered to complete request processing and to initiate the process of
     * generating a response. This method does not have to submit a response
     * immediately. It can defer transmission of an HTTP response back to
     * the client without blocking the I/O thread by delegating the process
     * of request handling to another service or a worker thread. HTTP response
     * can be submitted as a later a later point of time using
     * {@link HttpAsyncExchange} once response content becomes available.
     *
     * @param data request data returned by the request consumer.
     * @param httpExchange HTTP exchange.
     * @param context HTTP execution context.
     * @throws IOException in case of an I/O error.
     * @throws HttpException in case of HTTP protocol violation or a processing
     *   problem.
     */
    void handle(
            T data,
            HttpAsyncExchange httpExchange,
            HttpContext context) throws HttpException, IOException;

}

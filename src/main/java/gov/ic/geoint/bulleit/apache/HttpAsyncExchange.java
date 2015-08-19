package gov.ic.geoint.bulleit.apache;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.Cancellable;

/**
 * {@code HttpAsyncExchange} represents a server-side HTTP message exchange
 * where an HTTP response can be deferred without blocking the I/O event thread
 * and triggered asynchronously at a later point of later time.
 *
 * @since 4.2
 */
public interface HttpAsyncExchange {

    /**
     * Returns the received HTTP request message.
     *
     * @return received HTTP request message.
     */
    HttpRequest getRequest();

    /**
     * Returns the default HTTP response message. Once ready the response
     * message can submitted using {@link #submitResponse()} method.
     *
     * @return default HTTP response message.
     */
    HttpResponse getResponse();

    /**
     * Submits the default HTTP response and completed the message exchange.
     * If the response encloses an {@link org.apache.http.HttpEntity} instance
     * the entity is also expected to implement the
     * {@link org.apache.http.nio.entity.HttpAsyncContentProducer }
     * interface for efficient content streaming to a non-blocking HTTP
     * connection.
     *
     * @throws IllegalStateException if a response has already been submitted.
     */
    void submitResponse();

    /**
     * Submits an HTTP response using a custom
     * {@link HttpAsyncResponseProducer}.
     *
     * @param responseProducer
     * @throws IllegalStateException if a response has already been submitted.
     */
    void submitResponse(HttpAsyncResponseProducer responseProducer);

    /**
     * Determines whether or not the message exchange has been completed.
     *
     * @return {@code true} if the message exchange has been completed,
     * {@code false} otherwise.
     */
    boolean isCompleted();

    /**
     * Sets {@link Cancellable} callback to be invoked in case the underlying
     * connection times out or gets terminated prematurely by the client. This
     * callback can be used to cancel a long running response generating
     * process if a response is no longer needed.
     */
    void setCallback(Cancellable cancellable);

    /**
     * Sets timeout for this message exchange.
     */
    void setTimeout(int timeout);

    /**
     * Returns timeout for this message exchange.
     */
    int getTimeout();

}

package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
//import org.apache.http.nio.NHttpConnection;

/**
 * Abstract non-blocking client-side HTTP connection interface. It can be used
 * to submit HTTP requests and asynchronously receive HTTP responses.
 *
 * @see NHttpConnection
 *
 * @since 4.0
 */
public interface NHttpClientConnection extends NHttpConnection {

    /**
     * Submits {@link HttpRequest} to be sent to the target server.
     *
     * @param request HTTP request
     * @throws IOException if I/O error occurs while submitting the request
     * @throws HttpException if the HTTP request violates the HTTP protocol.
     */
    void submitRequest(HttpRequest request) throws IOException, HttpException;

    /**
     * Returns {@code true} if an HTTP request has been submitted to the
     * target server.
     *
     * @return {@code true} if an HTTP request has been submitted,
     * {@code false} otherwise.
     */
    boolean isRequestSubmitted();

    /**
     * Resets output state. This method can be used to prematurely terminate
     * processing of the outgoing HTTP request.
     */
    void resetOutput();

    /**
     * Resets input state. This method can be used to prematurely terminate
     * processing of the incoming HTTP response.
     */
    void resetInput();

}

package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
//import org.apache.http.nio.NHttpConnection;

/**
 * Abstract non-blocking server-side HTTP connection interface. It can be used
 * to receive HTTP requests and asynchronously submit HTTP responses.
 *
 * @see NHttpConnection
 *
 * @since 4.0
 */
public interface NHttpServerConnection extends NHttpConnection {

    /**
     * Submits {link @HttpResponse} to be sent to the client.
     *
     * @param response HTTP response
     *
     * @throws IOException if I/O error occurs while submitting the response
     * @throws HttpException if the HTTP response violates the HTTP protocol.
     */
    void submitResponse(HttpResponse response) throws IOException, HttpException;

    /**
     * Returns {@code true} if an HTTP response has been submitted to the
     * client.
     *
     * @return {@code true} if an HTTP response has been submitted,
     * {@code false} otherwise.
     */
    boolean isResponseSubmitted();

    /**
     * Resets output state. This method can be used to prematurely terminate
     * processing of the incoming HTTP request.
     */
    void resetInput();

    /**
     * Resets input state. This method can be used to prematurely terminate
     * processing of the outgoing HTTP response.
     */
    void resetOutput();

}
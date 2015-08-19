package gov.ic.geoint.bulleit.apache;


import org.apache.http.HttpConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

/**
 * Abstract non-blocking HTTP connection interface. Each connection contains an
 * HTTP execution context, which can be used to maintain a processing state,
 * as well as the actual {@link HttpRequest} and {@link HttpResponse} that are
 * being transmitted over this connection. Both the request and
 * the response objects can be {@code null} if there is no incoming or
 * outgoing message currently being transferred.
 * <p>
 * Please note non-blocking HTTP connections are stateful and not thread safe.
 * Input / output operations on non-blocking HTTP connections should be
 * restricted to the dispatch events triggered by the I/O event dispatch thread.
 * However, the {@link IOControl} interface is fully threading safe and can be
 * manipulated from any thread.
 *
 * @since 4.0
 */
public interface NHttpConnection extends HttpConnection, IOControl {

    public static final int ACTIVE      = 0;
    public static final int CLOSING     = 1;
    public static final int CLOSED      = 2;

    /**
     * Returns status of the connection:
     * <p>
     * {@link #ACTIVE}: connection is active.
     * <p>
     * {@link #CLOSING}: connection is being closed.
     * <p>
     * {@link #CLOSED}: connection has been closed.
     *
     * @return connection status.
     */
    int getStatus();

    /**
     * Returns the current HTTP request if one is being received / transmitted.
     * Otherwise returns {@code null}.
     *
     * @return HTTP request, if available, {@code null} otherwise.
     */
    HttpRequest getHttpRequest();

    /**
     * Returns the current HTTP response if one is being received / transmitted.
     * Otherwise returns {@code null}.
     *
     * @return HTTP response, if available, {@code null} otherwise.
     */
    HttpResponse getHttpResponse();

    /**
     * Returns an HTTP execution context associated with this connection.
     * @return HTTP context
     */
    HttpContext getContext();

}

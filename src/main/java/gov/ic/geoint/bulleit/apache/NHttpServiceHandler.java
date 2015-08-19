package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;

/**
 * Abstract server-side HTTP protocol handler.
 *
 * @since 4.0
 *
 * @deprecated (4.2) use {@link NHttpServerEventHandler}
 */
@Deprecated
public interface NHttpServiceHandler {

    /**
     * Triggered when a new incoming connection is created.
     *
     * @param conn new incoming connection HTTP connection.
     */
    void connected(NHttpServerConnection conn);

    /**
     * Triggered when a new HTTP request is received. The connection
     * passed as a parameter to this method is guaranteed to return
     * a valid HTTP request object.
     * <p>
     * If the request received encloses a request entity this method will
     * be followed a series of
     * {@link #inputReady(NHttpServerConnection, ContentDecoder)} calls
     * to transfer the request content.
     *
     * @see NHttpServerConnection
     *
     * @param conn HTTP connection that contains a new HTTP request
     */
    void requestReceived(NHttpServerConnection conn);

    /**
     * Triggered when the underlying channel is ready for reading a
     * new portion of the request entity through the corresponding
     * content decoder.
     * <p>
     * If the content consumer is unable to process the incoming content,
     * input event notifications can be temporarily suspended using
     * {@link IOControl} interface.
     *
     * @see NHttpServerConnection
     * @see ContentDecoder
     * @see IOControl
     *
     * @param conn HTTP connection that can produce a new portion of the
     * incoming request content.
     * @param decoder The content decoder to use to read content.
     */
    void inputReady(NHttpServerConnection conn, ContentDecoder decoder);

    /**
     * Triggered when the connection is ready to accept a new HTTP response.
     * The protocol handler does not have to submit a response if it is not
     * ready.
     *
     * @see NHttpServerConnection
     *
     * @param conn HTTP connection that contains an HTTP response
     */
    void responseReady(NHttpServerConnection conn);

    /**
     * Triggered when the underlying channel is ready for writing a
     * next portion of the response entity through the corresponding
     * content encoder.
     * <p>
     * If the content producer is unable to generate the outgoing content,
     * output event notifications can be temporarily suspended using
     * {@link IOControl} interface.
     *
     * @see NHttpServerConnection
     * @see ContentEncoder
     * @see IOControl
     *
     * @param conn HTTP connection that can accommodate a new portion
     * of the outgoing response content.
     * @param encoder The content encoder to use to write content.
     */
    void outputReady(NHttpServerConnection conn, ContentEncoder encoder);

    /**
     * Triggered when an I/O error occurs while reading from or writing
     * to the underlying channel.
     *
     * @param conn HTTP connection that caused an I/O error
     * @param ex I/O exception
     */
    void exception(NHttpServerConnection conn, IOException ex);

    /**
     * Triggered when an HTTP protocol violation occurs while receiving
     * an HTTP request.
     *
     * @param conn HTTP connection that caused an HTTP protocol violation
     * @param ex HTTP protocol violation exception
     */
    void exception(NHttpServerConnection conn, HttpException ex);

    /**
     * Triggered when no input is detected on this connection over the
     * maximum period of inactivity.
     *
     * @param conn HTTP connection that caused timeout condition.
     */
    void timeout(NHttpServerConnection conn);

    /**
     * Triggered when the connection is closed.
     *
     * @param conn closed HTTP connection.
     */
    void closed(NHttpServerConnection conn);

}

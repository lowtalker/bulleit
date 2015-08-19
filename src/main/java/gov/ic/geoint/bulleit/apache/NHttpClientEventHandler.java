package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;

/**
 * Abstract client-side HTTP protocol handler.
 *
 * @since 4.2
 */
public interface NHttpClientEventHandler {

    /**
     * Triggered when a new outgoing connection is created.
     *
     * @param conn new outgoing HTTP connection.
     * @param attachment an object that was attached to the session request
     */
    void connected(
            NHttpClientConnection conn,
            Object attachment) throws IOException, HttpException;

    /**
     * Triggered when the connection is ready to accept a new HTTP request.
     * The protocol handler does not have to submit a request if it is not
     * ready.
     *
     * @see NHttpClientConnection
     *
     * @param conn HTTP connection that is ready to accept a new HTTP request.
     */
    void requestReady(
            NHttpClientConnection conn) throws IOException, HttpException;

    /**
     * Triggered when an HTTP response is received. The connection
     * passed as a parameter to this method is guaranteed to return
     * a valid HTTP response object.
     * <p>
     * If the response received encloses a response entity this method will
     * be followed by a series of
     * {@link #inputReady(NHttpClientConnection, ContentDecoder)} calls
     * to transfer the response content.
     *
     * @see NHttpClientConnection
     *
     * @param conn HTTP connection that contains an HTTP response
     */
    void responseReceived(
            NHttpClientConnection conn) throws IOException, HttpException;

    /**
     * Triggered when the underlying channel is ready for reading a
     * new portion of the response entity through the corresponding
     * content decoder.
     * <p>
     * If the content consumer is unable to process incoming content,
     * input event notifications can be temporarily suspended using
     * {@link IOControl} interface (super interface of {@link NHttpClientConnection}).
     * <p>
     * Please note that the {@link NHttpClientConnection} and {@link ContentDecoder}
     * objects are not thread-safe and should only be used within the context of
     * this method call. The {@link IOControl} object can be shared and used on other
     * thread to resume input event notifications when the handler is capable of
     * processing more content.
     *
     * @see NHttpClientConnection
     * @see ContentDecoder
     * @see IOControl
     *
     * @param conn HTTP connection that can produce a new portion of the
     * incoming response content.
     * @param decoder The content decoder to use to read content.
     */
    void inputReady(
            NHttpClientConnection conn,
            ContentDecoder decoder) throws IOException, HttpException;

    /**
     * Triggered when the underlying channel is ready for writing a next portion
     * of the request entity through the corresponding content encoder.
     * <p>
     * If the content producer is unable to generate outgoing content,
     * output event notifications can be temporarily suspended using
     * {@link IOControl} interface (super interface of {@link NHttpClientConnection}).
     * <p>
     * Please note that the {@link NHttpClientConnection} and {@link ContentEncoder}
     * objects are not thread-safe and should only be used within the context of
     * this method call. The {@link IOControl} object can be shared and used on other
     * thread to resume output event notifications when more content is made available.
     *
     * @see NHttpClientConnection
     * @see ContentEncoder
     * @see IOControl
     *
     * @param conn HTTP connection that can accommodate a new portion
     * of the outgoing request content.
     * @param encoder The content encoder to use to write content.
     */
    void outputReady(
            NHttpClientConnection conn,
            ContentEncoder encoder) throws IOException, HttpException;

    /**
     * Triggered when the connection is closed by the opposite end point
     * (half-closed).
     *
     * @param conn half-closed HTTP connection.
     */
    void endOfInput(
            NHttpClientConnection conn) throws IOException;

    /**
     * Triggered when no input is detected on this connection over the
     * maximum period of inactivity.
     *
     * @param conn HTTP connection that caused timeout condition.
     */
    void timeout(
            NHttpClientConnection conn) throws IOException, HttpException;

    /**
     * Triggered when the connection is closed.
     *
     * @param conn closed HTTP connection.
     */
    void closed(NHttpClientConnection conn);

    /**
     * Triggered if an error occurs during the HTTP exchange.
     *
     * @param conn HTTP connection that caused an I/O error
     * @param ex exception
     */
    void exception(NHttpClientConnection conn, Exception ex);

}

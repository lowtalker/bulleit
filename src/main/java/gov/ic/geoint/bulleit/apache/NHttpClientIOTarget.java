package gov.ic.geoint.bulleit.apache;


/**
 * Extended version of the {@link NHttpClientConnection} used by
 * {@link org.apache.http.nio.reactor.IOEventDispatch} implementations
 * to inform client-side connection objects of I/O events.
 *
 * @since 4.0
 *
 * @deprecated (4.2) no longer used
 */
@Deprecated
public interface NHttpClientIOTarget extends NHttpClientConnection {

    /**
     * Triggered when the connection is ready to consume input.
     *
     * @param handler the client protocol handler.
     */
    void consumeInput(NHttpClientHandler handler);

    /**
     * Triggered when the connection is ready to produce output.
     *
     * @param handler the client protocol handler.
     */
    void produceOutput(NHttpClientHandler handler);

}

package gov.ic.geoint.bulleit.apache;


/**
 * Extended version of the {@link NHttpServerConnection} used by
 * {@link org.apache.http.nio.reactor.IOEventDispatch } implementations
 * to inform server-side connection objects of I/O events.
 *
 * @since 4.0
 *
 * @deprecated (4.2) no longer used
 */
@Deprecated
public interface NHttpServerIOTarget extends NHttpServerConnection {

    /**
     * Triggered when the connection is ready to consume input.
     *
     * @param handler the server protocol handler.
     */
    void consumeInput(NHttpServiceHandler handler);

    /**
     * Triggered when the connection is ready to produce output.
     *
     * @param handler the server protocol handler.
     */
    void produceOutput(NHttpServiceHandler handler);

}

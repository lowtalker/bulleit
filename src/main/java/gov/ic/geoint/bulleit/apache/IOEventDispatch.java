package gov.ic.geoint.bulleit.apache;

import org.apache.http.nio.reactor.IOSession;


/**
 * IOEventDispatch interface is used by I/O reactors to notify clients of I/O
 * events pending for a particular session. All methods of this interface are
 * executed on a dispatch thread of the I/O reactor. Therefore, it is important
 * that processing that takes place in the event methods will not block the
 * dispatch thread for too long, as the I/O reactor will be unable to react to
 * other events.
 *
 * @since 4.0
 */
public interface IOEventDispatch {

    /**
     * Attribute name of an object that represents a non-blocking connection.
     */
    public static final String CONNECTION_KEY = "http.connection";

    /**
     * Triggered after the given session has been just created.
     *
     * @param session the I/O session.
     */
    void connected(IOSession session);

    /**
     * Triggered when the given session has input pending.
     *
     * @param session the I/O session.
     */
    void inputReady(IOSession session);

    /**
     * Triggered when the given session is ready for output.
     *
     * @param session the I/O session.
     */
    void outputReady(IOSession session);

    /**
     * Triggered when the given session as timed out.
     *
     * @param session the I/O session.
     */
    void timeout(IOSession session);

    /**
     * Triggered when the given session has been terminated.
     *
     * @param session the I/O session.
     */
    void disconnected(IOSession session);

}
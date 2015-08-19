package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

/**
 * Connection input/output control interface. It can be used to control interest
 * in I/O event notifications for non-blocking HTTP connections.
 * <p>
 * Implementations of this interface are expected to be threading safe.
 * Therefore it can be used to request / suspend I/O event notifications from
 * any thread of execution.
 *
 * @since 4.0
 */
public interface IOControl {

    /**
     * Requests event notifications to be triggered when the underlying
     * channel is ready for input operations.
     */
    void requestInput();

    /**
     * Suspends event notifications about the underlying channel being
     * ready for input operations.
     */
    void suspendInput();

    /**
     * Requests event notifications to be triggered when the underlying
     * channel is ready for output operations.
     */
    void requestOutput();

    /**
     * Suspends event notifications about the underlying channel being
     * ready for output operations.
     */
    void suspendOutput();

    /**
     * Shuts down the underlying channel.
     *
     * @throws IOException in an error occurs
     */
    void shutdown() throws IOException;

}
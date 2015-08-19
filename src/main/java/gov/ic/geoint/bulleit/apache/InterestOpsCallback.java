package gov.ic.geoint.bulleit.apache;



/**
 * Callback interface used internally by I/O session implementations to delegate execution
 * of a {@link java.nio.channels.SelectionKey#interestOps(int)} operation to the I/O reactor.
 *
 * @since 4.1
 */
interface InterestOpsCallback {

    void addInterestOps(InterestOpEntry entry);

}

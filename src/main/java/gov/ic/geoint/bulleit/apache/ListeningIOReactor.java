package gov.ic.geoint.bulleit.apache;


import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;
//import org.apache.http.nio.reactor.IOReactor;
import org.apache.http.nio.reactor.ListenerEndpoint;

/**
 * ListeningIOReactor represents an I/O reactor capable of listening for
 * incoming connections on one or several ports.
 *
 * @since 4.0
 */
public interface ListeningIOReactor extends IOReactor {

    /**
     * Opens a new listener endpoint with the given socket address. Once
     * the endpoint is fully initialized it starts accepting incoming
     * connections and propagates I/O activity notifications to the I/O event
     * dispatcher.
     * <p>
     * {@link ListenerEndpoint#waitFor()} can be used to wait for the
     *  listener to be come ready to accept incoming connections.
     * <p>
     * {@link ListenerEndpoint#close()} can be used to shut down
     * the listener even before it is fully initialized.
     *
     * @param address the socket address to listen on.
     * @return listener endpoint.
     */
    ListenerEndpoint listen(SocketAddress address);

    /**
     * Suspends the I/O reactor preventing it from accepting new connections on
     * all active endpoints.
     *
     * @throws IOException in case of an I/O error.
     */
    void pause()
        throws IOException;

    /**
     * Resumes the I/O reactor restoring its ability to accept incoming
     * connections on all active endpoints.
     *
     * @throws IOException in case of an I/O error.
     */
    void resume()
        throws IOException;

    /**
     * Returns a set of endpoints for this I/O reactor.
     *
     * @return set of endpoints.
     */
    Set<ListenerEndpoint> getEndpoints();

}

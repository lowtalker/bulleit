package gov.ic.geoint.bulleit.apache;


import org.apache.http.nio.reactor.IOSession;

/**
 * Factory for {@link NHttpConnection} instances.
 *
 * @param <T>
 * @since 4.2
 */
public interface NHttpConnectionFactory<T extends NHttpConnection> {

    T createConnection(IOSession session);

}

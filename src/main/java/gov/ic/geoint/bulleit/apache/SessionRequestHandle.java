package gov.ic.geoint.bulleit.apache;



import org.apache.http.annotation.Immutable;
import org.apache.http.util.Args;

/**
 * Session request handle class used by I/O reactor implementations to keep
 * a reference to a {@link org.apache.http.nio.reactor.SessionRequest} along
 * with the time the request was made.
 *
 * @since 4.0
 */
@Immutable
public class SessionRequestHandle {

    private final SessionRequestImpl sessionRequest;
    private final long requestTime;

    public SessionRequestHandle(final SessionRequestImpl sessionRequest) {
        super();
        Args.notNull(sessionRequest, "Session request");
        this.sessionRequest = sessionRequest;
        this.requestTime = System.currentTimeMillis();
    }

    public SessionRequestImpl getSessionRequest() {
        return this.sessionRequest;
    }

    public long getRequestTime() {
        return this.requestTime;
    }

}

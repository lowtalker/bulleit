package gov.ic.geoint.bulleit.apache;


import org.apache.http.nio.reactor.IOSession;
import org.apache.http.protocol.HttpContext;

class SessionHttpContext implements HttpContext {

    private final IOSession iosession;

    public SessionHttpContext(final IOSession iosession) {
        super();
        this.iosession = iosession;
    }

    @Override
    public Object getAttribute(final String id) {
        return this.iosession.getAttribute(id);
    }

    @Override
    public Object removeAttribute(final String id) {
        return this.iosession.removeAttribute(id);
    }

    @Override
    public void setAttribute(final String id, final Object obj) {
        this.iosession.setAttribute(id, obj);
    }

}

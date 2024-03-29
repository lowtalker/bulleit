package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.HttpAsyncExchange;
import gov.ic.geoint.bulleit.apache.IOControl;
import gov.ic.geoint.bulleit.config.Destination;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
//import org.apache.http.nio.IOControl;
//import org.apache.http.nio.protocol.HttpAsyncExchange;

/**
 *
 */
class ProxyHttpExchange {

    private final ByteBuffer inBuffer;
    private final ByteBuffer outBuffer;

    private volatile String id;
    private volatile HttpHost target;
    private volatile Destination destination;
    private volatile HttpAsyncExchange responseTrigger;
    private volatile IOControl originIOControl;
    private volatile IOControl clientIOControl;
    private volatile HttpRequest request;
    private volatile boolean requestReceived;
    private volatile HttpResponse response;
    private volatile boolean responseReceived;
    private volatile Exception ex;

    private static final Logger logger = Logger.getLogger(ProxyHttpExchange.class.getName());

    public ProxyHttpExchange() {
        super();
        logger.log(Level.FINE, "ProxyHttpExchange initialized");
        this.inBuffer = ByteBuffer.allocateDirect(10240);
        this.outBuffer = ByteBuffer.allocateDirect(10240);
    }

    public ByteBuffer getInBuffer() {
        return this.inBuffer;
    }

    public ByteBuffer getOutBuffer() {
        return this.outBuffer;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setDestination(final Destination destination) {
        this.destination = destination;
    }

    public Destination getDestination() {
        return this.destination;
    }

    public HttpHost getTarget() {
        return this.target;
    }

    public void setTarget(final HttpHost target) {
        this.target = target;
    }

    public HttpRequest getRequest() {
        return this.request;
    }

    public void setRequest(final HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return this.response;
    }

    public void setResponse(final HttpResponse response) {
        this.response = response;
    }

    public HttpAsyncExchange getResponseTrigger() {
        return this.responseTrigger;
    }

    public void setResponseTrigger(final HttpAsyncExchange responseTrigger) {
        this.responseTrigger = responseTrigger;
    }

    public IOControl getClientIOControl() {
        return this.clientIOControl;
    }

    public void setClientIOControl(final IOControl clientIOControl) {
        this.clientIOControl = clientIOControl;
    }

    public IOControl getOriginIOControl() {
        return this.originIOControl;
    }

    public void setOriginIOControl(final IOControl originIOControl) {
        this.originIOControl = originIOControl;
    }

    public boolean isRequestReceived() {
        return this.requestReceived;
    }

    public void setRequestReceived() {
        this.requestReceived = true;
    }

    public boolean isResponseReceived() {
        return this.responseReceived;
    }

    public void setResponseReceived() {
        this.responseReceived = true;
    }

    public Exception getException() {
        return this.ex;
    }

    public void setException(final Exception ex) {
        this.ex = ex;
    }

    public void reset() {
        this.inBuffer.clear();
        this.outBuffer.clear();
        this.target = null;
        this.id = null;
        this.responseTrigger = null;
        this.clientIOControl = null;
        this.originIOControl = null;
        this.request = null;
        this.requestReceived = false;
        this.response = null;
        this.responseReceived = false;
        this.ex = null;
    }

}

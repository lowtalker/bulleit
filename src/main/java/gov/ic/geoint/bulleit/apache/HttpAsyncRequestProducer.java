package gov.ic.geoint.bulleit.apache;


import java.io.Closeable;
import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;

/**
 * {@code HttpAsyncRequestProducer} is a callback interface whose methods
 * get invoked to generate an HTTP request message and to stream message
 * content to a non-blocking HTTP connection.
 * <p>
 * Repeatable request producers capable of generating the same request
 * message more than once can be reset to their initial state by calling
 * the {@link #resetRequest()} method, at which point request producers are
 * expected to release currently allocated resources that are no longer needed
 * or re-acquire resources needed to repeat the process.
 *
 * @since 4.2
 */
public interface HttpAsyncRequestProducer extends Closeable {

    /**
     * Invoked to obtain the request target host.
     */
    HttpHost getTarget();

    /**
     * Invoked to generate a HTTP request message head. The message is expected
     * to implement the {@link org.apache.http.HttpEntityEnclosingRequest} interface
     * if it is to enclose a content entity. The {@link #produceContent(
     * ContentEncoder, IOControl)} method will not be invoked if
     * {@link org.apache.http.HttpEntityEnclosingRequest#getEntity()}
     * returns {@code null}.
     *
     * @return HTTP request message.
     * @throws HttpException in case of HTTP protocol violation
     * @throws IOException in case of an I/O error
     */
    HttpRequest generateRequest() throws IOException, HttpException;

    /**
     * Invoked to write out a chunk of content to the {@link ContentEncoder}.
     * The {@link IOControl} interface can be used to suspend output event
     * notifications if the producer is temporarily unable to produce more content.
     * <p>
     * When all content is finished, the producer <b>MUST</b> call
     * {@link ContentEncoder#complete()}. Failure to do so may cause the entity
     * to be incorrectly delimited.
     * <p>
     * Please note that the {@link ContentEncoder} object is not thread-safe and
     * should only be used within the context of this method call.
     * The {@link IOControl} object can be shared and used on other thread
     * to resume output event notifications when more content is made available.
     *
     * @param encoder content encoder.
     * @param ioctrl I/O control of the underlying connection.
     * @throws IOException in case of an I/O error
     */
    void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException;

    /**
     * Invoked to signal that the request has been fully written out.
     *
     * @param context HTTP context
     */
    void requestCompleted(HttpContext context);

    /**
     * Invoked to signal that the response processing terminated abnormally.
     *
     * @param ex exception
     */
    void failed(Exception ex);

    /**
     * Determines whether or not this producer is capable of producing
     * HTTP request messages more than once.
     */
    boolean isRepeatable();

    /**
     * Invoked to reset the producer to its initial state. Repeatable request
     * producers are expected to release currently allocated resources that are
     * no longer needed or re-acquire resources needed to repeat the process.
     *
     * @throws IOException in case of an I/O error
     */
    void resetRequest() throws IOException;

}

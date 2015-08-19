package gov.ic.geoint.bulleit.apache;


import java.io.Closeable;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;

/**
 * {@code HttpAsyncResponseProducer} is a callback interface whose methods
 * get invoked to generate an HTTP response message and to stream message
 * content to a non-blocking HTTP connection on the server side.
 *
 * @since 4.2
 */
public interface HttpAsyncResponseProducer extends Closeable {

    /**
     * Invoked to generate a HTTP response message head.
     *
     * @return HTTP response message.
     */
    HttpResponse generateResponse();

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
     * Invoked to signal that the response has been fully written out.
     *
     * @param context HTTP context
     */
    void responseCompleted(HttpContext context);

    /**
     * Invoked to signal that the response processing terminated abnormally.
     *
     * @param ex exception
     */
    void failed(Exception ex);

}

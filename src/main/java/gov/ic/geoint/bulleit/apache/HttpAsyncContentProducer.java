package gov.ic.geoint.bulleit.apache;


import java.io.Closeable;
import java.io.IOException;

import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;

/**
 * {@code HttpAsyncContentProducer} is a callback interface whose methods
 * get invoked to stream out message content to a non-blocking HTTP connection.
 *
 * @since 4.2
 */
public interface HttpAsyncContentProducer extends Closeable {

    /**
     * Invoked to write out a chunk of content to the {@link ContentEncoder}.
     * The {@link IOControl} interface can be used to suspend output event
     * notifications if the entity is temporarily unable to produce more content.
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
     */
    void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException;

    /**
     * Determines whether or not this producer is capable of producing
     * its content more than once. Repeatable content producers are expected
     * to be able to recreate their content even after having been closed.
     */
    boolean isRepeatable();

}


package gov.ic.geoint.bulleit.apache;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.IOControl;
import org.apache.http.util.Args;

/**
 * Basic implementation of {@link HttpAsyncContentProducer} that relies on
 * inefficient and potentially blocking I/O operation redirection through
 * {@link Channels#newChannel(java.io.InputStream)}.
 *
 * @since 4.2
 */
@NotThreadSafe
public class EntityAsyncContentProducer implements HttpAsyncContentProducer {

    private final HttpEntity entity;
    private final ByteBuffer buffer;
    private ReadableByteChannel channel;

    public EntityAsyncContentProducer(final HttpEntity entity) {
        super();
        Args.notNull(entity, "HTTP entity");
        this.entity = entity;
        this.buffer = ByteBuffer.allocate(4096);
    }

    @Override
    public void produceContent(
            final ContentEncoder encoder, final IOControl ioctrl) throws IOException {
        if (this.channel == null) {
            this.channel = Channels.newChannel(this.entity.getContent());
        }
        final int i = this.channel.read(this.buffer);  //decryption done here
        this.buffer.flip();
        encoder.write(this.buffer);
        final boolean buffering = this.buffer.hasRemaining();
        this.buffer.compact();
        if (i == -1 && !buffering) {
            encoder.complete();
            close();
        }
    }

    @Override
    public boolean isRepeatable() {
        return this.entity.isRepeatable();
    }

    @Override
    public void close() throws IOException {
        final ReadableByteChannel local = this.channel;
        this.channel = null;
        if (local != null) {
            local.close();
        }
        if (this.entity.isStreaming()) {
            final InputStream instream = this.entity.getContent();
            instream.close();
        }
    }

    @Override
    public String toString() {
        return this.entity.toString();
    }

}

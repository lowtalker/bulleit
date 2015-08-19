package gov.ic.geoint.bulleit.apache;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.HttpRequest;
import org.apache.http.nio.ContentDecoder;
//import org.apache.http.nio.IOControl;
//import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.protocol.HttpContext;

class NullRequestConsumer implements HttpAsyncRequestConsumer<Object> {

    private final ByteBuffer buffer;
    private volatile boolean completed;

    NullRequestConsumer() {
        super();
        this.buffer = ByteBuffer.allocate(2048);
    }

    @Override
    public void requestReceived(final HttpRequest request) {
    }

    @Override
    public void consumeContent(
            final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
        int lastRead;
        do {
            this.buffer.clear();
            lastRead = decoder.read(this.buffer);
        } while (lastRead > 0);
    }

    @Override
    public void requestCompleted(final HttpContext context) {
        this.completed = true;
    }

    @Override
    public void failed(final Exception ex) {
        this.completed = true;
    }

    @Override
    public Object getResult() {
        return Boolean.valueOf(this.completed);
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public void close() throws IOException {
        this.completed = true;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }

}

package gov.ic.geoint.bulleit.apache;


import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.ListenerEndpointClosedCallback;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.util.Args;

/**
 * Default implementation of {@link ListenerEndpoint}.
 *
 * @since 4.0
 */
@ThreadSafe
public class ListenerEndpointImpl implements ListenerEndpoint {

    private volatile boolean completed;
    private volatile boolean closed;
    private volatile SelectionKey key;
    private volatile SocketAddress address;
    private volatile IOException exception;

    private final ListenerEndpointClosedCallback callback;

    public ListenerEndpointImpl(
            final SocketAddress address,
            final ListenerEndpointClosedCallback callback) {
        super();
        Args.notNull(address, "Address");
        this.address = address;
        this.callback = callback;
    }

    @Override
    public SocketAddress getAddress() {
        return this.address;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public IOException getException() {
        return this.exception;
    }

    @Override
    public void waitFor() throws InterruptedException {
        if (this.completed) {
            return;
        }
        synchronized (this) {
            while (!this.completed) {
                wait();
            }
        }
    }

    public void completed(final SocketAddress address) {
        Args.notNull(address, "Address");
        if (this.completed) {
            return;
        }
        this.completed = true;
        synchronized (this) {
            this.address = address;
            notifyAll();
        }
    }

    public void failed(final IOException exception) {
        if (exception == null) {
            return;
        }
        if (this.completed) {
            return;
        }
        this.completed = true;
        synchronized (this) {
            this.exception = exception;
            notifyAll();
        }
    }

    public void cancel() {
        if (this.completed) {
            return;
        }
        this.completed = true;
        this.closed = true;
        synchronized (this) {
            notifyAll();
        }
    }

    protected void setKey(final SelectionKey key) {
        this.key = key;
    }

    @Override
    public boolean isClosed() {
        return this.closed || (this.key != null && !this.key.isValid());
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.completed = true;
        this.closed = true;
        if (this.key != null) {
            this.key.cancel();
            final Channel channel = this.key.channel();
            try {
                channel.close();
            } catch (final IOException ignore) {}
        }
        if (this.callback != null) {
            this.callback.endpointClosed(this);
        }
        synchronized (this) {
            notifyAll();
        }
    }

}

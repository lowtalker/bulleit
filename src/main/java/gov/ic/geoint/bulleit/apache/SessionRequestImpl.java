package gov.ic.geoint.bulleit.apache;



import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.util.Args;

/**
 * Default implementation of {@link SessionRequest}.
 *
 * @since 4.0
 */
@ThreadSafe
public class SessionRequestImpl implements SessionRequest {

    private volatile boolean completed;
    private volatile SelectionKey key;

    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;
    private final Object attachment;
    private final SessionRequestCallback callback;

    private volatile int connectTimeout;
    private volatile IOSession session = null;
    private volatile IOException exception = null;

    public SessionRequestImpl(
            final SocketAddress remoteAddress,
            final SocketAddress localAddress,
            final Object attachment,
            final SessionRequestCallback callback) {
        super();
        Args.notNull(remoteAddress, "Remote address");
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.attachment = attachment;
        this.callback = callback;
        this.connectTimeout = 0;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override
    public Object getAttachment() {
        return this.attachment;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    protected void setKey(final SelectionKey key) {
        this.key = key;
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

    @Override
    public IOSession getSession() {
        synchronized (this) {
            return this.session;
        }
    }

    @Override
    public IOException getException() {
        synchronized (this) {
            return this.exception;
        }
    }

    public void completed(final IOSession session) {
        Args.notNull(session, "Session");
        if (this.completed) {
            return;
        }
        this.completed = true;
        synchronized (this) {
            this.session = session;
            if (this.callback != null) {
                this.callback.completed(this);
            }
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
        final SelectionKey k = this.key;
        if (k != null) {
            k.cancel();
            final Channel channel = k.channel();
            try {
                channel.close();
            } catch (final IOException ignore) {}
        }
        synchronized (this) {
            this.exception = exception;
            if (this.callback != null) {
                this.callback.failed(this);
            }
            notifyAll();
        }
    }

    public void timeout() {
        if (this.completed) {
            return;
        }
        this.completed = true;
        final SelectionKey k = this.key;
        if (k != null) {
            k.cancel();
            final Channel channel = k.channel();
            if (channel.isOpen()) {
                try {
                    channel.close();
                } catch (final IOException ignore) {}
            }
        }
        synchronized (this) {
            if (this.callback != null) {
                this.callback.timeout(this);
            }
        }
    }

    @Override
    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    public void setConnectTimeout(final int timeout) {
        if (this.connectTimeout != timeout) {
            this.connectTimeout = timeout;
            final SelectionKey k = this.key;
            if (k != null) {
                k.selector().wakeup();
            }
        }
    }

    @Override
    public void cancel() {
        if (this.completed) {
            return;
        }
        this.completed = true;
        final SelectionKey k = this.key;
        if (k != null) {
            k.cancel();
            final Channel channel = k.channel();
            if (channel.isOpen()) {
                try {
                    channel.close();
                } catch (final IOException ignore) {}
            }
        }
        synchronized (this) {
            if (this.callback != null) {
                this.callback.cancelled(this);
            }
            notifyAll();
        }
    }

}
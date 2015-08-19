package gov.ic.geoint.bulleit.apache;




import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.http.annotation.ThreadSafe;
//import org.apache.http.impl.nio.reactor.ChannelEntry;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
//import org.apache.http.impl.nio.reactor.SessionRequestHandle;
//import org.apache.http.impl.nio.reactor.SessionRequestImpl;
//import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Asserts;

/**
 * Default implementation of {@link ConnectingIOReactor}. This class extends
 * {@link AbstractMultiworkerIOReactor} with capability to connect to remote
 * hosts.
 *
 * @since 4.0
 */
@SuppressWarnings("deprecation")
@ThreadSafe // public methods only
public class DefaultConnectingIOReactor extends AbstractMultiworkerIOReactor
        implements ConnectingIOReactor {

    private final Queue<SessionRequestImpl> requestQueue;

    private long lastTimeoutCheck;

    /**
     * Creates an instance of DefaultConnectingIOReactor with the given configuration.
     *
     * @param config I/O reactor configuration.
     * @param threadFactory the factory to create threads.
     *   Can be {@code null}.
     * @throws IOReactorException in case if a non-recoverable I/O error.
     *
     * @since 4.2
     */
    public DefaultConnectingIOReactor(
            final IOReactorConfig config,
            final ThreadFactory threadFactory) throws IOReactorException {
        super(config, threadFactory);
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.lastTimeoutCheck = System.currentTimeMillis();
    }

    /**
     * Creates an instance of DefaultConnectingIOReactor with the given configuration.
     *
     * @param config I/O reactor configuration.
     *   Can be {@code null}.
     * @throws IOReactorException in case if a non-recoverable I/O error.
     *
     * @since 4.2
     */
    public DefaultConnectingIOReactor(final IOReactorConfig config) throws IOReactorException {
        this(config, null);
    }

    /**
     * Creates an instance of DefaultConnectingIOReactor with default configuration.
     *
     * @throws IOReactorException in case if a non-recoverable I/O error.
     *
     * @since 4.2
     */
    public DefaultConnectingIOReactor() throws IOReactorException {
        this(null, null);
    }

    /**
     * @param workerCount
     * @param threadFactory
     * @param params
     * @throws org.apache.http.nio.reactor.IOReactorException
     * @deprecated (4.2)
     */
   
    @Deprecated
    public DefaultConnectingIOReactor(
            final int workerCount,
            final ThreadFactory threadFactory,
            final HttpParams params) throws IOReactorException {
        this(convert(workerCount, params), threadFactory);
    }

    /**
     * @param workerCount
     * @param params
     * @throws org.apache.http.nio.reactor.IOReactorException
     * @deprecated (4.2) use {@link DefaultConnectingIOReactor#DefaultConnectingIOReactor(IOReactorConfig)}
     */
    @Deprecated
    public DefaultConnectingIOReactor(
            final int workerCount,
            final HttpParams params) throws IOReactorException {
        this(convert(workerCount, params), null);
    }

    @Override
    protected void cancelRequests() throws IOReactorException {
        SessionRequestImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            request.cancel();
        }
    }

    @Override
    protected void processEvents(final int readyCount) throws IOReactorException {
        processSessionRequests();

        if (readyCount > 0) {
            final Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
            for (final SelectionKey key : selectedKeys) {

                processEvent(key);

            }
            selectedKeys.clear();
        }

        final long currentTime = System.currentTimeMillis();
        if ((currentTime - this.lastTimeoutCheck) >= this.selectTimeout) {
            this.lastTimeoutCheck = currentTime;
            final Set<SelectionKey> keys = this.selector.keys();
            processTimeouts(keys);
        }
    }

    private void processEvent(final SelectionKey key) {
        try {

            if (key.isConnectable()) {

                final SocketChannel channel = (SocketChannel) key.channel();
                // Get request handle
                final SessionRequestHandle requestHandle = (SessionRequestHandle) key.attachment();
                final SessionRequestImpl sessionRequest = requestHandle.getSessionRequest();

                // Finish connection process
                try {
                    channel.finishConnect();
                } catch (final IOException ex) {
                    sessionRequest.failed(ex);
                }
                key.cancel();
                key.attach(null);
                if (!sessionRequest.isCompleted()) {
                    addChannel(new ChannelEntry(channel, sessionRequest));
                } else {
                    try {
                        channel.close();
                    } catch (IOException ignore) {
                    }
                }
            }

        } catch (final CancelledKeyException ex) {
            final SessionRequestHandle requestHandle = (SessionRequestHandle) key.attachment();
            key.attach(null);
            if (requestHandle != null) {
                final SessionRequestImpl sessionRequest = requestHandle.getSessionRequest();
                if (sessionRequest != null) {
                    sessionRequest.cancel();
                }
            }
        }
    }

    private void processTimeouts(final Set<SelectionKey> keys) {
        final long now = System.currentTimeMillis();
        for (final SelectionKey key : keys) {
            final Object attachment = key.attachment();

            if (attachment instanceof SessionRequestHandle) {
                final SessionRequestHandle handle = (SessionRequestHandle) key.attachment();
                final SessionRequestImpl sessionRequest = handle.getSessionRequest();
                final int timeout = sessionRequest.getConnectTimeout();
                if (timeout > 0) {
                    if (handle.getRequestTime() + timeout < now) {
                        sessionRequest.timeout();
                    }
                }
            }

        }
    }

    @Override
    public SessionRequest connect(
            final SocketAddress remoteAddress,
            final SocketAddress localAddress,
            final Object attachment,
            final SessionRequestCallback callback) {
        Asserts.check(this.status.compareTo(IOReactorStatus.ACTIVE) <= 0,
            "I/O reactor has been shut down");
        final SessionRequestImpl sessionRequest = new SessionRequestImpl(
                remoteAddress, localAddress, attachment, callback);
        sessionRequest.setConnectTimeout(this.config.getConnectTimeout());

        this.requestQueue.add(sessionRequest);
        this.selector.wakeup();

        return sessionRequest;
    }

    private void validateAddress(final SocketAddress address) throws UnknownHostException {
        if (address == null) {
            return;
        }
        if (address instanceof InetSocketAddress) {
            final InetSocketAddress endpoint = (InetSocketAddress) address;
            if (endpoint.isUnresolved()) {
                throw new UnknownHostException(endpoint.getHostName());
            }
        }
    }

    private void processSessionRequests() throws IOReactorException {
        SessionRequestImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            if (request.isCompleted()) {
                continue;
            }
            final SocketChannel socketChannel;
            try {
                socketChannel = SocketChannel.open();
                
            } catch (final IOException ex) {
                throw new IOReactorException("Failure opening socket", ex);
            }
            try {
                validateAddress(request.getLocalAddress());
                validateAddress(request.getRemoteAddress());    //@todo the remote address here is null.  

                socketChannel.configureBlocking(false);
                prepareSocket(socketChannel.socket());

                if (request.getLocalAddress() != null) {
                    final Socket sock = socketChannel.socket();
                    sock.setReuseAddress(this.config.isSoReuseAddress());
                    sock.bind(request.getLocalAddress());
                }
                final boolean connected = socketChannel.connect(request.getRemoteAddress());//@todo it fails here.....>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                if (connected) {
                    final ChannelEntry entry = new ChannelEntry(socketChannel, request);
                    addChannel(entry);
                    continue;
                }
            } catch (final IOException ex) {
                closeChannel(socketChannel);
                request.failed(ex);
                return;
            }

            final SessionRequestHandle requestHandle = new SessionRequestHandle(request);
            try {
                final SelectionKey key = socketChannel.register(this.selector, SelectionKey.OP_CONNECT,
                        requestHandle);
                request.setKey(key);
            } catch (final IOException ex) {
                closeChannel(socketChannel);
                throw new IOReactorException("Failure registering channel " +
                        "with the selector", ex);
            }
        }
    }

}

package gov.ic.geoint.bulleit.apache;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.impl.nio.reactor.ListenerEndpointClosedCallback;
//import org.apache.http.impl.nio.reactor.ListenerEndpointImpl;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.ListenerEndpoint;
//import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Asserts;

/**
 * Default implementation of {@link ListeningIOReactor}. This class extends
 * {@link AbstractMultiworkerIOReactor} with capability to listen for incoming
 * connections.
 *
 * @since 4.0
 */
@SuppressWarnings("deprecation")
@ThreadSafe // public methods only
public class DefaultListeningIOReactor extends AbstractMultiworkerIOReactor
        implements ListeningIOReactor {

    private final Queue<ListenerEndpointImpl> requestQueue;
    private final Set<ListenerEndpointImpl> endpoints;
    private final Set<SocketAddress> pausedEndpoints;

    private volatile boolean paused;

    /**
     * Creates an instance of DefaultListeningIOReactor with the given configuration.
     *
     * @param config I/O reactor configuration.
     * @param threadFactory the factory to create threads.
     *   Can be {@code null}.
     * @throws IOReactorException in case if a non-recoverable I/O error.
     *
     * @since 4.2
     */
    public DefaultListeningIOReactor(
            final IOReactorConfig config,
            final ThreadFactory threadFactory) throws IOReactorException {
        super(config, threadFactory);
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.endpoints = Collections.synchronizedSet(new HashSet<ListenerEndpointImpl>());
        this.pausedEndpoints = new HashSet<>();
    }

    /**
     * Creates an instance of DefaultListeningIOReactor with the given configuration.
     *
     * @param config I/O reactor configuration.
     *   Can be {@code null}.
     * @throws IOReactorException in case if a non-recoverable I/O error.
     *
     * @since 4.2
     */
    public DefaultListeningIOReactor(final IOReactorConfig config) throws IOReactorException {
        this(config, null);
    }

    /**
     * Creates an instance of DefaultListeningIOReactor with default configuration.
     *
     * @throws IOReactorException in case if a non-recoverable I/O error.
     *
     * @since 4.2
     */
    public DefaultListeningIOReactor() throws IOReactorException {
        this(null, null);
    }

    /**
     * @param workerCount
     * @param threadFactory
     * @param params
     * @throws org.apache.http.nio.reactor.IOReactorException
     * @deprecated (4.2) use {@link Defa
     *
     */
    @Deprecated    
    public DefaultListeningIOReactor(
            final int workerCount,
            final ThreadFactory threadFactory,
            final HttpParams params) throws IOReactorException {
        this(convert(workerCount, params), threadFactory);
    }

    /**
     * @param workerCount
     * @param params
     * @throws org.apache.http.nio.reactor.IOReactorException
     * @deprecated (4.2) use {@link DefaultListeningIOReactor#DefaultListeningIOReactor(IOReactorConfig)}
     */
    @Deprecated
    public DefaultListeningIOReactor(
            final int workerCount,
            final HttpParams params) throws IOReactorException {
        this(convert(workerCount, params), null);
    }

    @Override
    protected void cancelRequests() throws IOReactorException {
        ListenerEndpointImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            request.cancel();
        }
    }

    @Override
    protected void processEvents(final int readyCount) throws IOReactorException {
        if (!this.paused) {
            processSessionRequests();
        }

        if (readyCount > 0) {
            final Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
            for (final SelectionKey key : selectedKeys) {

                processEvent(key);

            }
            selectedKeys.clear();
        }
    }

    private void processEvent(final SelectionKey key)
            throws IOReactorException {
        try {

            if (key.isAcceptable()) {

                final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                for (;;) {
                    SocketChannel socketChannel = null;
                    try {
                        socketChannel = serverChannel.accept();
                    } catch (final IOException ex) {
                        if (this.exceptionHandler == null ||
                                !this.exceptionHandler.handle(ex)) {
                            throw new IOReactorException(
                                    "Failure accepting connection", ex);
                        }
                    }
                    //@todo why would the socketChannel be null??
                    if (socketChannel == null) {
                        break;
                    }
                    try {
                        prepareSocket(socketChannel.socket());
                    } catch (final IOException ex) {
                        if (this.exceptionHandler == null ||
                                !this.exceptionHandler.handle(ex)) {
                            throw new IOReactorException(
                                    "Failure initalizing socket", ex);
                        }
                    }
                    final ChannelEntry entry = new ChannelEntry(socketChannel);
                    addChannel(entry);
                }
            }

        } catch (final CancelledKeyException ex) {
            final ListenerEndpoint endpoint = (ListenerEndpoint) key.attachment();
            this.endpoints.remove(endpoint);
            key.attach(null);
        }
    }

    private ListenerEndpointImpl createEndpoint(final SocketAddress address) {
        return new ListenerEndpointImpl(
                address,
                new ListenerEndpointClosedCallback() {

                    @Override
                    public void endpointClosed(final ListenerEndpoint endpoint) {
                        endpoints.remove(endpoint);
                    }

                });
    }

    @Override
    public ListenerEndpoint listen(final SocketAddress address) {
        Asserts.check(this.status.compareTo(IOReactorStatus.ACTIVE) <= 0,
                "I/O reactor has been shut down");
        final ListenerEndpointImpl request = createEndpoint(address);
        this.requestQueue.add(request);
        this.selector.wakeup();
        return request;
    }

    private void processSessionRequests() throws IOReactorException {
        ListenerEndpointImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            final SocketAddress address = request.getAddress();
            final ServerSocketChannel serverChannel;
            try {
                serverChannel = ServerSocketChannel.open();
            } catch (final IOException ex) {
                throw new IOReactorException("Failure opening server socket", ex);
            }
            try {
                final ServerSocket socket = serverChannel.socket();
                socket.setReuseAddress(this.config.isSoReuseAddress());
                if (this.config.getSoTimeout() > 0) {
                    socket.setSoTimeout(this.config.getSoTimeout());
                }
                if (this.config.getRcvBufSize() > 0) {
                    socket.setReceiveBufferSize(this.config.getRcvBufSize());
                }
                serverChannel.configureBlocking(false);
                socket.bind(address, this.config.getBacklogSize());
            } catch (final IOException ex) {
                closeChannel(serverChannel);
                request.failed(ex);
                if (this.exceptionHandler == null || !this.exceptionHandler.handle(ex)) {
                    throw new IOReactorException("Failure binding socket to address "
                            + address, ex);
                } else {
                    return;
                }
            }
            try {
                final SelectionKey key = serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
                key.attach(request);
                request.setKey(key);
            } catch (final IOException ex) {
                closeChannel(serverChannel);
                throw new IOReactorException("Failure registering channel " +
                        "with the selector", ex);
            }

            this.endpoints.add(request);
            request.completed(serverChannel.socket().getLocalSocketAddress());
        }
    }

    @Override
    public Set<ListenerEndpoint> getEndpoints() {
        final Set<ListenerEndpoint> set = new HashSet<>();
        synchronized (this.endpoints) {
            final Iterator<ListenerEndpointImpl> it = this.endpoints.iterator();
            while (it.hasNext()) {
                final ListenerEndpoint endpoint = it.next();
                if (!endpoint.isClosed()) {
                    set.add(endpoint);
                } else {
                    it.remove();
                }
            }
        }
        return set;
    }

    @Override
    public void pause() throws IOException {
        if (this.paused) {
            return;
        }
        this.paused = true;
        synchronized (this.endpoints) {
            for (final ListenerEndpointImpl endpoint : this.endpoints) {
                if (!endpoint.isClosed()) {
                    endpoint.close();
                    this.pausedEndpoints.add(endpoint.getAddress());
                }
            }
            this.endpoints.clear();
        }
    }

    @Override
    public void resume() throws IOException {
        if (!this.paused) {
            return;
        }
        this.paused = false;
        for (final SocketAddress address: this.pausedEndpoints) {
            final ListenerEndpointImpl request = createEndpoint(address);
            this.requestQueue.add(request);
        }
        this.pausedEndpoints.clear();
        this.selector.wakeup();
    }

}
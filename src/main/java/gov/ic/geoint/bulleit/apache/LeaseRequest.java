package gov.ic.geoint.bulleit.apache;




import org.apache.http.annotation.Immutable;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.pool.PoolEntry;

import java.util.concurrent.atomic.AtomicBoolean;

@Immutable
class LeaseRequest<T, C, E extends PoolEntry<T, C>> {

    private final T route;
    private final Object state;
    private final long connectTimeout;
    private final long deadline;
    private final BasicFuture<E> future;
    private final AtomicBoolean completed;
    private volatile E result;
    private volatile Exception ex;

    /**
     * Contructor
     * @param route route
     * @param state state
     * @param connectTimeout http connection timeout
     * @param leaseTimeout timeout to wait in a request queue until kicked off
     * @param future future callback
     */
    public LeaseRequest(
            final T route,
            final Object state,
            final long connectTimeout,
            final long leaseTimeout,
            final BasicFuture<E> future) {
        super();
        this.route = route;
        this.state = state;
        this.connectTimeout = connectTimeout;
        this.deadline = leaseTimeout > 0 ? System.currentTimeMillis() + leaseTimeout :
                Long.MAX_VALUE;
        this.future = future;
        this.completed = new AtomicBoolean(false);
    }

    public T getRoute() {
        return this.route;
    }

    public Object getState() {
        return this.state;
    }

    public long getConnectTimeout() {
        return this.connectTimeout;
    }

    public long getDeadline() {
        return this.deadline;
    }

    public boolean isDone() {
        return this.completed.get();
    }

    public void failed(final Exception ex) {
        if (this.completed.compareAndSet(false, true)) {
            this.ex = ex;
        }
    }

    public void completed(final E result) {
        if (this.completed.compareAndSet(false, true)) {
            this.result = result;
        }
    }

    public BasicFuture<E> getFuture() {
        return this.future;
    }

    public E getResult() {
        return this.result;
    }

    public Exception getException() {
        return this.ex;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        buffer.append(this.route);
        buffer.append("][");
        buffer.append(this.state);
        buffer.append("]");
        return buffer.toString();
    }

}
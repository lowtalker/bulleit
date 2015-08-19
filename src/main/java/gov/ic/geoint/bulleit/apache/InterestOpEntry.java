package gov.ic.geoint.bulleit.apache;


import java.nio.channels.SelectionKey;

import org.apache.http.util.Args;

/**
 * Helper class, representing an entry on an {@link java.nio.channels.SelectionKey#interestOps(int)
 * interestOps(int)} queue.
 *
 * @since 4.1
 */
class InterestOpEntry {

    private final SelectionKey key;
    private final int eventMask;

    public InterestOpEntry(final SelectionKey key, final int eventMask) {
        super();
        Args.notNull(key, "Selection key");
        this.key = key;
        this.eventMask = eventMask;
    }

    public SelectionKey getSelectionKey() {
        return this.key;
    }

    public int getEventMask() {
        return this.eventMask;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof InterestOpEntry) {
            final InterestOpEntry that = (InterestOpEntry) obj;
            return this.key.equals(that.key);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

}

package gov.ic.geoint.bulleit.apache;


import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
//import org.apache.http.nio.NHttpClientConnection;
//import org.apache.http.nio.NHttpClientEventHandler;
//import org.apache.http.nio.NHttpClientHandler;

/**
 * @deprecated (4.2)
 */
@Deprecated
class NHttpClientEventHandlerAdaptor implements NHttpClientEventHandler {

    private final NHttpClientHandler handler;

    public NHttpClientEventHandlerAdaptor(final NHttpClientHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public void connected(final NHttpClientConnection conn, final Object attachment) {
        this.handler.connected(conn, attachment);
    }

    @Override
    public void requestReady(
            final NHttpClientConnection conn) throws IOException, HttpException {
        this.handler.requestReady(conn);
    }

    @Override
    public void responseReceived(
            final NHttpClientConnection conn) throws IOException, HttpException {
        this.handler.responseReceived(conn);
    }

    @Override
    public void inputReady(
            final NHttpClientConnection conn,
            final ContentDecoder decoder) throws IOException, HttpException {
        this.handler.inputReady(conn, decoder);
    }

    @Override
    public void outputReady(
            final NHttpClientConnection conn,
            final ContentEncoder encoder) throws IOException, HttpException {
        this.handler.outputReady(conn, encoder);
    }

    @Override
    public void exception(final NHttpClientConnection conn, final Exception ex) {
        if (ex instanceof HttpException) {
            this.handler.exception(conn, (HttpException) ex);
        } else if (ex instanceof IOException) {
            this.handler.exception(conn, (IOException) ex);
        } else {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new Error("Unexpected exception: ", ex);
            }
        }
    }

    @Override
    public void endOfInput(final NHttpClientConnection conn) throws IOException {
        conn.close();
    }

    @Override
    public void timeout(final NHttpClientConnection conn) {
        this.handler.timeout(conn);
    }

    @Override
    public void closed(final NHttpClientConnection conn) {
        this.handler.closed(conn);
    }

}

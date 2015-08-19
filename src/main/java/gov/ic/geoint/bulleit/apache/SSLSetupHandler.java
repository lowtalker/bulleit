package gov.ic.geoint.bulleit.apache;


import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.apache.http.nio.reactor.IOSession;

/**
 * Callback interface that can be used to customize various aspects of
 * the TLS/SSl protocol.
 *
 * @since 4.2
 */
public interface SSLSetupHandler {

    /**
     * Triggered when the SSL connection is being initialized. Custom handlers
     * can use this callback to customize properties of the {@link SSLEngine}
     * used to establish the SSL session.
     *
     * @param sslengine the SSL engine.
     * @throws SSLException if case of SSL protocol error.
     */
    //FIXME: fix type
    void initalize(SSLEngine sslengine) throws SSLException;

    /**
     * Triggered when the SSL connection has been established and initial SSL
     * handshake has been successfully completed. Custom handlers can use
     * this callback to verify properties of the {@link SSLSession}.
     * For instance this would be the right place to enforce SSL cipher
     * strength, validate certificate chain and do hostname checks.
     *
     * @param iosession the underlying IOSession for the SSL connection.
     * @param sslsession newly created SSL session.
     * @throws SSLException if case of SSL protocol error.
     */
    void verify(IOSession iosession, SSLSession sslsession) throws SSLException;

}
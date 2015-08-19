package gov.ic.geoint.bulleit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

/**
 *
 */
public class SecureContextFactory {

    private static SSLContext context;
    private static final Logger logger = Logger.getLogger(SecureContextFactory.class.getName());

    public static SSLContext createSSLContext() {

        if (context != null) {
            return context;

        } else {

//            String keyStorePath = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
            String keyStorePath = System.getProperty("java.home") + "/lib/security/keystoreproxy.pfx".replace('/', File.separatorChar);
            String trustStorePath = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
            char[] password = "changeit".toCharArray();
            KeyStore keyStore;
            KeyStore trustStore;

            try (FileInputStream fis = new FileInputStream(keyStorePath)) {

                //KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore = KeyStore.getInstance("PKCS12");

                keyStore.load(fis, password);

                try (FileInputStream fin = new FileInputStream(trustStorePath)) {
                    trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(fin, password);
                }

                context = SSLContexts.custom()
                        .loadKeyMaterial(keyStore, password)
                        .useProtocol("TLS")
                        .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                        .build();
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableKeyException e) {
                logger.log(Level.SEVERE, "unable to load keystore/truststore and create a secure socket connection {0}", e);
            }
            return context;
        }
    }
}

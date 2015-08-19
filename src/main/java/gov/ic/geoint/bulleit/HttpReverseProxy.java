package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.BasicNIOConnFactory;
import gov.ic.geoint.bulleit.apache.ConnectingIOReactor;
import gov.ic.geoint.bulleit.apache.DefaultConnectingIOReactor;
import gov.ic.geoint.bulleit.apache.DefaultHttpClientIODispatch;
import gov.ic.geoint.bulleit.apache.DefaultHttpServerIODispatch;
import gov.ic.geoint.bulleit.apache.DefaultListeningIOReactor;
import gov.ic.geoint.bulleit.apache.HttpAsyncRequester;
import gov.ic.geoint.bulleit.apache.IOEventDispatch;
import gov.ic.geoint.bulleit.apache.ListeningIOReactor;
import gov.ic.geoint.bulleit.apache.UriHttpAsyncRequestHandlerMapper;
import gov.ic.geoint.bulleit.config.Domains;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;


import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.config.ConnectionConfig;
import gov.ic.geoint.bulleit.interceptors.RequestRedirectProcessor;
import gov.ic.geoint.bulleit.interceptors.ResponseRewriter;
//import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
//import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
//import gov.ic.geoint.bulleit.apache.BasicNIOConnFactory;
//import gov.ic.geoint.bulleit.apache.ConnectingIOReactor;
//import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
//import gov.ic.geoint.bulleit.apache.DefaultConnectingIOReactor;
//import gov.ic.geoint.bulleit.apache.DefaultHttpClientIODispatch;
//import gov.ic.geoint.bulleit.apache.DefaultHttpServerIODispatch;
//import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
//import gov.ic.geoint.bulleit.apache.HttpAsyncRequester;
//import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
//import gov.ic.geoint.bulleit.apache.ConnectingIOReactor;
//import org.apache.http.nio.reactor.IOEventDispatch;
//import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.RequestContent;

/**
 *
 */
public class HttpReverseProxy {

    private static final Logger logger
            = Logger.getLogger(HttpReverseProxy.class.getName());

    public static void main(String[] args) throws Exception {

        IOReactorConfig config
                = IOReactorConfig.custom()
                .setIoThreadCount(10)
                .setSoTimeout(30000)
                .setConnectTimeout(30000)                        
                .build();

        final ConnectingIOReactor connectingIOReactor
                = new DefaultConnectingIOReactor(config);
        final ListeningIOReactor listeningIOReactor
                = new DefaultListeningIOReactor(config);
        final ListeningIOReactor securelisteningIOReactor
                = new DefaultListeningIOReactor(config);

        // Set up HTTP protocol processor for incoming connections
        HttpProcessor inhttpproc
                = new ImmutableHttpProcessor(
                        new HttpResponseInterceptor[]{
                            new ResponseDate(),
                            new ResponseServer("Test/1.1"),
                            new ResponseContent(), 
                            new ResponseRewriter(),
                            new ResponseRedirectProcessor(),
                            new ResponseConnControl()});
                
        // Set up HTTP protocol processor for outgoing connections
        HttpProcessor outhttpproc
                = new ImmutableHttpProcessor(
                        new HttpRequestInterceptor[]{
                            new RequestContent(),
                            new RequestTargetHost(),
                            new RequestConnControl(),
                            new RequestRedirectProcessor(),
                            new RequestUserAgent("Test/1.1"),
                            new RequestExpectContinue(true)});

        ProxyClientProtocolHandler clientHandler
                = new ProxyClientProtocolHandler();
        HttpAsyncRequester asyncRequester
                = new HttpAsyncRequester(
                        outhttpproc,
                        new ProxyOutgoingConnectionReuseStrategy());

        ProxyConnPool connPool = createConnectionPool(connectingIOReactor);
        connPool.setMaxTotal(100);
        connPool.setDefaultMaxPerRoute(20);

        /**
         * add target domains listed in the config file to the proxy handler
         * registry
         */
        UriHttpAsyncRequestHandlerMapper handlerRegistry
                = new UriHttpAsyncRequestHandlerMapper();
        Domains proxyDomains = Domains.newInstance();
        String proxyHostURL = proxyDomains.getProxyConfig().getHostURL()
                + ":"
                + proxyDomains.getProxyConfig().getHostPort();
        
        URI uri = new URI("https://atom.io");
        System.out.println("host: " + uri.getHost());
        HttpHost secureHost = new HttpHost(uri.getHost(), 443, "https");
        
        handlerRegistry.register("*", new ProxyRequestHandler(secureHost, asyncRequester, connPool));
        
        
//        URI uri = new URI("http://www.nytimes.com");        
//        HttpHost host = new HttpHost(uri.getHost(), 80, "http"); 
//        handlerRegistry.register("*", new ProxyRequestHandler(host, asyncRequester, connPool));
        
        

//        for (Destination d : proxyDomains.getDestinations()) {
//            HttpHost target = new HttpHost(d.getDestinationUrl(), new Integer(d.getDestinationPort()), d.getScheme());
//            if (target.getSchemeName().equalsIgnoreCase("https")) {
//                handlerRegistry.register(d.getScheme()
//                        + "://"
//                        + proxyHostURL
//                        + d.getPrefix()
//                        + "*",
//                        new ProxyRequestHandler(
//                                target,
//                                asyncRequester,
//                                secureConnPool));
//            } else {
//                handlerRegistry.register(d.getScheme()
//                        + "://"
//                        + proxyHostURL
//                        + d.getPrefix()
//                        + "*",
//                        new ProxyRequestHandler(
//                                target,
//                                asyncRequester,
//                                connPool));
//            }
//        }

        ProxyServiceHandler serviceHandler
                = new ProxyServiceHandler(
                        inhttpproc,
                        new ProxyIncomingConnectionReuseStrategy(),
                        handlerRegistry);

        final IOEventDispatch connectingEventDispatch  //@todo need to add ssl here
                = new DefaultHttpClientIODispatch(
                        clientHandler,
                        ConnectionConfig.DEFAULT);

        /**
         * unsecured / secured event listeners
         */
        final IOEventDispatch listeningEventDispatch
                = createListeningEventDispatcher(serviceHandler);

        final IOEventDispatch secureListeningEventDispatch
                = createSecureListeningEventDispatcher(serviceHandler);

        /**
         * thread pool & executor for creating services
         */
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        Thread shutdownThread = new Thread(() -> {
            logger.log(Level.INFO, "executorService shutdown initiated");
            executorService.shutdown();
        });
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        //connecting reactor connections
        executorService.execute(new Thread(() -> {
            try {
                logger.log(Level.INFO, "connectingIOReactor started");
                connectingIOReactor.execute(connectingEventDispatch);
                
            } catch (InterruptedIOException ex) {
                logger.log(Level.SEVERE,
                        "event connection interrupted {0}", ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE,
                        "event connection IO failure {0}", ex);
            } finally {
                try {
                    logger.log(Level.INFO, "shutting down listening reactors");
                    listeningIOReactor.shutdown();
                    securelisteningIOReactor.shutdown();
                } catch (IOException ex2) {
                    logger.log(Level.SEVERE,
                            "failure shutting down the listening IOReactor {0}",
                            ex2);
                }
            }
        }));

        
        //unsecured connections
        executorService.execute(new Thread(() -> {
            try {
                listeningIOReactor.listen(new InetSocketAddress(
                        new Integer(
                                proxyDomains.getProxyConfig().getHostPort())));
                logger.log(Level.INFO,
                        "listening for http connection requests on port {0}",
                        proxyDomains.getProxyConfig().getHostPort());
                listeningIOReactor.execute(listeningEventDispatch);

            } catch (InterruptedIOException ex) {
                logger.log(Level.SEVERE,
                        "listening IOReactor IO interrupted {0}", ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "listening IO Exception {0}", ex);
            } finally {
                try {
                    connectingIOReactor.shutdown();
                } catch (IOException ex2) {
                    logger.log(Level.SEVERE,
                            "IO exception occurred shutting down the "
                            + "connectingIOReactor {0}", ex2);
                }
            }
        }));

        //secured connections
        executorService.execute(new Thread(() -> {
            try {
                securelisteningIOReactor.listen(new InetSocketAddress(
                        new Integer(
                                proxyDomains
                                .getProxyConfig()
                                .getSecureHostPort())));
                logger.log(Level.INFO, "listening for https connection requests "
                        + "on port {0}",
                        proxyDomains.getProxyConfig().getSecureHostPort());
                securelisteningIOReactor.execute(secureListeningEventDispatch);

            } catch (InterruptedIOException ex) {
                logger.log(Level.SEVERE,
                        "listening IOReactor IO interrupted {0}", ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "listening IO Exception {0}", ex);
            } finally {
                try {
                    connectingIOReactor.shutdown();
                } catch (IOException ex2) {
                    logger.log(Level.SEVERE,
                            "IO exception occurred shutting down the "
                            + "connectingIOReactor {0}", ex2);
                }
            }
        }));
    }

    private static IOEventDispatch createListeningEventDispatcher(
            ProxyServiceHandler serviceHandler) {
        IOEventDispatch dispatch = new DefaultHttpServerIODispatch(
                serviceHandler, ConnectionConfig.DEFAULT);
        return dispatch;
    }

    private static IOEventDispatch createSecureListeningEventDispatcher(
            ProxyServiceHandler serviceHandler) {
        SSLContext serverSSLContext = SecureContextFactory.createSSLContext();
        IOEventDispatch dispatch = new DefaultHttpServerIODispatch(serviceHandler,
                serverSSLContext, ConnectionConfig.DEFAULT);
        return dispatch;
    }

    private static ProxyConnPool createConnectionPool(
            final ConnectingIOReactor connectingIOReactor) {
        SSLContext clientSSLContext = SecureContextFactory.createSSLContext();
        BasicNIOConnFactory connectionFactory
                = new BasicNIOConnFactory(clientSSLContext,
                        null, ConnectionConfig.DEFAULT);
        ProxyConnPool connPool = new ProxyConnPool(connectingIOReactor, connectionFactory, 5000);
        return connPool;
    }
}

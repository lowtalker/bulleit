package gov.ic.geoint.bulleit;

import gov.ic.geoint.bulleit.apache.BasicAsyncResponseProducer;
import gov.ic.geoint.bulleit.config.Destination;
import gov.ic.geoint.bulleit.config.Domains;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.AbstractContentEncoder;
import org.apache.http.impl.nio.codecs.AbstractMessageParser;
import org.apache.http.impl.nio.codecs.AbstractMessageWriter;
import org.apache.http.impl.nio.codecs.ChunkDecoder;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParser;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseWriter;
import org.apache.http.impl.nio.reactor.SessionInputBufferImpl;
import org.apache.http.impl.nio.reactor.SessionOutputBufferImpl;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.message.LineFormatter;
import org.apache.http.message.LineParser;
import org.apache.http.nio.NHttpMessageParser;
import org.apache.http.nio.NHttpMessageParserFactory;
import org.apache.http.nio.NHttpMessageWriter;
import org.apache.http.nio.NHttpMessageWriterFactory;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.nio.util.DirectByteBufferAllocator;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

/**
 *
 */
class EmbeddedLinkRewriter {

    /**
     * The location for this server, used when we rewrite absolute URIs
     */
    private final String ownHostName;

    /**
     * The contextPath, needed when we rewrite links.
     */
    private static String contextPath;

    /**
     * Restricted list of target URLs made accessible via this proxy
     * application.
     */
    private final Domains proxyDomains;

    private static ProxyHttpExchange exchange;

    /**
     * The regular expression that matches embedded HTML links
     */
    private static final Pattern linkPattern = Pattern.compile("\\b(href=|src=|action=|url[(]|setModulePrefix[(]\"webui.suntheme\", \")([\"\']?)(([^/]+://)([^/<>]+))?([^\"\'>\\)]*)([\"\']?)",
            Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    private static final String HTTP_IN_CONN = "http.proxy.in-conn";
    private static final String HTTP_OUT_CONN = "http.proxy.out-conn";

    private static StringBuffer page = new StringBuffer();
    private static Matcher matcher;

//    /**
//     * Regex to find absolute links.
//     */
//    private static final Pattern linkPattern
//            = Pattern.compile("\\b([^/]+://)([^/]+)([\\w/]*)",
//                    Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
//
//    /**
//     * Regex to find the path in Set-Cookie headers.
//     */
//    private static final Pattern pathAndDomainPattern
//            = Pattern.compile("\\b(path=|domain=)([^;\\s]+);?",
//                    Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
    /**
     * Logging element supplied by commons-logging.
     */
    private static final Logger logger = Logger.getLogger(EmbeddedLinkRewriter.class.getName());
    private static Destination remoteTarget;

    protected EmbeddedLinkRewriter(ProxyHttpExchange xchg) {
        exchange = xchg;
        this.ownHostName = "";
        this.contextPath = "";
        this.proxyDomains = null;
    }

    protected EmbeddedLinkRewriter() {
//        super(new SessionInputBufferImpl(4048), BasicLineParser.INSTANCE, ConnectionConfig.DEFAULT.getMessageConstraints());
        Domains proxyDomains = Domains.newInstance();
        this.ownHostName = "";
        this.contextPath = "";
        this.proxyDomains = null;

    }

    public static ByteBuffer rewriteEmbeddedLinks(ProxyHttpExchange httpExchange, Domains proxyDomains) {
        exchange = httpExchange;
        remoteTarget = discoverDestination(exchange);
        ByteBuffer buf = exchange.getOutBuffer();
        buf.flip();
        byte[] dest = new byte[buf.limit()];
        buf.get(dest, 0, buf.limit());

        String s = new String(dest, StandardCharsets.UTF_8);
        String rewrittenEntity = rewriteEntityString(s, remoteTarget);

        /**
         * need to rewrite links and then put the data back into the buffer
         */
        byte[] rewrittenDest = rewrittenEntity.getBytes(StandardCharsets.UTF_8);
        ByteBuffer rewrittenBuffer = ByteBuffer.wrap(rewrittenDest);

        buf.flip();
        return rewrittenBuffer;
    }

    /**
     * Processes the entity body looking for links, all links found are
     * rewritten. After this the string is written to the response.
     *
     * @param String entity
     * @return String rewritten entity
     * @throws IOException Is thrown when there is a problem with the streams
     */
    private static String rewriteEntityString(String entity, Destination remoteTarget) {
        /*
         * Using regex can be quite harsh sometimes so here is how
         * the regex trying to find links works
         * 
         * \\b(href=|src=|action=|url:\\s*|url\\()([\"\'])
         * This part is the identification of links, matching
         * something like href=", href=' and href=
         * 
         * (([^/]+://)([^/<>]+))?
         * This is to identify absolute paths. A link doesn't have
         * to be absolute therefore there is a ?.
         * 
         * ([^\"\'>]*)
         * This is the link
         * 
         * [\"\']
         * Ending " or '
         * 
         * $1 - link type, e.g. href=
         * $2 - ", ' or whitespace
         * $3 - The entire http://www.server.com if present
         * $4 - The protocol, e.g http:// or ftp:// 
         * $5 - The host name, e.g. www.server.com
         * $6 - The link
         */

        matcher = linkPattern.matcher(entity);

        while (matcher.find()) {
            logger.log(Level.INFO, "embedded link found: {0}", matcher.groupCount());

            String link = matcher.group(6).replaceAll("\\$", "\\\\\\$");
            if (link.length() == 0) {
                link = "/";
            }

            String rewritten = null;
            //group 4 contains the protocol: http or https
            if (matcher.group(4) != null) {
                rewritten = handleExternalLink(matcher, link, remoteTarget);

            } else if (link.startsWith("/")) {
                rewritten = handleLocalLink(matcher, link, remoteTarget);
            }

            if (rewritten != null) {
//                logger.finest("Found link " + link + " >> " + rewritten);
                matcher.appendReplacement(page, rewritten);
            }
        }
        matcher.appendTail(page);
        if (page.length() > 0) {
            return page.toString();
        } else {
            return entity;
        }
    }

    private static Destination discoverDestination(ProxyHttpExchange exchange) {
        Destination d = new Destination();
        Domains domains = Domains.newInstance();
        d = domains.selectMappedDestination(exchange.getTarget().getHostName());
        return d;
    }

    /**
     * *************************************************************************
     * helper methods
     *
     * *************************************************************************
     */
    /**
     * Rewrites a absolute path starting with a protocol e.g.
     * http://www.server.com/index.html
     *
     * @param matcher The matcher used for this link
     * @param link The part of the link after the domain name
     * @param remoteTarget
     * @return The link now rewritten
     */
    protected static String handleExternalLink(Matcher matcher, String link, Destination remoteTarget) {

//        link = link.substring(remoteTarget.getDestinationUrl().length());
        link = remoteTarget.revert(link);
//        if (matcher.find()) {
        String type = matcher.group(1);
        String separator = matcher.group(2);
        String protocol = matcher.group(4);

        String returnVal = type + separator + protocol
                + contextPath + link + separator;

        System.out.println("return value: " + returnVal);

        return type + separator + protocol
                + contextPath + link + separator;
//        }
//        return null;
    }

    protected static String handleLocalLink(Matcher matcher, String link, Destination remoteTarget) {

        if (matcher.find()) {
            String trailingSeparator = matcher.group(2);

            if (remoteTarget == null) {
                throw new IllegalArgumentException("The path: "
                        + matcher.group(6)
                        + " cannot be matched to a target URL for proxying.");
            }

            String moddedLink = matcher.group(6).replaceAll("\\$", "\\\\\\$");
            if (moddedLink.length() == 0) {
                moddedLink = "/";
            }

            if (moddedLink.startsWith("/")) {
                /**
                 * corrects the incorrect rewriting of html tags with
                 * incongruent single/double quotation marks or with nested
                 * single quotes
                 *
                 */
                Pattern localLinkPattern = Pattern.compile("[\\D]*'\"");
                Matcher localLinkMatcher = localLinkPattern.matcher(matcher.group(0));
                if (localLinkMatcher.find()) {
                    trailingSeparator = "\"";
                }
                String serverDir = remoteTarget.getPrefix();
                if (serverDir.equals("") || moddedLink.startsWith(serverDir)) {
                    moddedLink = remoteTarget.revert(moddedLink.substring(serverDir.length()));
                    String type = matcher.group(1);
                    String separator = matcher.group(2);
                    return type
                            + separator
                            + contextPath
                            + File.separator
                            + moddedLink
                            + trailingSeparator;
                }
            }
        }
        return null;
    }

    /**
     * Checks the contentType to evaluate if we should do link rewriting for
     * this content.
     *
     * @param contentType The Content-Type header
     * @return true if we need to rewrite links, false otherwise
     */
    protected boolean shouldRewrite(String contentType) {
        String lowerCased = contentType.toLowerCase();
        return (lowerCased.contains("html")
                || lowerCased.contains("utf")
                || lowerCased.contains("css")
                || lowerCased.contains("javascript")
                || lowerCased.contains("js"));
    }

    private static String revert(String uri, String prefix) {
        if (uri.startsWith(prefix)) {
            return uri;
        } else if (uri.startsWith("/")) {
            return prefix + uri.substring(1);
        } else {
            return uri;
        }
    }
}

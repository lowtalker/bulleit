package gov.ic.geoint.bulleit.interceptors;

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
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.nio.util.DirectByteBufferAllocator;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

/**
 *
 */
public class ResponseRewriter implements HttpResponseInterceptor {

    /**
     * The location for this server, used when we rewrite absolute URIs
     */
    private final String ownHostName;

    /**
     * The contextPath, needed when we rewrite links.
     */
    private final String contextPath;

    /**
     * Restricted list of target URLs made accessible via this proxy
     * application.
     */
    private final Domains proxyDomains;

    /**
     * The regular expression that matches embedded HTML links
     */
    private static final Pattern linkPattern = Pattern.compile("\\b(href=|src=|action=|url[(]|setModulePrefix[(]\"webui.suntheme\", \")([\"\']?)(([^/]+://)([^/<>]+))?([^\"\'>\\)]*)([\"\']?)",
            Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    private static final String HTTP_IN_CONN = "http.proxy.in-conn";
    private static final String HTTP_OUT_CONN = "http.proxy.out-conn";

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
    private static final Logger logger = Logger.getLogger(ResponseRewriter.class.getName());

    public ResponseRewriter(SessionInputBuffer buffer, LineParser lineParser, HttpParams params) {
//        super(buffer, lineParser, params);
        this.ownHostName = "";
        this.contextPath = "";
        this.proxyDomains = null;
    }

    public ResponseRewriter() {
//        super(new SessionInputBufferImpl(4048), BasicLineParser.INSTANCE, ConnectionConfig.DEFAULT.getMessageConstraints());
        this.ownHostName = "";
        this.contextPath = "";
        this.proxyDomains = null;

    }
//
//    public ResponseRewriter(Domains proxyDomains) {
//
//        this.contextPath = proxyDomains.getProxyConfig().getHostURL()
//                + ":" + proxyDomains.getProxyConfig().getHostPort();
//        this.proxyDomains = proxyDomains;
//        this.ownHostName = proxyDomains.getProxyConfig().getHostURL();
//

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
//         GzipDecompressingEntity gde = new GzipDecompressingEntity(e);
        HttpEntity e = response.getEntity();
        String reasonPhrase = response.getStatusLine().getReasonPhrase();
        int statusCode = response.getStatusLine().getStatusCode();

        if (e != null) {
            long contentLength = e.getContentLength();
            if (contentLength > 0) {
                System.out.println("################### contentLength: " + contentLength);
                System.out.println("################### contentType: " + e.getContentType());
            }

            NHttpMessageParserFactory<HttpResponse> responseParserFactory = new NHttpMessageParserFactory<HttpResponse>() {
                @Override
                public NHttpMessageParser<HttpResponse> create(SessionInputBuffer buffer, MessageConstraints constraints) {
                    BasicLineParser lineParser = BasicLineParser.INSTANCE;
                    NHttpMessageParser<HttpResponse> customParser = new AbstractMessageParser<HttpResponse>(buffer, lineParser, constraints) {

                        //@todo this will need to change...
                        @Override
                        protected HttpResponse createMessage(CharArrayBuffer buffer) throws HttpException, ParseException {
                            System.out.println("buffer size: " + buffer.buffer().length);
                            HttpResponse parsedResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, reasonPhrase));
                            try {
                                HttpEntity updatedEntity = new StringEntity(buffer.toString());
                                parsedResponse.setEntity(updatedEntity);
                            } catch (UnsupportedEncodingException e) {
                                logger.log(Level.SEVERE, "unable to set response body {0}", e);
                            }
                            return parsedResponse;
                        }

                        public HttpEntity parseEntity() {
                            HttpEntity entity = new BasicHttpEntity();
                            return entity;
                        }
                    };
                    return customParser;
                }
            };

            SessionInputBufferImpl inputBuffer = new SessionInputBufferImpl(8 * 1024);
            NHttpMessageParser<HttpResponse> responseParser = responseParserFactory.create(inputBuffer, MessageConstraints.DEFAULT);
//            HttpEntity ent = responseParser.parseEntity();

            HttpResponse parsedResponse = responseParser.parse();

//            if (parsedResponse == null) {
            parsedResponse = response;
//            }
            SessionOutputBufferImpl outputBuffer = new SessionOutputBufferImpl(8 * 1024);
            NHttpMessageWriter<HttpResponse> responseWriter = new DefaultHttpResponseWriter(outputBuffer);
            responseWriter.write(parsedResponse);

        }
    }

    private byte[] getContent(byte[] message) {
        int start = -1;
        byte[] content = null;

        for (int i = 0; i < message.length; i++) {
            if (start >= 0) {
                content[i - start] = message[i];
                continue;
            }
            System.out.println((char) message[i]);
            if (message[i]
                    == (byte) 13 && message[i + 1]
                    == (byte) 10 && message[i + 2]
                    == (byte) 13 && message[i + 3]
                    == (byte) 10) {
                start = i + 4;
                content = new byte[message.length - (i + 4)];
                i += 3;
            }
        }
        return content;
    }

    /**
     * Processes the stream looking for links, all links found are rewritten.
     * After this the stream is written to the response.
     *
     * @param is
     * @param remoteTarget
     * @return OutputStream
     * @throws IOException Is thrown when there is a problem with the streams
     */
//    protected OutputStream rewriteEmbeddedLinks(InputStream is, Destination remoteTarget) throws IOException {
//        /*
//         * Using regex can be quite harsh sometimes so here is how
//         * the regex trying to find links works
//         * 
//         * \\b(href=|src=|action=|url:\\s*|url\\()([\"\'])
//         * This part is the identification of links, matching
//         * something like href=", href=' and href=
//         * 
//         * (([^/]+://)([^/<>]+))?
//         * This is to identify absolute paths. A link doesn't have
//         * to be absolute therefore there is a ?.
//         * 
//         * ([^\"\'>]*)
//         * This is the link
//         * 
//         * [\"\']
//         * Ending " or '
//         * 
//         * $1 - link type, e.g. href=
//         * $2 - ", ' or whitespace
//         * $3 - The entire http://www.server.com if present
//         * $4 - The protocol, e.g http:// or ftp:// 
//         * $5 - The host name, e.g. www.server.com
//         * $6 - The link
//         */
//        StringBuffer page = new StringBuffer();
//
//        Charset charset = Charset.forName(StandardCharsets.ISO_8859_1.toString());
//        CharsetEncoder encoder = charset.newEncoder();
//        CharsetDecoder decoder = charset.newDecoder();
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//
//        StringBuilder inputString = new StringBuilder();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        String line = null;
//        while ((line = reader.readLine()) != null) {
//            inputString.append(line);
//        }
//        System.out.println("inputString: " + inputString.toString());
//
//        Matcher matcher = linkPattern.matcher(inputString.toString());
//
//        //alternative method of passing inputStream to the pattern matcher
////        ByteBuffer buf = encoder.encode(CharBuffer.wrap(bis.toString()));
////        linkPattern.matcher(buf.asCharBuffer());
////        Matcher matcher = linkPattern.matcher(decoder.decode(buf));
////        Matcher matcher = linkPattern.matcher(stream.toString(encoding));
//        while (matcher.find()) {
//
//            String link = matcher.group(6).replaceAll("\\$", "\\\\\\$");
//            if (link.length() == 0) {
//                link = "/";
//            }
//
//            String rewritten = null;
//            //group 4 contains the protocol: http or https
//            if (matcher.group(4) != null) {
//                rewritten = handleExternalLink(matcher, link, remoteTarget);
//
//            } else if (link.startsWith("/")) {
//                rewritten = handleLocalLink(matcher, link, remoteTarget);
//            }
//
//            if (rewritten != null) {
////                logger.finest("Found link " + link + " >> " + rewritten);
//                matcher.appendReplacement(page, rewritten);
//            }
//        }
//        matcher.appendTail(page);
//        os.write(page.toString().getBytes());
//        return os;
//    }
//
//    /**
//     * Rewrites a absolute path starting with a protocol e.g.
//     * http://www.server.com/index.html
//     *
//     * @param matcher The matcher used for this link
//     * @param link The part of the link after the domain name
//     * @param remoteTarget
//     * @return The link now rewritten
//     */
//    protected String handleExternalLink(Matcher matcher, String link, Destination remoteTarget) {
//
////        link = link.substring(remoteTarget.getDestinationUrl().length());
//        link = remoteTarget.revert(link);
////        if (matcher.find()) {
//        String type = matcher.group(1);
//        String separator = matcher.group(2);
//        String protocol = matcher.group(4);
//
//        String returnVal = type + separator + protocol
//                + contextPath + link + separator;
//
//        System.out.println("return value: " + returnVal);
//
//        return type + separator + protocol
//                + contextPath + link + separator;
////        }
////        return null;
//    }
//
//    protected String handleLocalLink(Matcher matcher, String link, Destination remoteTarget) {
//
//        if (matcher.find()) {
//            String trailingSeparator = matcher.group(2);
//
//            if (remoteTarget == null) {
//                throw new IllegalArgumentException("The path: "
//                        + matcher.group(6)
//                        + " cannot be matched to a target URL for proxying.");
//            }
//
//            String moddedLink = matcher.group(6).replaceAll("\\$", "\\\\\\$");
//            if (moddedLink.length() == 0) {
//                moddedLink = "/";
//            }
//
//            if (moddedLink.startsWith("/")) {
//                /**
//                 * corrects the incorrect rewriting of html tags with
//                 * incongruent single/double quotation marks or with nested
//                 * single quotes
//                 *
//                 */
//                Pattern localLinkPattern = Pattern.compile("[\\D]*'\"");
//                Matcher localLinkMatcher = localLinkPattern.matcher(matcher.group(0));
//                if (localLinkMatcher.find()) {
//                    trailingSeparator = "\"";
//                }
//                String serverDir = remoteTarget.getPrefix();
//                if (serverDir.equals("") || moddedLink.startsWith(serverDir)) {
//                    moddedLink = remoteTarget.revert(moddedLink.substring(serverDir.length()));
//                    String type = matcher.group(1);
//                    String separator = matcher.group(2);
//                    return type
//                            + separator
//                            + contextPath
//                            + File.separator
//                            + moddedLink
//                            + trailingSeparator;
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Checks the contentType to evaluate if we should do link rewriting for
//     * this content.
//     *
//     * @param contentType The Content-Type header
//     * @return true if we need to rewrite links, false otherwise
//     */
//    protected boolean shouldRewrite(String contentType) {
//        String lowerCased = contentType.toLowerCase();
//        return (lowerCased.contains("html")
//                || lowerCased.contains("utf")
//                || lowerCased.contains("css")
//                || lowerCased.contains("javascript")
//                || lowerCased.contains("js"));
//    }
//
//    protected Destination identifyTargetDestination() {
//        Destination destination = null;
//        return destination;
//    }
    /**
     * // * Checks if we have to rewrite the header and if so will rewrite it.
     * // * // * @param name // * @param originalValue // * @see
     * javax.servlet.http.HttpServletResponse#addHeader(java.lang.String,
     *
     */
//    public void addHeader(String name, String originalValue) {
//        String value;
//        if (name.equalsIgnoreCase("location")) {
//            value = rewriteLocation(originalValue);
//        } else if (name.equalsIgnoreCase("set-cookie")) {
//            value = rewriteSetCookie(originalValue);
//        } else {
//            value = originalValue;
//        }
//        
//        super.addHeader(name, value);
//    }
//    /**
//     * Checks if we have to rewrite the header and if so will rewrite it.
//     *
//     * @param name
//     * @param originalValue
//     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
//     * java.lang.String)
//     */
//    public void setHeader(String name, String originalValue) {  <== probably will be handled by a different apache interceptor
//        String value;
//        if (name.equalsIgnoreCase("location")) {
//            value = rewriteLocation(originalValue);
//        } else if (name.equalsIgnoreCase("set-cookie")) {
//            value = rewriteSetCookie(originalValue);
//        } else {
//            value = originalValue;
//        }
////        logger.log(Level.INFO,
////                "setHeader name: {0} original value: {1} new value: {2}",
////                new Object[]{name, originalValue, value});
//        super.setHeader(name, value);
//    }
//    /**
//     * Rewrites the location header. Will first locate any links in the header
//     * and then rewrite them.
//     *
//     * @param value The header value we are to rewrite
//     * @return A rewritten header
//     */
//    private String rewriteLocation(String value) {  <== will probably be handled by another apache header file interceptor
//        StringBuffer header = new StringBuffer();
//
//        Matcher matcher = linkPattern.matcher(value);
//        while (matcher.find()) {
//
//            String link = matcher.group(3).replaceAll("\\$", "\\\\$");
//            if (link.length() == 0) {
//                link = "/";
//            }
//            String location = matcher.group(2) + link;
//
//            Destination targetDestination = proxyDomains.selectMappedDestination(location);
//            if (targetDestination != null) {
//                link = link.substring(targetDestination.getDestinationUrl().length());
//                link = targetDestination.revert(link);
//                matcher.appendReplacement(header, "$1"
//                        + ownHostName + contextPath + link);
//            }
//        }
//        matcher.appendTail(header);
////        logger.log(Level.INFO, "Location header rewritten {0} >> {1}",
////                new Object[]{value, header.toString()});
//        return header.toString();
//    }
//    /**
//     * Rewrites the header Set-Cookie so that path and domain is correct.  <== may not be needed. check apache interceptor for cookie rewriting
//     *
//     * @param value The original header
//     * @return The rewritten header
//     */
//    private String rewriteSetCookie(String value) {
//        StringBuffer header = new StringBuffer();
//
//        Matcher matcher = pathAndDomainPattern.matcher(value);
//        while (matcher.find()) {
//            if (matcher.group(1).equalsIgnoreCase("path=")) {
//                String path = server.getRule().revert(matcher.group(2));
//                matcher.appendReplacement(header, "$1"
//                        + contextPath + path + ";");
//            } else {
//                matcher.appendReplacement(header, "");
//            }
//
//        }
//        //@todo set logging from finest to info
//        matcher.appendTail(header);
////        logger.log(Level.INFO, "Set-Cookie header rewritten \"{0}\" >> {1}",
////                new Object[]{value, header.toString()});
//        return header.toString();
//    }
//    /**
//     * Based on the value in the content-type header we either return the
//     * default stream or our own stream that can rewrite links.
//     *
//     * @return
//     * @throws java.io.IOException
//     * @see javax.servlet.ServletResponse#getOutputStream()
//     */
//    public ServletOutputStream getOutputStream() throws IOException {
//        if (getContentType() != null && shouldRewrite(getContentType())) {
//            return outStream;
//        } else {
//            return super.getOutputStream();
//        }
//    }
//    /**
//     * Based on the value in the content-type header we either return the
//     * default writer or our own writer. Our own writer will write to the stream
//     * that can rewrite links.
//     *
//     * @return
//     * @throws java.io.IOException
//     * @see javax.servlet.ServletResponse#getWriter()
//     */
//    public PrintWriter getWriter() throws IOException {
//        if (getContentType() != null && shouldRewrite(getContentType())) {
//            return outWriter;
//        } else {
//            return originalWriter;
//        }
//    }
//    /**
//     * Rewrites the output stream to change any links. Also closes all the
//     * streams and writers. We need the user to flush and close the streams
//     * himself as usual but we can't be sure that the writers created are used
//     * by the client and therefore we close them here.
//     *
//     * @throws IOException Is thrown when there is a problem with the streams
//     */
//    public void processStream(Destination destination) throws IOException {//     <= probably not needed; handled by the process() method
//        if (getContentType() != null && shouldRewrite(getContentType())) {
//            streamRewriter.rewrite(destination);
//        }
//        super.getOutputStream().flush();
//        super.getOutputStream().close();
//        outStream.close();
//        originalWriter.close();
//        outWriter.close();
//    }
}

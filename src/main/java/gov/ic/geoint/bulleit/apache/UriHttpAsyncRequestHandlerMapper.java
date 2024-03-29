package gov.ic.geoint.bulleit.apache;


import org.apache.http.HttpRequest;
import org.apache.http.annotation.ThreadSafe;
//import org.apache.http.protocol.UriPatternMatcher;
import org.apache.http.util.Args;

/**
 * Maintains a map of HTTP request handlers keyed by a request URI pattern.
 * <br>
 * Patterns may have three formats:
 * <ul>
 *   <li>{@code *}</li>
 *   <li>{@code *&lt;uri&gt;}</li>
 *   <li>{@code &lt;uri&gt;*}</li>
 * </ul>
 * <br>
 * This class can be used to map an instance of {@link HttpAsyncRequestHandler}
 * matching a particular request URI. Usually the mapped request handler
 * will be used to process the request with the specified request URI.
 *
 * @since 4.3
 */
@ThreadSafe
public class UriHttpAsyncRequestHandlerMapper implements HttpAsyncRequestHandlerMapper {

    private final UriPatternMatcher<HttpAsyncRequestHandler<?>> matcher;

    protected UriHttpAsyncRequestHandlerMapper(final UriPatternMatcher<HttpAsyncRequestHandler<?>> matcher) {
        super();
        this.matcher = Args.notNull(matcher, "Pattern matcher");
    }

    public UriHttpAsyncRequestHandlerMapper() {
        this(new UriPatternMatcher<HttpAsyncRequestHandler<?>>());
    }

    /**
     * Registers the given {@link HttpAsyncRequestHandler} as a handler for URIs
     * matching the given pattern.
     *
     * @param pattern the pattern to register the handler for.
     * @param handler the handler.
     */
    public void register(final String pattern, final HttpAsyncRequestHandler<?> handler) {
        matcher.register(pattern, handler);
    }

    /**
     * Removes registered handler, if exists, for the given pattern.
     *
     * @param pattern the pattern to unregister the handler for.
     */
    public void unregister(final String pattern) {
        matcher.unregister(pattern);
    }

    /**
     * Extracts request path from the given {@link HttpRequest}
     */
    protected String getRequestPath(final HttpRequest request) {
        String uriPath = request.getRequestLine().getUri();
        int index = uriPath.indexOf("?");
        if (index != -1) {
            uriPath = uriPath.substring(0, index);
        } else {
            index = uriPath.indexOf("#");
            if (index != -1) {
                uriPath = uriPath.substring(0, index);
            }
        }
        return uriPath;
    }

    /**
     * Looks up a handler matching the given request URI.
     *
     * @param request the request
     * @return handler or {@code null} if no match is found.
     */
    @Override
    public HttpAsyncRequestHandler<?> lookup(final HttpRequest request) {
        return matcher.lookup(getRequestPath(request));
    }

}

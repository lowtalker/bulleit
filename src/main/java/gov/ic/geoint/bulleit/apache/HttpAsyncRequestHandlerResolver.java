package gov.ic.geoint.bulleit.apache;

/**
 * {@code HttpAsyncRequestHandlerResolver} can be used to map an instance
 * of {@link HttpAsyncRequestHandler} matching a particular request URI.
 * Usually the mapped request handler will be used to process the request
 * with the specified request URI.
 *
 * @since 4.2
 * @deprecated see {@link HttpAsyncRequestHandlerMapper}
 */
@Deprecated
public interface HttpAsyncRequestHandlerResolver {

    /**
     * Looks up a handler matching the given request URI.
     *
     * @param requestURI the request URI
     * @return HTTP request handler or {@code null} if no match
     * is found.
     */
    HttpAsyncRequestHandler<?> lookup(String requestURI);

}

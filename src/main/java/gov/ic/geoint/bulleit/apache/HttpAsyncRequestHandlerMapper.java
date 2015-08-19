package gov.ic.geoint.bulleit.apache;

import org.apache.http.HttpRequest;

/**
 * {@code HttpAsyncRequestHandlerMapper} can be used to resolve an instance
 * of {@link HttpAsyncRequestHandler} matching a particular {@link HttpRequest}.
 * Usually the resolved request handler will be used to process the request.
 *
 * @since 4.3
 */
public interface HttpAsyncRequestHandlerMapper {

    /**
     * Looks up a handler matching the given request.
     *
     * @param request the request
     * @return HTTP request handler or {@code null} if no match
     * is found.
     */
    HttpAsyncRequestHandler<?> lookup(HttpRequest request);

}

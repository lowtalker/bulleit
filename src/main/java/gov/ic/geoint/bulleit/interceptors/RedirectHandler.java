package gov.ic.geoint.bulleit.interceptors;

import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class RedirectHandler {

//    public static class RedirectStrategy extends DefaultRedirectStrategy {
//
//        public final Deque<URI> history = new LinkedList<>();
//        private static final Logger logger
//                = Logger.getLogger(RedirectStrategy.class.getName());
//
//        public RedirectStrategy(URI uri) {
//            history.push(uri);
//        }
//
//        @Override
//        public HttpUriRequest getRedirect(
//                HttpRequest request,
//                HttpResponse response,
//                HttpContext context) throws ProtocolException {
//            HttpUriRequest redirect = super.getRedirect(request, response, context);
//            history.push(redirect.getURI());
//            return redirect;
//        }
//    }

//    public static Deque<URI> expand(String uri) {
//        try {
//            HttpHead head = new HttpHead(uri);
//            RedirectStrategy redirectStrategy = new RedirectStrategy(head.getURI());
//            DefaultHttpClient defaultClient = new DefaultHttpClient();
//            defaultClient.setRedirectStrategy(redirectStrategy);
//            defaultClient.execute(head);
//            return redirectStrategy.history;
//
//        } catch (IOException e) {
//            logger.log(Level.INFO, "unable to ");
//        }
//    }
}

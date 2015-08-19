package gov.ic.geoint.bulleit.interceptors;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class RequestRedirectProcessor implements HttpRequestInterceptor{
    
    
    
    private static final Logger logger = Logger.getLogger(RequestRedirectProcessor.class.getName());
    
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        
        try {
            RequestLine rl = request.getRequestLine();
            logger.log(Level.INFO, "requestLine: {0}", rl.getUri());

            for (Header h : request.getAllHeaders()) {
                String name = h.getName();
                String value = h.getValue();
                if (name != null && value != null) {
                    logger.log(Level.INFO, "header: {0} : {1}", new Object[]{name, value});
                }
                for (HeaderElement he : h.getElements()) {
                    String n = he.getName();
                    String v = he.getValue();
                    if (n != null && v != null) {
                        logger.log(Level.INFO, "headerelement: {0} : {1}");
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "unable to intercept outgoing request {0}", e);
        }
    }    
}

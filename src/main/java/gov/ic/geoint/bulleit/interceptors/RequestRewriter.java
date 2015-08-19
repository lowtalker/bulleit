package gov.ic.geoint.bulleit.interceptors;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class RequestRewriter implements HttpRequestInterceptor {

    private static final Logger logger = Logger.getLogger(RequestRewriter.class.getName());

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        logger.log(Level.FINE, "request rewriter");
    }
}

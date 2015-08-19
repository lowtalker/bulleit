package gov.ic.geoint.bulleit;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class ResponseRedirectProcessor implements HttpResponseInterceptor {

    private static final Logger logger = Logger.getLogger(ResponseRedirectProcessor.class.getName());

    public ResponseRedirectProcessor() {
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {

        Integer responseCode = response.getStatusLine().getStatusCode();

        if (responseCode >= 300 && responseCode < 400) {
            logger.log(Level.INFO, "%%%%%%%%%%%%%% response code: REDIRECT {0} %%%%%%%%%%%%%%%%%%%", responseCode);
        }

//        try {
//            StatusLine sl = response.getStatusLine();
//            logger.log(Level.INFO, "***********responsestatuscode: {0} ********************", sl.getStatusCode());
//
//            if (sl.getStatusCode() == 302) {
//                logger.log(Level.WARNING, "$$$$$$$$$$$$$$$  REDIRECT  $$$$$$$$$$$$$$$$$$$$$$$$$$ {0}", sl.getStatusCode());
//                URL redirectUrl = new URL("https://atom.io/auth/github");
//                RedirectLocations rl = new RedirectLocations();
//                rl.add(redirectUrl);
//                context.setAttribute(HttpClientContext.REDIRECT_LOCATIONS, rl);
//                Header locationHeader = new BasicHeader("location", redirectUrl.toString());
//                response.addHeader(locationHeader);
//
//            }
//
////            HttpEntity entity = response.getEntity();
////            if (entity != null) {
////                logger.log(Level.INFO, "****************** response entity content length: {0} *********************", entity.getContentLength());
////            }
//        } catch (Exception e) {
//            logger.log(Level.WARNING, "unable to intercept outgoing request {0}", e);
//        }
    }

}

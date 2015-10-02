package gov.ic.geoint.bulleit;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import static org.apache.http.HttpHeaders.VIA;

/**
 *
 */
@Immutable
public class ResponseProxyHeaders implements HttpResponseInterceptor {

    ResponseProxyHeaders() {
        super();
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        
        if(!response.containsHeader(VIA)){
            response.addHeader(VIA, "");  //what goes here?            
        }
    }
}

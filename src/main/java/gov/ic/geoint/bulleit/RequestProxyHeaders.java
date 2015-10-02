package gov.ic.geoint.bulleit;

import java.io.IOException;
import org.apache.http.HttpException;
import static org.apache.http.HttpHeaders.VIA;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

/**
 *
 */
@Immutable
public class RequestProxyHeaders implements HttpRequestInterceptor {
    
    public RequestProxyHeaders(){
        super();
    }

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        
        if(!request.containsHeader(VIA)){
            request.setHeader(VIA, "");            
        }
        String uri = request.getRequestLine().getUri();
        
        request.setHeader("X-Forwarded-For", ""); //comma-delimited set of client, proxy, proxy1, proxy2, etc)
        request.setHeader("X-Forwarded-Proto", "https");
        
    }

}

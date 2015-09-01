package gov.ic.geoint.bulleit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
//gov.ic.geoint.bulleit.apache.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class RewriteAsyncByteConsumer extends AsyncByteConsumer<Integer> {

    @Override
    protected void onByteReceived(ByteBuffer bb, IOControl ioc) throws IOException {

        try {
            while (bb.hasRemaining()) {
                
                System.out.println("");
                
                
                String parsedString = new String(bb.array(), StandardCharsets.UTF_8);
                
                System.out.println("");
                
                
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Integer buildResult(HttpContext context) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

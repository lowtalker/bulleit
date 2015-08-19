package gov.ic.geoint.bulleit.apache;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.protocol.HttpContext;

/**
 * {@code HttpAsyncExpectationVerifier} defines a strategy to verify whether or
 * not an incoming HTTP request meets the target server's expectations.
 * <p>
 * A server that does not understand or is unable to comply with any of the
 * expectation values in the Expect field of a request MUST respond with
 * appropriate error status. The server MUST respond with a 417 (Expectation
 * Failed) status if any of the expectations cannot be met or, if there are
 * other problems with the request, some other 4xx status.
 *
 * @since 4.2
 */
public interface HttpAsyncExpectationVerifier {

    void verify(
            HttpAsyncExchange httpExchange,
            HttpContext context) throws HttpException, IOException;

}

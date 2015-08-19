package gov.ic.geoint.bulleit;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 *
 */
public class HttpReverseProxyTest {

    private static final Logger logger = Logger.getLogger(HttpReverseProxyTest.class.getName());

    public HttpReverseProxyTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of main method, of class HttpReverseProxy.
     */
    @Ignore
    @Test
    public void testMain() {
        System.out.println("testMain");
        try {
            HttpReverseProxy.main(null);
            assertTrue(true);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "testMain failed {0}", e);
            assertTrue(false);
        }
    }

    @Ignore
    @Test
    public void testGetMain() {
        logger.log(Level.INFO, "testGetMain");
        try {
            HttpReverseProxy.main(null);
            HttpGet getRequest = new HttpGet("http://localhost:8080/test");

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = null;

            response = client.execute(getRequest);
            assertNotNull(response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "testGetMain failed {0}", e);
            assertTrue(false);
        }

    }

    @Ignore
    @Test
    public void testGetHttpsMain() {
        logger.log(Level.INFO, "testGetHttpsMain");
        try {
            HttpReverseProxy.main(null);
            HttpGet getRequest = new HttpGet("https://localhost:8989/stest");

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = null;
            response = client.execute(getRequest);
            assertNotNull(response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "testGetHttpsMain failed {0}", e);
            assertTrue(false);
        }
    }

    @Ignore
    @Test
    public void testGetNyt() {
        logger.log(Level.INFO, "testGetNyt");
        try {
            HttpReverseProxy.main(null);
            HttpGet getRequest = new HttpGet("http://localhost:8080/nyt");

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = null;
            response = client.execute(getRequest);
            if (response != null) {
                logger.log(Level.INFO, "response status code {0}", response.getStatusLine().getStatusCode());
            }
            assertEquals(response, "200");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "testGetNyt failed {0}", e);
            assertTrue(false);
        }
    }

    @Ignore
    @Test
    public void testNyt() {
        try {
            HttpReverseProxy.main(null);
            HttpGet getRequest = new HttpGet("http://www.nytimes.com");

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(getRequest);
            if (response != null) {
                logger.log(Level.INFO, "response status code {0}", response.getStatusLine().getStatusCode());
            }
            assertNotNull(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "testNyt failed {0}", e);
            assertTrue(false);
        }
    }

}

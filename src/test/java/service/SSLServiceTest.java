package service;import com.getgo.cloudtower.service.SSLCertificateService;import junit.framework.TestCase;import org.slf4j.Logger;import org.slf4j.LoggerFactory;/** * Created by amirnashat on 9/15/16. */public class SSLServiceTest extends TestCase {		final Logger logger = LoggerFactory.getLogger( SSLServiceTest.class );		public void testProcessSSLService () {//		SSLCertificateService sslCertificateService = new SSLCertificateService();		sslCertificateService.process();			}}
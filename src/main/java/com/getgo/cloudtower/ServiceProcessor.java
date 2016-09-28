package com.getgo.cloudtower;import com.getgo.cloudtower.model.Account;import com.getgo.cloudtower.model.SSLCertificate;import com.getgo.cloudtower.model.Subnet;import com.getgo.cloudtower.model.Usage;import com.getgo.cloudtower.service.AccountService;import com.getgo.cloudtower.service.SSLCertificateService;import com.getgo.cloudtower.service.SubnetService;import com.getgo.cloudtower.service.UsageService;import com.getgo.cloudtower.service.UsageService.Services;import freemarker.template.Configuration;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import spark.ModelAndView;import spark.Request;import spark.template.freemarker.FreeMarkerEngine;import java.util.*;import static spark.Spark.*;/** * Created by amirnashat on 7/29/16. */public class ServiceProcessor {	static final Logger logger = LoggerFactory.getLogger(ServiceProcessor.class);	public static void main (String[] args) {				spark.template.freemarker.FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();		Configuration freeMarkerConfiguration = new Configuration();		freeMarkerConfiguration.setTemplateLoader(new freemarker.cache.ClassTemplateLoader(ServiceProcessor.class, "/"));		freeMarkerEngine.setConfiguration(freeMarkerConfiguration);		before((request, response) -> {			boolean authenticated = true;			// ... check if authenticated			if (!authenticated) {				halt(401, "You are not welcome here");			}			logger.info( "url for app is <"+request.uri()+">" );					});		get( "/sslcertificates" , (request , response) -> {						SSLCertificateService sslSrvc = new SSLCertificateService();			Map<SSLCertificate,String> ssl_certificates = sslSrvc.retrieveWithAccountName();						Calendar cal = Calendar.getInstance();			cal.add( Calendar.DATE, -1 );			Date yesterday = cal.getTime();			cal.add( Calendar.DATE, +30 );			Date oneMonthFromToday = cal.getTime();						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put("ssl_certificates", ssl_certificates);				attributes.put("yesterday", yesterday);				attributes.put("oneMonthFromToday", oneMonthFromToday);				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/ssl_certificates.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});		get( "/subnets/audit" , (request , response) -> {			logger.info("retrieved incorrect subnets");			SubnetService subnetService = new SubnetService();			Map<Subnet,String> subnets = subnetService.verifySubnets(null);			logger.info("retrieved incorrectly configured subnets of size <"+subnets.size()+">");			if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put("subnets", subnets);				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/subnets_verification.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});		get( "/subnets" , (request , response) -> {			SubnetService subnetService = new SubnetService();			Map<Subnet,String> subnetsWithAccountNames = subnetService.retrieveSubnetsWithAccountName();			List<String> vpcs = subnetService.retrieveUniqueVPCs();						logger.info("retrieved list of subnets");			if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "subnets", subnetsWithAccountNames );				attributes.put( "vpcs" , vpcs);				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/subnets.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});				post( "/subnets/vpcs" , (request , response) -> {						String vpc_name = request.queryParams( "vpc" );			SubnetService subnetService = new SubnetService();			List<String> vpcs = subnetService.retrieveUniqueVPCs();						Map<Subnet,String> subnets;						if( vpc_name.equalsIgnoreCase( "all" ) ) {				subnets = subnetService.retrieveSubnetsWithAccountName();			}   else {				logger.info("retriving list of subnets for vpc <"+vpc_name+">");				subnets = subnetService.retrieveSubnetsWithVPC( vpc_name );			}						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "subnets", subnets );				attributes.put( "vpcs" , vpcs );				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/subnets.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});				get( "/admin" , (request , response) -> {						//list of services			List<String> usagesTypes = UsageService.retrieveServices();						//list of accounts			AccountService accountService = new AccountService();			List<Account> accounts = accountService.getAccounts();			Map<Integer,String> accountsMap = new HashMap<>();						for (Account account : accounts ) {				accountsMap.put( account.getId() , account.getName() );			}						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "accounts" , accountsMap );				attributes.put( "usagesTypes" , usagesTypes );				attributes.put( "defaultUsage" , Services.S3.toString() );				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/admin.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});			post( "/admin/usage/update" , (request , response) -> {						String usageParam = request.queryParams( "usageType" );			Integer accountParam = Integer.valueOf( request.queryParams( "account" ) );			Integer maxAllowedValueParam = Integer.valueOf( request.queryParams( "maxAllowed" ) );						Usage usageQuery = new Usage();			usageQuery.setAccountId( accountParam );			usageQuery.setUsageName( usageParam );						UsageService usageService = new UsageService();			Usage retrievedUsage = usageService.retrieveForAccountAndName( usageQuery );			retrievedUsage.setMaxAllowedValue( maxAllowedValueParam );			usageService.updateUsageMaxAllowedValueForService( retrievedUsage );						response.redirect( "/admin" );			return null;		});				get( "/usages" , (request , response) -> {						List<String> usagesTypes = UsageService.retrieveServices();						UsageService usageService = new UsageService();			List<Usage> usages = usageService.retrieve();						AccountService accountService = new AccountService();						Map<Usage,String> usagesMap = new HashMap<Usage,String>();						for ( Usage usage : usages ) {				String accountName = accountService.getAccountWithAccountId( usage.getAccountId() ).getName();				usagesMap.put( usage , accountName );			}						//list of accounts			List<Account> accounts = accountService.getAccounts();			Map<Integer,String> accountsMap = new HashMap<>();						for (Account account : accounts ) {				accountsMap.put( account.getId() , account.getName() );			}						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "usagesTypes" , usagesTypes );				attributes.put( "usages" , usagesMap );				attributes.put( "accounts" , accountsMap );				attributes.put( "defaultUsage" , Services.S3.toString() );				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/usages.ftl" ));				// return produce HTML			} else {				return null;				// return produce JSON			}		});				post( "/usages/:name" , (request , response) -> {						UsageService usageService = new UsageService();						String usageType = Services.EC2.toString(); //default for ec2 instances			String usageTypeRequest = request.params("name");						if ( usageTypeRequest != null ) {				usageType = usageTypeRequest;			}						// Retrieve the set of data for the specific usage type that is being requested			List usages = usageService.retrieveForUsageType( usageType );						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put("usages", usages);				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/usages.ftl" ));				// return produce HTML			} else {				return null;				// return produce JSON			}		});				after( (request , response) -> {			response.header("foo", "set by filter");		});			}	private static boolean shouldReturnHtml(Request request) {		String accept = request.headers("Accept");		return accept != null && accept.contains("text/html");	}	}
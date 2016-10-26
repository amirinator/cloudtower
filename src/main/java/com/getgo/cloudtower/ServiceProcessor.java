package com.getgo.cloudtower;import com.getgo.cloudtower.model.Account;import com.getgo.cloudtower.model.SSLCertificate;import com.getgo.cloudtower.model.Subnet;import com.getgo.cloudtower.model.Usage;import com.getgo.cloudtower.schedule.ProcessScheduler;import com.getgo.cloudtower.service.AccountService;import com.getgo.cloudtower.service.SSLCertificateService;import com.getgo.cloudtower.service.SubnetService;import com.getgo.cloudtower.service.UsageService;import com.getgo.cloudtower.service.UsageService.Services;import freemarker.template.Configuration;import org.quartz.*;import org.quartz.impl.StdSchedulerFactory;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import spark.ModelAndView;import spark.Request;import spark.Spark;import spark.template.freemarker.FreeMarkerEngine;import java.io.*;import java.util.Calendar;import java.util.*;import static spark.Spark.*;/** * Created by amirnashat on 7/29/16. */public class ServiceProcessor {	static final Logger logger = LoggerFactory.getLogger(ServiceProcessor.class);	public static void main (String[] args) {				//define this public folder for serving images, css and, js files		Spark.staticFileLocation("/public");				spark.template.freemarker.FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();		Configuration freeMarkerConfiguration = new Configuration();		freeMarkerConfiguration.setTemplateLoader(new freemarker.cache.ClassTemplateLoader(ServiceProcessor.class, "/"));		freeMarkerEngine.setConfiguration(freeMarkerConfiguration);				//initiate Quartz cron integration		initiateScheduler();								before((request, response) -> {			boolean authenticated = true;			// ... check if authenticated			if (!authenticated) {				halt(401, "You are not welcome here");			}		});				get( "/" , (request , response) -> {						response.redirect( "/sslcertificates" );			return null;					});				get( "/sslcertificates" , (request , response) -> {						SSLCertificateService sslSrvc = new SSLCertificateService();			Map<SSLCertificate,String> ssl_certificates = sslSrvc.retrieveWithAccountName();						Calendar cal = Calendar.getInstance();			cal.add( Calendar.DATE, -1 );			Date yesterday = cal.getTime();			cal.add( Calendar.DATE, +30 );			Date oneMonthFromToday = cal.getTime();						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put("ssl_certificates", ssl_certificates);				attributes.put("yesterday", yesterday);				attributes.put("oneMonthFromToday", oneMonthFromToday);				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/ssl_certificates.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});		get( "/subnets-audit" , (request , response) -> {			SubnetService subnetService = new SubnetService();			Map<Subnet,String> subnets = subnetService.verifySubnets(null);			if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put("subnets", subnets);				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/subnets_verification.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});		get( "/subnets" , (request , response) -> {			SubnetService subnetService = new SubnetService();			Map<Subnet,String> subnetsWithAccountNames = subnetService.retrieveSubnetsWithAccountName();			List<String> vpcs = subnetService.retrieveUniqueVPCs();						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "subnets", subnetsWithAccountNames );				attributes.put( "vpcs" , vpcs);				attributes.put( "vpc_selected" , "All");				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/subnets.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});				post( "/subnets" , (request , response) -> {						String vpc_name = request.queryParams( "vpc" );			SubnetService subnetService = new SubnetService();			List<String> vpcs = subnetService.retrieveUniqueVPCs();						Map<Subnet,String> subnets;						if( vpc_name.equalsIgnoreCase( "all" ) ) {				subnets = subnetService.retrieveSubnetsWithAccountName();			}   else {				subnets = subnetService.retrieveSubnetsWithVPC( vpc_name );			}						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "subnets", subnets );				attributes.put( "vpcs" , vpcs );				attributes.put( "vpc_selected" , vpc_name);				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/subnets.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});				get( "/admin" , (request , response) -> {						//list of services			List<String> usagesTypes = UsageService.retrieveServices();						//list of regions			List<String> awsRegions = UsageService.retrieveAWSRegions();						//list of accounts			AccountService accountService = new AccountService();			List<Account> accounts = accountService.getAccounts();			Map<Integer,String> accountsMap = new HashMap<>();						for (Account account : accounts ) {				accountsMap.put( account.getId() , account.getName() );			}						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "accounts" , accountsMap );				attributes.put( "usages_types" , usagesTypes );				attributes.put( "aws_regions" , awsRegions );				attributes.put( "default_usage" , Services.S3.toString() );				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/admin.ftl"));				// return produce HTML			} else {				return null;				// return produce JSON			}		});			post( "/admin/usage/update/:region" , (request , response) -> {			String usageParam = request.queryParams( "usage_type" );			Integer accountParam = Integer.valueOf( request.queryParams( "account" ) );			Integer maxAllowedValueParam = Integer.valueOf( request.queryParams( "max_allowed" ) );			String region = request.params( "region" );						if(region.equalsIgnoreCase( "srvc" )) {				region = null;			}						Usage usage = new Usage();			usage.setAccountId( accountParam );			usage.setUsageName( usageParam );			usage.setMaxAllowedValue( maxAllowedValueParam );			usage.setRegionName( region );			UsageService usageService = new UsageService();			usageService.updateUsageForService( usage );						response.redirect( "/admin" );			return null;		});				get( "/usages" , (request , response) -> {						List<String> usagesTypes = UsageService.retrieveServices();						UsageService usageService = new UsageService();			List<Usage> usages = usageService.retrieve();						AccountService accountService = new AccountService();						Map<Usage,String> usagesMap = new HashMap<Usage,String>();						for ( Usage usage : usages ) {				String accountName = accountService.getAccountWithAccountId( usage.getAccountId() ).getName();				usagesMap.put( usage , accountName );			}						//list of accounts			List<Account> accounts = accountService.getAccounts();			Map<Integer,String> accountsMap = new HashMap<>();						for (Account account : accounts ) {				accountsMap.put( account.getId() , account.getName() );			}						if (shouldReturnHtml(request)) {				response.status( 200 );				response.type( "text/html" );				Map < String, Object > attributes = new HashMap <>();				attributes.put( "usages_types", usagesTypes );				attributes.put( "usages", usagesMap );				attributes.put( "accounts", accountsMap );				attributes.put( "default_usage", "All" );				return freeMarkerEngine.render( new ModelAndView( attributes, "/spark/template/freemarker/usages.ftl" ) );				// return produce HTML			} else {				return null;			}		});				post( "/usages" , (request , response) -> {						String accountRequestedId = request.queryParams( "account" );						AccountService accountService = new AccountService();			//list of accounts			List<Account> accounts = accountService.getAccounts();			Map<Integer,String> accountsMap = new HashMap<>();						UsageService usageService = new UsageService();						String usageType = Services.EC2.toString(); //default for ec2 instances			String usageTypeRequest = request.queryParams("usage_choice");						if ( usageTypeRequest != null ) {				usageType = usageTypeRequest;			}						List<Usage> usages;						if ( accountRequestedId.equalsIgnoreCase( "all" ) && usageTypeRequest.equalsIgnoreCase( "all" )) {								usages = usageService.retrieve();							} else if ( !accountRequestedId.equalsIgnoreCase( "all" ) && usageTypeRequest.equalsIgnoreCase( "all" )) {								Integer accountId = Integer.valueOf( accountRequestedId );				usages = usageService.getUsagesForAccount(accountId);							} else if ( accountRequestedId.equalsIgnoreCase( "all" ) && !usageTypeRequest.equalsIgnoreCase( "all" )) {								usages = usageService.getUsagesForUsageName(usageType);							} else {								Integer accountId = Integer.valueOf( accountRequestedId );				// Retrieve the set of data for the specific usage type that is being requested				usages = usageService.getUsagesWithNameAndAccount( usageType, accountId );							}									Map<Usage,String> usagesMap = new HashMap<Usage,String>();						for ( Usage usage : usages ) {				String accountName = accountService.getAccountWithAccountId( usage.getAccountId() ).getName();				usagesMap.put( usage , accountName );			}									List<String> usagesTypes = UsageService.retrieveServices();						for (Account account : accounts ) {				accountsMap.put( account.getId() , account.getName() );			}						if (shouldReturnHtml(request)) {				response.status(200);				response.type("text/html");				Map<String, Object> attributes = new HashMap<>();				attributes.put( "usages_types", usagesTypes );				attributes.put( "usages", usagesMap );				attributes.put( "accounts", accountsMap );				attributes.put( "default_usage", usageTypeRequest );				return freeMarkerEngine.render(new ModelAndView(attributes, "/spark/template/freemarker/usages.ftl" ));				// return produce HTML			} else {				return null;				// return produce JSON			}		});				after( (request , response) -> {			response.header("foo", "set by filter");		});			}	private static boolean shouldReturnHtml(Request request) {		String accept = request.headers( "Accept" );		return accept != null && accept.contains("text/html");	}		private static boolean shouldReturnJSON(Request request) {		String accept = request.headers( "Accept" );		return accept != null && accept.contains( "application/json" );	}		private static void initiateScheduler() {						//default to hourly		String schedule = "0 * * * * ?";				try {						File configFile = new File("./config/config.properties");			FileReader reader = new FileReader(configFile);			Properties props = new Properties();			props.load(reader);			schedule = props.getProperty( "schedule" );			reader.close();					} catch (FileNotFoundException fileException) {						StringWriter writer = new StringWriter();			PrintWriter printWriter = new PrintWriter( writer );			fileException.printStackTrace( printWriter );			printWriter.flush();						String stackTrace = writer.toString();			logger.error("Could not locate the file config.properties for Cloudtower");			logger.error(stackTrace);					}  catch (IOException ioException) {						StringWriter writer = new StringWriter();			PrintWriter printWriter = new PrintWriter( writer );			ioException.printStackTrace( printWriter );			printWriter.flush();						String stackTrace = writer.toString();			logger.error("I/O exception reading the config.properties for Cloudtower");			logger.error(stackTrace);					}  catch (Exception exception) {						StringWriter writer = new StringWriter();			PrintWriter printWriter = new PrintWriter( writer );			exception.printStackTrace( printWriter );			printWriter.flush();						String stackTrace = writer.toString();			logger.error(stackTrace);					}				JobDetail job = JobBuilder				                .newJob(ProcessScheduler.class)				                .withIdentity("dummyJobName", "group1").build();				Trigger trigger = TriggerBuilder				.newTrigger()				.withIdentity( "dummyTriggerName" , "group1" )				.withSchedule( CronScheduleBuilder.cronSchedule( schedule ) )				.build();				try {			Scheduler scheduler = new StdSchedulerFactory().getScheduler();						scheduler.start();			scheduler.scheduleJob( job , trigger );		} catch ( Exception exception ) {						StringWriter writer = new StringWriter();			PrintWriter printWriter = new PrintWriter( writer );			exception.printStackTrace( printWriter );			printWriter.flush();						String stackTrace = writer.toString();			logger.error(stackTrace);		}	}		}
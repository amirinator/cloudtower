package service;import com.amazonaws.regions.Regions;import com.getgo.cloudtower.model.Usage;import com.getgo.cloudtower.service.UsageService;import junit.framework.TestCase;import org.slf4j.Logger;import org.slf4j.LoggerFactory;/** * Created by amirnashat on 9/7/16. */public class UsageServiceTest extends TestCase{	final Logger logger = LoggerFactory.getLogger(UsageServiceTest.class);		enum Services { S3 , EC2 , EBS , SUBNET , SSL };		public void testProcessEC2() {		UsageService usageService = new UsageService();		usageService.processUsageType(UsageService.Services.EC2.toString());			}	public void testProcessS3() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.S3.toString() );			}		public void testProcessCloudFormation() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.CLOUDFORMATION.toString() );			}		public void testProcessRDS() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.RDS.toString() );			}		public void testProcessECS() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.ECS.toString() );			}		public void testProcessSNS() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.SNS.toString() );			}		public void testProcessDynamoDB() {				Usage usage = new Usage();				usage.setUsageName( UsageService.Services.DYNAMO.toString() );		usage.setAccountId( new Integer(1) );		usage.setRegionName( Regions.AP_NORTHEAST_1.toString() );		usage.setMaxAllowedValue( new Integer(111) );		usage.setRunningValue( new Integer(34) );				UsageService srvc = new UsageService();		srvc.insertUsageForService( usage );				Usage retrievedUsage = srvc.retrieveUsage( usage );				assertEquals( retrievedUsage.getRegionName(),Regions.AP_NORTHEAST_1.toString() );				Integer usageId = retrievedUsage.getId();				retrievedUsage.setRunningValue( 69 );				srvc.updateUsageForService( retrievedUsage );				Usage updatedUsage = srvc.retrieveUsage( retrievedUsage );				assertEquals( retrievedUsage.getId(),updatedUsage.getId() );				assertEquals( 69,retrievedUsage.getRunningValue().intValue() );	}		}
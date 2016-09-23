package service;import com.getgo.cloudtower.service.UsageService;import junit.framework.TestCase;import org.slf4j.Logger;import org.slf4j.LoggerFactory;/** * Created by amirnashat on 9/7/16. */public class UsageServiceTest extends TestCase{	final Logger logger = LoggerFactory.getLogger(UsageServiceTest.class);		enum Services { S3 , EC2 , EBS , SUBNET , SSL };		public void testProcessEC2() {		UsageService usageService = new UsageService();		usageService.processUsageType(UsageService.Services.EC2.toString());	}		public void testProcessS3() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.S3.toString() );	}		public void testProcessCloudFormation() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.CLOUDFORMATION.toString() );	}		public void testProcessRDS() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.RDS.toString() );	}		public void testProcessECS() {				UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.ECS.toString() );	}		public void testProcessSNS() {		UsageService usageService = new UsageService();		usageService.processUsageType( UsageService.Services.SNS.toString() );	}}
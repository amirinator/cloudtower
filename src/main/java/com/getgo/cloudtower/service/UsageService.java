package com.getgo.cloudtower.service;import com.amazonaws.AmazonClientException;import com.amazonaws.auth.AWSCredentials;import com.amazonaws.auth.AWSCredentialsProvider;import com.amazonaws.auth.profile.ProfileCredentialsProvider;import com.amazonaws.regions.Region;import com.amazonaws.regions.Regions;import com.amazonaws.services.cloudformation.AmazonCloudFormation;import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;import com.amazonaws.services.cloudformation.model.ListStacksRequest;import com.amazonaws.services.cloudformation.model.StackStatus;import com.amazonaws.services.cloudformation.model.StackSummary;import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;import com.amazonaws.services.ec2.AmazonEC2;import com.amazonaws.services.ec2.AmazonEC2Client;import com.amazonaws.services.ec2.model.*;import com.amazonaws.services.ecs.AmazonECS;import com.amazonaws.services.ecs.AmazonECSClient;import com.amazonaws.services.ecs.model.Cluster;import com.amazonaws.services.elasticache.AmazonElastiCache;import com.amazonaws.services.elasticache.AmazonElastiCacheClient;import com.amazonaws.services.elasticache.model.CacheCluster;import com.amazonaws.services.elasticache.model.CacheNode;import com.amazonaws.services.lambda.AWSLambda;import com.amazonaws.services.lambda.AWSLambdaClient;import com.amazonaws.services.lambda.model.FunctionConfiguration;import com.amazonaws.services.rds.AmazonRDS;import com.amazonaws.services.rds.AmazonRDSClient;import com.amazonaws.services.rds.model.DBInstance;import com.amazonaws.services.route53.AmazonRoute53;import com.amazonaws.services.route53domains.AmazonRoute53Domains;import com.amazonaws.services.route53domains.AmazonRoute53DomainsClient;import com.amazonaws.services.route53domains.model.DomainSummary;import com.amazonaws.services.s3.AmazonS3;import com.amazonaws.services.s3.AmazonS3Client;import com.amazonaws.services.s3.model.Bucket;import com.amazonaws.services.sns.AmazonSNS;import com.amazonaws.services.sns.AmazonSNSClient;import com.amazonaws.services.sns.model.Topic;import com.getgo.cloudtower.dao.AccountDAO;import com.getgo.cloudtower.dao.UsageDAO;import com.getgo.cloudtower.model.Account;import com.getgo.cloudtower.model.Usage;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.io.PrintWriter;import java.io.StringWriter;import java.util.ArrayList;import java.util.Arrays;import java.util.List;/** * Created by amirnashat on 9/1/16. */public class UsageService implements AWSService {	private Logger logger = LoggerFactory.getLogger(UsageService.class);	public enum Services {				CLOUDFORMATION( AmazonCloudFormation.ENDPOINT_PREFIX ) ,		DYNAMO ( AmazonDynamoDB.ENDPOINT_PREFIX ) ,		ECS ( AmazonECS.ENDPOINT_PREFIX ) ,		EC2 ( AmazonEC2.ENDPOINT_PREFIX ) ,		ELASTICACHE ( AmazonElastiCache.ENDPOINT_PREFIX ) ,		LAMBDA ( AWSLambda.ENDPOINT_PREFIX ) ,		RDS ( AmazonRDS.ENDPOINT_PREFIX ) ,		ROUTE53 (AmazonRoute53.ENDPOINT_PREFIX ) ,		SNS ( AmazonSNS.ENDPOINT_PREFIX ) ,		S3 ( AmazonS3.ENDPOINT_PREFIX ) ;				private final String service;				private Services ( final String service ) {						this.service = service;				}				public String toString() {						return service;				}			};		public enum AWSRegions {				US_EAST_1( "us-east-1" ) ,		US_WEST_1( "us-west-1" ) ,		US_WEST_2( "us-west-2" ) ,		EU_WEST_1( "eu-west-1" ) ,		EU_CENTRAL_1( "eu-central-1" ) ,		AP_SOUTHEAST_1( "ap-southeast-1" ) ,		AP_NORTHEAST_1( "ap-northeast-1" ) ,		AP_SOUTHEAST_2( "ap-southeast-2" ) ,		AP_NORTHEAST_2( "ap-northeast-2" ) ,		AP_SOUTH_1( "ap-south-1" ) ,		SA_EAST_1( " sa-east-1" ) ,		NOT_APPLICABLE( "Not Applicable" );				private final String region;				private AWSRegions ( final String region ) {						this.region = region;				}				public String toString() {						return region;				}			};	@Override	public void process() {				processUsageType(Services.CLOUDFORMATION.toString());		processUsageType(Services.DYNAMO.toString());		processUsageType(Services.ECS.toString());		processUsageType(Services.EC2.toString());		processUsageType(Services.ELASTICACHE.toString());		processUsageType(Services.LAMBDA.toString());		processUsageType(Services.RDS.toString());		processUsageType(Services.ROUTE53.toString());		processUsageType(Services.SNS.toString());		processUsageType(Services.S3.toString());	}	public void processUsageType( String usageName ) {		List<Account> accounts = AccountDAO.getAccounts();		for (Account account: accounts) {			Integer accountId = account.getId();			/*	         * The ProfileCredentialsProvider will return your [default]	         * credential profile by reading from the credentials file located at	         * (~/.aws/credentials).	         */			AWSCredentialsProvider credentialsProvider = null;			AWSCredentials credentials = null;			try {				credentialsProvider = new ProfileCredentialsProvider( account.getName() );				credentials = new ProfileCredentialsProvider(account.getName()).getCredentials();			} catch (Exception e) {				throw new AmazonClientException("Cannot load the credentials from the credential profiles file. " + "Please make sure that your credentials file is at the correct " + "location (~/.aws/credentials), and is in valid format.", e);			}			// Create the AmazonEC2Client object so we can call various APIs.			AmazonEC2 ec2 = new AmazonEC2Client(credentials);			Region usEast1 = Region.getRegion(Regions.US_EAST_1);			ec2.setRegion(usEast1);			List<com.amazonaws.services.ec2.model.Region> regions = ec2.describeRegions().getRegions();			for ( com.amazonaws.services.ec2.model.Region region : regions ) {								try {					if ( usageName.equalsIgnoreCase(Services.EC2.toString() ) ) {												if ( isServiceSupportedForRegion( AmazonEC2.ENDPOINT_PREFIX, region.getRegionName() ) ) {														AmazonEC2 ec2RegionClient = Region.getRegion( Regions.fromName( region.getRegionName() ) ).createClient( AmazonEC2Client.class , credentialsProvider , null );							ec2RegionClient.setEndpoint( region.getEndpoint() );														Filter filter = new Filter();							filter.setName("instance-state-code");							filter.setValues(Arrays.asList("16", "80"));														DescribeInstancesRequest ec2InstanceRequest = new DescribeInstancesRequest();							ec2InstanceRequest.setFilters(Arrays.asList(filter));														DescribeInstancesResult ec2Instances = ec2RegionClient.describeInstances(ec2InstanceRequest);														Integer runningValue  = ec2Instances.getReservations().size();														List<AccountAttribute > ec2Attributes = ec2RegionClient.describeAccountAttributes().getAccountAttributes();														for (AccountAttribute ec2Attribute : ec2Attributes) {																if ( ec2Attribute.getAttributeName().equalsIgnoreCase("max-instances") ) {																		Integer maxRunningValue = null;									List<AccountAttributeValue > attributeValues = ec2Attribute.getAttributeValues();																		for (AccountAttributeValue attributeValue : attributeValues) {										maxRunningValue = Integer.valueOf(attributeValue.getAttributeValue());									}																		Usage usagePOJO = new Usage();									usagePOJO.setAccountId(accountId);									usagePOJO.setRegionName(region.getRegionName());									usagePOJO.setUsageName(Services.EC2.toString());									usagePOJO.setRunningValue(runningValue);									usagePOJO.setMaxAllowedValue(maxRunningValue);																											if( !doesUsageExist( usagePOJO ) ) {																				insertUsageForService( usagePOJO );																			} else {																				Usage retrievedUsage = retrieveUsage( usagePOJO );										usagePOJO.setId( retrievedUsage.getId() );										updateUsageForService( usagePOJO );																		}								}							}						}											} else if ( usageName.equalsIgnoreCase( Services.SNS.toString() ) ) {												AmazonSNS amazonSNS = new AmazonSNSClient( credentials );						List<Topic > topics = amazonSNS.listTopics().getTopics();												Usage usagePOJO = new Usage();						usagePOJO.setRunningValue( topics.size() );						usagePOJO.setAccountId( accountId );						usagePOJO.setRegionName( AWSRegions.NOT_APPLICABLE.toString() );						usagePOJO.setUsageName( Services.SNS.toString() );												if( !doesUsageExist( usagePOJO ) ) {														insertUsageForService( usagePOJO );												} else {														Usage retrievedUsage = retrieveUsage( usagePOJO );							usagePOJO.setId( retrievedUsage.getId() );							updateUsageForService( usagePOJO );												}												//SNS service is only specific to an account and not a region so we can break out of 'region' loop						break;										} else if ( usageName.equalsIgnoreCase( Services.ECS.toString() ) ) {												if ( isServiceSupportedForRegion( AmazonECS.ENDPOINT_PREFIX, region.getRegionName() ) ) {														AmazonECS ecs = Region.getRegion( Regions.fromName( region.getRegionName() ) ).createClient( AmazonECSClient.class, credentialsProvider, null );							List<Cluster> clusters = ecs.describeClusters().getClusters();														Usage usagePOJO = new Usage();							usagePOJO.setRunningValue( clusters.size() );							usagePOJO.setAccountId( accountId );							usagePOJO.setRegionName( region.getRegionName() );							usagePOJO.setUsageName( Services.ECS.toString() );														if ( !doesUsageExist( usagePOJO ) ) {														insertUsageForService( usagePOJO );															} else {																Usage retrievedUsage = retrieveUsage( usagePOJO );								usagePOJO.setId( retrievedUsage.getId() );								updateUsageForService( usagePOJO );															}													}											} else if ( usageName.equalsIgnoreCase( Services.LAMBDA.toString() ) ) {												AWSLambda awsLambda = new AWSLambdaClient( credentials );						List<FunctionConfiguration> functions = awsLambda.listFunctions().getFunctions();												Usage usagePOJO = new Usage();						usagePOJO.setRunningValue( functions.size() );						usagePOJO.setAccountId( accountId );						usagePOJO.setRegionName( AWSRegions.NOT_APPLICABLE.toString() );						usagePOJO.setUsageName( Services.LAMBDA.toString() );												if ( !doesUsageExist( usagePOJO ) ) {														insertUsageForService( usagePOJO );												} else {														Usage retrievedUsage = retrieveUsage( usagePOJO );							usagePOJO.setId( retrievedUsage.getId() );							updateUsageForService( usagePOJO );												}												//LAMBDA service is only specific to an account and not a region so we can break out of 'region' loop						break;											} else if ( usageName.equalsIgnoreCase( Services.ROUTE53.toString() ) ) {												AmazonRoute53Domains route53 = new AmazonRoute53DomainsClient( credentials );						List<DomainSummary> domainSummaries = route53.listDomains().getDomains();												Usage usagePOJO = new Usage();						usagePOJO.setRunningValue( domainSummaries.size() );						usagePOJO.setAccountId( accountId );						usagePOJO.setRegionName( AWSRegions.NOT_APPLICABLE.toString() );						usagePOJO.setUsageName( Services.ROUTE53.toString() );												if( !doesUsageExist( usagePOJO ) ) {														insertUsageForService( usagePOJO );													} else {														Usage retrievedUsage = retrieveUsage( usagePOJO );							usagePOJO.setId( retrievedUsage.getId() );							updateUsageForService( usagePOJO );													}												//Route53 service is only specific to an account and not a region so we can break out of 'region' loop						break;											} else if ( usageName.equalsIgnoreCase( Services.ELASTICACHE.toString() ) ) {												if ( isServiceSupportedForRegion( AmazonElastiCache.ENDPOINT_PREFIX, region.getRegionName() ) ) {														AmazonElastiCache amazonElastiCache = Region.getRegion( Regions.fromName( region.getRegionName() ) ).createClient( AmazonElastiCacheClient.class , credentialsProvider , null );							List<CacheCluster> cacheClusters = amazonElastiCache.describeCacheClusters().getCacheClusters();														UsageDAO usageDAO = new UsageDAO();														for ( CacheCluster cluster : cacheClusters ) {																List<CacheNode> cacheNodes = cluster.getCacheNodes();								Usage usagePOJO = new Usage();								usagePOJO.setAccountId( accountId );								usagePOJO.setRegionName( region.getRegionName() );								usagePOJO.setUsageName( Services.ELASTICACHE.toString() );								usagePOJO.setRunningValue( cacheNodes.size() );																if ( !doesUsageExist( usagePOJO ) ) {																		insertUsageForService( usagePOJO );																} else {																		Usage retrievedUsage = retrieveUsage( usagePOJO );									usagePOJO.setId( retrievedUsage.getId() );									updateUsageForService( usagePOJO );																	}															}													}											} else if ( usageName.equalsIgnoreCase( Services.RDS.toString() ) ) {												if ( isServiceSupportedForRegion( AmazonRDS.ENDPOINT_PREFIX, region.getRegionName() ) ) {														AmazonRDS rds = Region.getRegion( Regions.fromName( region.getRegionName() ) ).createClient( AmazonRDSClient.class, credentialsProvider, null );							List <DBInstance> dbInstances = rds.describeDBInstances().getDBInstances();														Usage usagePOJO = new Usage();							usagePOJO.setAccountId( accountId );							usagePOJO.setRegionName( region.getRegionName() );							usagePOJO.setUsageName( Services.RDS.toString() );							usagePOJO.setRunningValue( dbInstances.size() );														if ( !doesUsageExist( usagePOJO ) ) {																insertUsageForService( usagePOJO );															} else {																Usage retrievedUsage = retrieveUsage( usagePOJO );								usagePOJO.setId( retrievedUsage.getId() );								updateUsageForService( usagePOJO );														}													}											} else if ( usageName.equalsIgnoreCase( Services.DYNAMO.toString() ) ) {												if ( isServiceSupportedForRegion( AmazonDynamoDB.ENDPOINT_PREFIX, region.getRegionName() ) ) {														AmazonDynamoDB dynamoDB = Region.getRegion( Regions.fromName( region.getRegionName() ) ).createClient( AmazonDynamoDBClient.class, credentialsProvider, null );							List<String> tableNames = dynamoDB.listTables().getTableNames();														Usage usagePOJO = new Usage();							usagePOJO.setAccountId( accountId );							usagePOJO.setRegionName( region.getRegionName() );							usagePOJO.setUsageName( Services.DYNAMO.toString() );							usagePOJO.setRunningValue( tableNames.size() );														if (!doesUsageExist( usagePOJO )) {																insertUsageForService( usagePOJO );															} else {																Usage retrievedUsage = retrieveUsage( usagePOJO );								usagePOJO.setId( retrievedUsage.getId() );								updateUsageForService( usagePOJO );															}													}											} else if ( usageName.equalsIgnoreCase( Services.S3.toString() ) ) {												AmazonS3 s3 = new AmazonS3Client(credentials);						List<Bucket> buckets = s3.listBuckets();												Usage usagePOJO = new Usage();						usagePOJO.setAccountId( accountId );						usagePOJO.setUsageName( Services.S3.toString() );						usagePOJO.setRunningValue( buckets.size() );						usagePOJO.setRegionName( AWSRegions.NOT_APPLICABLE.toString() );												if ( !doesUsageExist( usagePOJO )) {														logger.error("Inserting S3 record for <"+usagePOJO.toString()+">\n\n");							insertUsageForService( usagePOJO );													} else {														logger.error("Updating S3 record for <"+usagePOJO.toString()+">\n\n");														Usage retrievedUsage = retrieveUsage( usagePOJO );							usagePOJO.setId( retrievedUsage.getId() );							updateUsageForService( usagePOJO );													}												//S3 service is only specific to an account and not a region so we can break out of 'region' loop						break;					} else if ( usageName.equalsIgnoreCase(Services.CLOUDFORMATION.toString() ) ) {												AmazonCloudFormation cloudFormation = new AmazonCloudFormationClient(credentials);												//list only active stacks						ListStacksRequest cloudFormationStacksRequest = new ListStacksRequest();						cloudFormationStacksRequest.setStackStatusFilters(								Arrays.asList(										StackStatus.CREATE_COMPLETE.toString() ,										StackStatus.UPDATE_COMPLETE.toString() ,										StackStatus.ROLLBACK_COMPLETE.toString() ,										StackStatus.DELETE_IN_PROGRESS.toString() ,										StackStatus.DELETE_FAILED.toString() ,										StackStatus.CREATE_FAILED.toString() ) );						List<StackSummary> cloudFormationStacks = cloudFormation.listStacks( cloudFormationStacksRequest ).getStackSummaries();												Usage usagePOJO = new Usage();						usagePOJO.setAccountId( accountId );						usagePOJO.setRegionName( AWSRegions.NOT_APPLICABLE.toString() );						usagePOJO.setUsageName( Services.CLOUDFORMATION.toString() );						usagePOJO.setRunningValue( cloudFormationStacks.size() );																		if ( !doesUsageExist( usagePOJO ) ) {														insertUsageForService( usagePOJO );												} else {														Usage retrievedUsage = retrieveUsage( usagePOJO );							usagePOJO.setId( retrievedUsage.getId() );							updateUsageForService( usagePOJO );												}												//CloudFormation is only specific to an account and not a region so we can break ouf of region loop						break;											}									} catch ( Exception exception ) {										StringWriter writer = new StringWriter();					PrintWriter printWriter = new PrintWriter( writer );					exception.printStackTrace( printWriter );					printWriter.flush();					String stackTrace = writer.toString();					logger.error(stackTrace);									}			}		}	}	@Override	public List<Usage> retrieve() {			UsageDAO usageDAO = new UsageDAO();		List<Usage> usages = usageDAO.getUsages();		return usages;		}		public List<Usage> getUsagesForAccount(Integer accountId) {				Usage usage = new Usage();		usage.setAccountId( accountId );		UsageDAO usageDAO = new UsageDAO();		List<Usage> usages = usageDAO.getUsagesForAccount( usage );		return usages;			}		public List<Usage> getUsagesForUsageName(String usageName) {				Usage usage = new Usage();		usage.setUsageName( usageName );		UsageDAO usageDAO = new UsageDAO();		List<Usage> usages = usageDAO.getUsagesForUsageName( usage );		return usages;			}			public List<Usage> getUsagesWithNameAndAccount ( String usageType, Integer account ) {				UsageDAO usageDAO = new UsageDAO();		Usage usage = new Usage();		usage.setUsageName( usageType );		usage.setAccountId( account );		List<Usage> usages = usageDAO.getUsageWithNameAndAccount( usage);		return usages;	}		public Usage retrieveUsage( Usage usage ) {				UsageDAO usageDAO = new UsageDAO();		Usage retrievedUsage;				if ( isServicePerAccount( usage.getUsageName() ) ) {							retrievedUsage = usageDAO.getUsageWithNameAndAccount( usage ).get( 0 );						} else {							retrievedUsage = usageDAO.getUsageWithNameAndAccountAndRegion( usage );						}				return retrievedUsage;			}		public boolean doesUsageExist( Usage usage ) {				UsageDAO usageDAO = new UsageDAO();		List<Usage> usages = usageDAO.getUsageWithNameAndAccount( usage );				if( isServicePerAccount( usage.getUsageName() ) ) {						if ( usages != null && usages.size() >  0) {								return true;							} else {								return false;							}					} else {						for ( Usage retrievedUsage : usages ) {								if (retrievedUsage.getUsageName().equalsIgnoreCase( usage.getUsageName() ) && retrievedUsage.getRegionName().equalsIgnoreCase( usage.getRegionName() ) ) {										return true;								}							}						return false;					}			}		public void updateUsageForService( Usage usage ) {				if ( doesUsageExist( usage )) {						Usage retrievedUsage = retrieveUsage( usage );			retrievedUsage.setMaxAllowedValue( usage.getMaxAllowedValue() );			UsageDAO usageDAO = new UsageDAO();			usageDAO.updateUsage( retrievedUsage );					} else {						insertUsageForService( usage );					}	}		public void insertUsageForService( Usage usage ) {				UsageDAO usageDAO = new UsageDAO();		if ( isServicePerAccount( usage.getUsageName() )) {			usage.setRegionName( AWSRegions.NOT_APPLICABLE.toString() );		}		usageDAO.insertUsage( usage );			}		/*	 * Utility method which determins if a region supports the AWS service	 */	private boolean isServiceSupportedForRegion( String serviceName, String regionName ) {				try {						if ( Region.getRegion( Regions.fromName( regionName ) ).isServiceSupported( serviceName ) ) {				return true;			}					} catch (IllegalArgumentException execption) {			return false;		}				return false;	}		/*	 * Utility method which determins which AWS service is per account or per region	 */		private boolean isServicePerAccount( String serviceName ) {			if ( serviceName.equalsIgnoreCase( Services.CLOUDFORMATION.toString() ) ||				     serviceName.equalsIgnoreCase( Services.LAMBDA.toString() ) ||				     serviceName.equalsIgnoreCase( Services.ROUTE53.toString() ) ||				     serviceName.equalsIgnoreCase( Services.SNS.toString() ) ||				     serviceName.equalsIgnoreCase( Services.S3.toString() ) ) {			return true;		}				return false;			}		public static final List<String> retrieveServices() {				//list of services		List<String> usagesList = new ArrayList <String>();		usagesList.add( Services.CLOUDFORMATION.toString() );		usagesList.add( Services.DYNAMO.toString() );		usagesList.add( Services.EC2.toString() );		usagesList.add( Services.ECS.toString() );		usagesList.add( Services.ELASTICACHE.toString() );		usagesList.add( Services.LAMBDA.toString() );		usagesList.add( Services.RDS.toString() );		usagesList.add( Services.ROUTE53.toString() );		usagesList.add( Services.S3.toString() );		usagesList.add( Services.SNS.toString() );		return usagesList;			}		public static final List<String> retrieveAWSRegions() {				//list of regions		List<String> awsRegionsList = new ArrayList <String>();		awsRegionsList.add( AWSRegions.AP_NORTHEAST_1.toString() );		awsRegionsList.add( AWSRegions.AP_NORTHEAST_2.toString() );		awsRegionsList.add( AWSRegions.AP_SOUTH_1.toString() );		awsRegionsList.add( AWSRegions.AP_SOUTHEAST_1.toString() );		awsRegionsList.add( AWSRegions.AP_SOUTHEAST_2.toString() );		awsRegionsList.add( AWSRegions.EU_CENTRAL_1.toString() );		awsRegionsList.add( AWSRegions.EU_WEST_1.toString() );		awsRegionsList.add( AWSRegions.SA_EAST_1.toString() );		awsRegionsList.add( AWSRegions.US_EAST_1.toString() );		awsRegionsList.add( AWSRegions.US_WEST_1.toString() );		awsRegionsList.add( AWSRegions.US_WEST_2.toString() );		awsRegionsList.add( AWSRegions.NOT_APPLICABLE.toString() );		return awsRegionsList;			}}
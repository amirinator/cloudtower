package com.getgo.cloudtower.service;import com.amazonaws.AmazonClientException;import com.amazonaws.auth.AWSCredentials;import com.amazonaws.auth.profile.ProfileCredentialsProvider;import com.amazonaws.regions.Region;import com.amazonaws.regions.Regions;import com.amazonaws.services.ec2.AmazonEC2;import com.amazonaws.services.ec2.AmazonEC2Client;import com.amazonaws.services.ec2.model.Tag;import com.amazonaws.services.ec2.model.Vpc;import com.getgo.cloudtower.dao.AccountDAO;import com.getgo.cloudtower.dao.SubnetDAO;import com.getgo.cloudtower.model.Account;import com.getgo.cloudtower.model.Subnet;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.io.PrintWriter;import java.io.StringWriter;import java.util.HashMap;import java.util.List;import java.util.Map;/** * Created by amirnashat on 8/18/16. */public class SubnetService implements AWSService  {	final Logger logger = LoggerFactory.getLogger(SubnetService.class);	@Override	public void process() {		List<Account> accounts = AccountDAO.getAccounts();		for (Account account : accounts) {			/*	         * The ProfileCredentialsProvider will return your [default]	         * credential profile by reading from the credentials file located at	         * (~/.aws/credentials).	         */			AWSCredentials credentials = null;			try {				credentials = new ProfileCredentialsProvider(account.getName()).getCredentials();			} catch (Exception e) {				throw new AmazonClientException("Cannot load the credentials from the credential profiles file. " + "Please make sure that your credentials file is at the correct " + "location (~/.aws/credentials), and is in valid format.", e);			}			// Create the AmazonEC2Client object so we can call various APIs.			AmazonEC2 ec2Client = new AmazonEC2Client(credentials);			Region usEast1 = Region.getRegion(Regions.US_EAST_1);			ec2Client.setRegion(usEast1);			List<com.amazonaws.services.ec2.model.Region> regions = ec2Client.describeRegions().getRegions();			for(com.amazonaws.services.ec2.model.Region region : regions) {				//logger.info("Processing subnets for account <"+ account.getName()+"> and region <"+region.getRegionName()+"> and endpoint <"+region.getEndpoint()+">\n\n");				AmazonEC2Client ec2RegionClient = new AmazonEC2Client(credentials);				ec2RegionClient.setEndpoint(region.getEndpoint());				List<Vpc> vpcs = ec2RegionClient.describeVpcs().getVpcs();				for (Vpc vpc : vpcs) {					String vpcId = vpc.getVpcId();					String vpcName = "N/A";					List<Tag> vpcTags = vpc.getTags();					for(Tag tag : vpcTags) {						if (tag.getKey().equalsIgnoreCase("name")) {							vpcName = tag.getValue();						}					}					//obtain subnets for VPC					List<com.amazonaws.services.ec2.model.Subnet> subnets = ec2RegionClient.describeSubnets().getSubnets();					for (com.amazonaws.services.ec2.model.Subnet subnet : subnets) {						String subnetId = subnet.getSubnetId();						String subnetName = "N/A";						List<Tag> subnetTags = subnet.getTags();						for(Tag tag : subnetTags) {							if(tag.getKey().equalsIgnoreCase("name")){								subnetName = tag.getValue();							}						}						//logger.info("VPC Name <"+vpcName+"> : Subnet <"+subnetName+"> \n ");						//logger.info("Subnet info: <"+subnet.toString()+">\n\n");						SubnetDAO subnetDAO = new SubnetDAO();						com.getgo.cloudtower.model.Subnet subnetPOJO = new com.getgo.cloudtower.model.Subnet();						subnetPOJO.setVpcId(vpcId);						subnetPOJO.setVpcName(vpcName);						subnetPOJO.setAccountId(account.getId());						subnetPOJO.setRegionName(region.getRegionName());						subnetPOJO.setAwsSubnetId(subnetId);						subnetPOJO.setCidrBlock(subnet.getCidrBlock());						subnetPOJO.setIpCount(subnet.getAvailableIpAddressCount());						subnetPOJO.setSubnetName(subnetName);						subnetPOJO.setAvailabilityZone(subnet.getAvailabilityZone());						com.getgo.cloudtower.model.Subnet retrievedSubnet = subnetDAO.getSubnetWithAWSSubnetId(subnetPOJO);						if(retrievedSubnet == null) {							subnetDAO.createSubnet(subnetPOJO);						} else {							subnetPOJO.setId( retrievedSubnet.getId() );							subnetDAO.updateSubnet(subnetPOJO);						}					}				}			}		}		SubnetDAO subnetDAO = new SubnetDAO();			}	@Override	public List<Subnet> retrieve() {		SubnetDAO dao = new SubnetDAO();		List<Subnet> subnets = dao.getSubnets();		return subnets;	}	public List<String> retrieveUniqueVPCs() {		SubnetDAO dao = new SubnetDAO();		List<String> vpcs = dao.getUniqueVPCs();		return vpcs;	}	public Map<Subnet,String> retrieveSubnetsWithAccountName() {		AccountService accountService = new AccountService();		List<Subnet> subnets = retrieve();		Map<Subnet,String> subnetWithAccountName = new HashMap<Subnet,String>();		for (Subnet subnet : subnets ) {			String accountName = accountService.getAccountWithAccountId(subnet.getAccountId()).getName();			subnetWithAccountName.put(subnet,accountName);		}		return subnetWithAccountName;	}	public Map<Subnet,String> verifySubnets(List<Subnet> subnets ) {		AccountService accountService = new AccountService();		Map<Subnet,String> incorrectSubnets = new HashMap<Subnet,String>();		try {			if(subnets == null || subnets.isEmpty()) {				subnets = retrieve();			}						for (Subnet subnet : subnets) {				String subnetName = subnet.getSubnetName();				String availabilityZone = subnet.getAvailabilityZone();				//logger.info("AZ <" + availabilityZone + "> for Subnet <" + subnet.toString() + ">");				String availabilityZoneTail = availabilityZone.split("-")[2];				String accountName = accountService.getAccountWithAccountId(subnet.getAccountId()).getName();				if(subnetName.contains("-")) {					String subnetNameTail = subnetName.split("-")[1];					if(!availabilityZoneTail.equalsIgnoreCase(subnetNameTail)) {						//logger.info("obtaining account name <"+accountName+">");						incorrectSubnets.put(subnet,accountName);					}				} else {					incorrectSubnets.put(subnet,accountName);				}			}		} catch(Exception e) {			StringWriter writer = new StringWriter();			PrintWriter printWriter = new PrintWriter( writer );			e.printStackTrace( printWriter );			printWriter.flush();			String stackTrace = writer.toString();			logger.info(stackTrace);		}		return incorrectSubnets;	}		public Map<Subnet,String> retrieveSubnetsWithVPC (String vpc) {				AccountService accountService = new AccountService();		SubnetDAO dao = new SubnetDAO();		List<Subnet> subnets = dao.getSubnetsWithVPC(vpc);				Map<Subnet,String> subnetWithAccountName = new HashMap<Subnet,String>();				for (Subnet subnet : subnets ) {						String accountName = accountService.getAccountWithAccountId(subnet.getAccountId()).getName();			subnetWithAccountName.put(subnet,accountName);		}				return subnetWithAccountName;	}}
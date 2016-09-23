package com.getgo.cloudtower.dao;import com.getgo.cloudtower.model.Subnet;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.sql2o.Connection;import java.util.List;/** * Created by amirnashat on 8/18/16. */public class SubnetDAO extends DefaultDAO {	final Logger logger = LoggerFactory.getLogger(SubnetDAO.class);	public List<Subnet> getSubnets() {		String selectSubnetsSQL = "SELECT id , " +				                          "account_id AS accountId , " +				                          "region_name as regionName , " +				                          "vpc_name AS vpcName , " +				                          "vpc_id AS vpcId , " +				                          "subnet_name AS subnetName , " +				                          "aws_subnet_id AS awsSubnetId , " +				                          "cidr_block as cidrBlock , " +										  "availability_zone as availabilityZone , " +				                          "ip_count AS ipCount " +				                          "FROM subnets";		List<Subnet> results = null;		try (Connection conn = connection.open()) {			logger.info("Query for all Subnets");			results =  conn.createQuery(selectSubnetsSQL)					       .executeAndFetch(Subnet.class);		} catch (Exception e) {			logger.error("Issue with retrieving ssl certificates",e);		}		return results;	}	public Subnet getSubnetWithAWSSubnetId(Subnet subnet) {		String selectSubnetSQL = "SELECT id , " +				                         "account_id AS accountId , " +				                         "region_name as regionName , " +				                         "vpc_name AS vpcName , " +				                         "vpc_id AS vpcId , " +				                         "subnet_name AS subnetName , " +				                         "aws_subnet_id AS awsSubnetId , " +				                         "cidr_block as cidrBlock , " +				                         "availability_zone as availabilityZone , " +				                         "ip_count AS ipCount " +				                         "FROM subnets " +				                         "WHERE aws_subnet_id = :aws_subnet_id";		try (Connection conn = connection.open()) {			return conn.createQuery(selectSubnetSQL)					       .addParameter("aws_subnet_id" , subnet.getAwsSubnetId())					       .executeAndFetchFirst(Subnet.class);		}	}	public void createSubnet(Subnet subnet) {		String insertSubnetSQL = "INSERT INTO subnets(" +				                         "aws_subnet_id , " +				                         "region_name , " +				                         "vpc_name , " +				                         "vpc_id , " +				                         "subnet_name , " +				                         "cidr_block , " +				                         "ip_count , " +										 "availability_zone , " +				                         "account_id) " + "VALUES (" +				                         ":aws_subnet_id , " +				                         ":region_name , " +				                         ":vpc_name , " +				                         ":vpc_id , " +				                         ":subnet_name , " +				                         ":cidr , " +				                         ":ip_count , " +										 ":availability_zone , " +				                         ":account_id)";		try(Connection conn = connection.open()) {			conn.createQuery(insertSubnetSQL)					.addParameter("account_id" , subnet.getAccountId())					.addParameter("region_name" , subnet.getRegionName())					.addParameter("vpc_name" , subnet.getVpcName())					.addParameter("vpc_id" , subnet.getVpcId())					.addParameter("subnet_name" , subnet.getSubnetName())					.addParameter("cidr" , subnet.getCidrBlock())					.addParameter("aws_subnet_id" , subnet.getAwsSubnetId())					.addParameter("availability_zone" , subnet.getAvailabilityZone())					.addParameter("ip_count" , subnet.getIpCount())					.executeUpdate();		}	}	public void updateSubnet(Subnet subnet) {		String updateSubnetSQL = "UPDATE subnets SET " +				                         "account_id = :account_id , " +				                         "region_name = :region_name , " +				                         "vpc_name = :vpc_name , " +				                         "vpc_id = :vpc_id , " +				                         "subnet_name = :subnet_name , " +				                         "cidr_block = :cidr_block , " +				                         "ip_count = :ip_count , " +				                         "availability_zone = :availability_zone , " +				                         "aws_subnet_id = :aws_subnet_id " +				                         "WHERE id = :id ";		try (Connection conn = connection.open()) {			conn.createQuery(updateSubnetSQL)					.addParameter("account_id" , subnet.getAccountId())					.addParameter("region_name" , subnet.getRegionName())					.addParameter("vpc_name" , subnet.getVpcName())					.addParameter("vpc_id" , subnet.getVpcId())					.addParameter("subnet_name" , subnet.getSubnetName())					.addParameter("cidr_block" , subnet.getCidrBlock())					.addParameter("ip_count" , subnet.getIpCount())					.addParameter("availability_zone" , subnet.getAvailabilityZone())					.addParameter("aws_subnet_id" , subnet.getAwsSubnetId())					.addParameter("id" , subnet.getId())					.executeUpdate();		}	}}
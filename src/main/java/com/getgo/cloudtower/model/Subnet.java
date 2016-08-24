package com.getgo.cloudtower.model;

/**
 * Created by amirnashat on 7/6/16.
 */
public class Subnet {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof com.getgo.cloudtower.model.Subnet)) return false;

        com.getgo.cloudtower.model.Subnet subnet = (com.getgo.cloudtower.model.Subnet) o;

        if (getId() != null ? !getId().equals(subnet.getId()) : subnet.getId() != null) return false;
        if (getAccountId() != null ? !getAccountId().equals(subnet.getAccountId()) : subnet.getAccountId() != null)
            return false;
        if (getRegionName() != null ? !getRegionName().equals(subnet.getRegionName()) : subnet.getRegionName() != null)
            return false;
        if (getVpcName() != null ? !getVpcName().equals(subnet.getVpcName()) : subnet.getVpcName() != null)
            return false;
        if (getVpcId() != null ? !getVpcId().equals(subnet.getVpcId()) : subnet.getVpcId() != null) return false;
        if (getSubnetName() != null ? !getSubnetName().equals(subnet.getSubnetName()) : subnet.getSubnetName() != null)
            return false;
        if (getAwsSubnetId() != null ? !getAwsSubnetId().equals(subnet.getAwsSubnetId()) : subnet.getAwsSubnetId() != null)
            return false;
        if (getCidrBlock() != null ? !getCidrBlock().equals(subnet.getCidrBlock()) : subnet.getCidrBlock() != null)
            return false;
        return (getIpCount() != null ? !getIpCount().equals(subnet.getIpCount()) : subnet.getIpCount() != null);
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getAccountId() != null ? getAccountId().hashCode() : 0);
        result = 31 * result + (getRegionName() != null ? getRegionName().hashCode() : 0);
        result = 31 * result + (getVpcName() != null ? getVpcName().hashCode() : 0);
        result = 31 * result + (getVpcId() != null ? getVpcId().hashCode() : 0);
        result = 31 * result + (getSubnetName() != null ? getSubnetName().hashCode() : 0);
        result = 31 * result + (getAwsSubnetId() != null ? getAwsSubnetId().hashCode() : 0);
        result = 31 * result + (getCidrBlock() != null ? getCidrBlock().hashCode() : 0);
        result = 31 * result + (getIpCount() != null ? getIpCount().hashCode() : 0);
        return result;
    }

    private Integer id;
    private Integer accountId;
    private String regionName;
    private String vpcName;
    private String vpcId;
    private String subnetName;
    private String awsSubnetId;
    private String cidrBlock;
    private Integer ipCount;

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getVpcName() {
        return vpcName;
    }

    public void setVpcName(String vpcName) {
        this.vpcName = vpcName;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getAwsSubnetId() {
        return awsSubnetId;
    }

    public void setAwsSubnetId(String awsSubnetId) {
        this.awsSubnetId = awsSubnetId;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    public Integer getIpCount() {
        return ipCount;
    }

    public void setIpCount(Integer ipCount) {
        this.ipCount = ipCount;
    }

    public Integer getAccountId() {

        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}

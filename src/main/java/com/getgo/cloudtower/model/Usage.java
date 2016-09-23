package com.getgo.cloudtower.model;/** * Created by amirnashat on 9/2/16. */public class Usage {	private Integer id;	private Integer accountId;	private String usageName;	private String regionName;	private Integer runningValue;	private Integer maxAllowedValue;	public Integer getId() {		return id;	}	public void setId(Integer id) {		this.id = id;	}	public Integer getAccountId() {		return accountId;	}	public void setAccountId(Integer accountId) {		this.accountId = accountId;	}	public String getUsageName() {		return usageName;	}	public void setUsageName(String usageName) {		this.usageName = usageName;	}	public String getRegionName() {		return regionName;	}	public void setRegionName(String regionName) {		this.regionName = regionName;	}	public Integer getRunningValue() {		return runningValue;	}	public void setRunningValue(Integer runningValue) {		this.runningValue = runningValue;	}	public Integer getMaxAllowedValue() {		return maxAllowedValue;	}	public void setMaxAllowedValue(Integer maxAllowedValue) {		this.maxAllowedValue = maxAllowedValue;	}	@Override	public String toString() {		return "Usage{" + "id=" + id + ", accountId=" + accountId + ", usageName='" + usageName + '\'' + ", regionName='" + regionName + '\'' + ", runningValue=" + runningValue + ", maxAllowedValue=" + maxAllowedValue + '}';	}	@Override	public boolean equals(Object o) {		if (this == o) return true;		if (!(o instanceof Usage)) return false;		Usage usage = (Usage) o;		if (getId() != null ? !getId().equals(usage.getId()) : usage.getId() != null) return false;		if (getAccountId() != null ? !getAccountId().equals(usage.getAccountId()) : usage.getAccountId() != null)			return false;		if (getUsageName() != null ? !getUsageName().equals(usage.getUsageName()) : usage.getUsageName() != null)			return false;		if (getRegionName() != null ? !getRegionName().equals(usage.getRegionName()) : usage.getRegionName() != null)			return false;		if (getRunningValue() != null ? !getRunningValue().equals(usage.getRunningValue()) : usage.getRunningValue() != null)			return false;		return getMaxAllowedValue() != null ? getMaxAllowedValue().equals(usage.getMaxAllowedValue()) : usage.getMaxAllowedValue() == null;	}	@Override	public int hashCode() {		int result = getId() != null ? getId().hashCode() : 0;		result = 31 * result + (getAccountId() != null ? getAccountId().hashCode() : 0);		result = 31 * result + (getUsageName() != null ? getUsageName().hashCode() : 0);		result = 31 * result + (getRegionName() != null ? getRegionName().hashCode() : 0);		result = 31 * result + (getRunningValue() != null ? getRunningValue().hashCode() : 0);		result = 31 * result + (getMaxAllowedValue() != null ? getMaxAllowedValue().hashCode() : 0);		return result;	}}
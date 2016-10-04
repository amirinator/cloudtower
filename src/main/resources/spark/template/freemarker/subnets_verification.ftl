<html>
<#include "./navigation.ftl">
<body>
<div class="layout">
    <script type="application/javascript">
        $(document).ready(
         function() {
             $("table").tablesorter({
                sortList: [[1,1],[2,1],[3,1],[4,1],[5,1]]
             });
         });
    </script>
    <h2>AWS Subnets Audit</h2>
    <table class="tablesorter">
        <thead>
        <tr>
            <th>Subnet Name</th>
            <th>Subnet ID</th>
            <th>VPC Name</th>
            <th>Availability Zone</th>
            <th>Account</th>
        </tr>
        </thead>
        <tbody>
        <#list subnets as subnet,accountname>
        <tr>
            <#if subnet.subnetName??>
                <td>${subnet.subnetName}</td>
            </#if>
            <#if subnet.awsSubnetId??>
                <td>${subnet.awsSubnetId}</td>
            </#if>
            <#if subnet.vpcName??>
                <td>${subnet.vpcName}</td>
            </#if>
            <#if subnet.availabilityZone??>
                <td>${subnet.availabilityZone}</td>
            </#if>
            <#if accountname??>
                <td>${accountname}</td>
            </#if>
        </tr>
        </#list>
        </tbody>

    </table>
</div>
</body>
</html>
<html><#include "./navigation.ftl"><body><div class="layout"><script type="application/javascript">    function chgAction() {        var form = document.usage_form;        form.action = "/usages/" + form.usage_type.value;    }    $(document).ready(                    function() {                        $("table").tablesorter({                            sortList: [[1,1],[2,1],[3,1],[4,1],[5,1]]                        });                    });</script><h2>AWS Services Capacity</h2><table class="tablesorter">    <thead>    <tr>        <th>Account</th>        <th>Region</th>        <th>Service Name</th>        <th>Current Total</th>        <th>Max Allowed Value</th>    </tr>    </thead>    <tbody>    <form id="usage_form" name="usage_form" method="post" action="/" onsubmit="chgAction()">        <div class="form-group">            <label class"form-label"> Account </label>            <select class="form-label" name="account">                <option value="All">All</option>            <#list accounts as id , name >                <option value='${id}'>${name}</option>            </#list>            </select>            <label class"form-label"> Service Type </label>            <select class="form-label" name="usage_type">                <option value="All">All</option>            <#list usages_types as usage>                <option value='${(usage!" ")}'>${usage}</option>            </#list>            </select>            <input type="Submit" value="Submit">        </div>    </form>    <#list usages as usage,accountname>    <tr>        <#if accountname??>            <td>${accountname}</td>        <#else>            <td>N/A</td>        </#if>        <#if usage.regionName??>            <td>${usage.regionName}</td>        <#else>            <td>N/A</td>        </#if>        <#if usage.usageName??>            <td>${usage.usageName}</td>        <#else>            <td>N/A</td>        </#if>        <#if usage.runningValue??>            <td>${usage.runningValue}</td>        <#else>            <td>N/A</td>        </#if>        <#if usage.maxAllowedValue??>            <td>${usage.maxAllowedValue}</td>        <#else>            <td>N/A</td>        </#if>    </tr>    </#list>    </tbody></table>    </div></body></html>
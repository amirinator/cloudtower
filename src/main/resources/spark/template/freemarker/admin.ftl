<html><#include "./navigation.ftl"><body><script type="application/javascript">    function validateForm() {        var max_allowed = document.getElementById("max_allowed");        if(max_allowed == null || max_allowed == "") {            alert ("Please fill out a Service Limit value");            return false;        }    }</script><h2>Admin</h2><table>    <form method="post" name="admin_form" action="/admin/usage/update" onsubmit="return validateForm()">        <div class="form-group">            <label class"formLabel"> Account </label>            <select class="form-label" name="account">            <#list accounts as id , name >                <option value='${id}'>${name}</option>            </#list>            </select>            <label class"form-label"> Service Type </label>            <select class="form-label" name="usage_type">            <#list usages_types as usage>                <option value='${(usage!" ")}' <#if usage == default_usage> selected = "selected"</#if> >${usage}</option>            </#list>            </select>            <label class"formLabel"> Service Limit </label>            <input name="max_allowed" type="text" maxlength="10"/>            <input type="Submit" value="Submit" />        </div>    </form></table></body></html>
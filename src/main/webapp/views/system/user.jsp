<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="p" uri="/permission.tag" %>
<%
    request.setAttribute("lang", request.getLocale().getLanguage());
%>
<div class="content-header">
    <h2 class="content-title"><spring:message code="system.user.mgn"/></h2>
	<span class="layui-breadcrumb">
	  <a href="#!home"><spring:message code="home.page"/></a>
	  <a><cite><spring:message code="system.user.mgn"/></cite></a>
	</span>
</div>

<div>
    <div class="layui-form toolbar">
        <spring:message code="search"/>：
        <select id="searchKey">
            <option value="">-<spring:message code="please.select"/>-</option>
            <option value="username"><spring:message code="system.user.username"/></option>
            <option value="nickname"><spring:message code="system.user.nickname"/></option>
        </select>&emsp;

        <input id="searchValue" class="layui-input search-input" type="text"
               placeholder="<spring:message code="input.search.content"/>"/>&emsp;
        <p:hasPermission name="10011">
            <button id="searchBtn" class="layui-btn search-btn"><i class="layui-icon">&#xe615;</i><spring:message
                    code="search"/></button>
            &emsp;
        </p:hasPermission>
        <p:hasPermission name="10012">
            <button id="addBtn" class="layui-btn search-btn"><i class="layui-icon">&#xe654;</i><spring:message
                    code="add"/>
            </button>
        </p:hasPermission>
    </div>

    <table class="layui-table" id="table" lay-filter="table"></table>
</div>

<!-- 表单弹窗 -->
<script type="text/html" id="addModel">
    <form id="editForm" class="layui-form model-form" action="">
        <input name="userId" type="hidden"/>
        <div class="layui-form-item">
            <label class="layui-form-label"><spring:message code="system.user.username"/></label>
            <div class="layui-input-block">
                <input name="username"
                       placeholder="<spring:message code="please.input"/> <spring:message code="system.user.username"/>"
                       type="text" class="layui-input" maxlength="20" lay-verify="required" required/>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label"><spring:message code="system.user.nickname"/></label>
            <div class="layui-input-block">
                <input name="nickname"
                       placeholder="<spring:message code="please.input"/> <spring:message code="system.user.nickname"/>"
                       type="text" class="layui-input" maxlength="20" lay-verify="required" required/>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label"><spring:message code="system.user.phone"/></label>
            <div class="layui-input-block">
                <input name="phone"
                       placeholder="<spring:message code="please.input"/> <spring:message code="system.user.phone"/>"
                       type="text" class="layui-input" lay-verify="required" required/>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label"><spring:message code="sex"/></label>
            <div class="layui-input-block">
                <input type="radio" name="sex" id="sexMan" value="M" title='<spring:message code="male"/>' checked/>
                <input type="radio" name="sex" id="sexWoman" value="F" title='<spring:message code="female"/>'/>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label"><spring:message code="system.role"/></label>
            <div class="layui-input-block">
                <select id="role-select" name="roleId" lay-verify="required">
                </select>
            </div>
        </div>
        <div class="layui-form-item model-form-footer">
            <button class="layui-btn layui-btn-primary" type="button" id="btnCancel"><spring:message
                    code="cancel"/></button>
            <button class="layui-btn" lay-filter="btnSubmit" lay-submit><spring:message code="ok"/></button>
        </div>
    </form>
</script>
<!-- 表格操作列 -->
<script type="text/html" id="barTpl">
    <p:hasPermission name="10013">
        <a class="layui-btn layui-btn-primary layui-btn-xs" lay-event="edit"><spring:message
                code="edit"/></a>
    </p:hasPermission>
    <p:hasPermission name="10014">
        <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><spring:message code="delete"/></a>
    </p:hasPermission>
    <p:hasPermission name="10015">
        <a class="layui-btn layui-btn-xs" lay-event="reset"><spring:message code="reset.password"/></a>
    </p:hasPermission>
</script>
<!-- 表格状态列 -->
<script type="text/html" id="statusTpl">
    <input type="checkbox" value="{{d.userId}}" lay-filter="statusCB" lay-skin="switch"
           lay-text="<spring:message code='on'/>|<spring:message code='off'/>" {{ d.status== 1?'checked' : '' }}>
</script>
<script type="text/javascript" src="/static/js/system/user.js"></script>
<script>

    var i18n = new Object();
    i18n['username'] = '<spring:message code="system.user.username"/>';
    i18n['nickname'] = '<spring:message code="system.user.nickname"/>';
    i18n['phone'] = '<spring:message code="system.user.phone"/>';
    i18n['sex'] = '<spring:message code="sex"/>';
    i18n['roleName'] = '<spring:message code="system.role.name"/>';
    i18n['createTime'] = '<spring:message code="create.time"/>';
    i18n['status'] = '<spring:message code="status"/>';
    i18n['opt'] = '<spring:message code="opt"/>';
    i18n['add'] = '<spring:message code="add"/>';
    i18n['edit'] = '<spring:message code="edit"/>';
    i18n['confirm_delete'] = '<spring:message code="confirm.delete"/>';
    i18n['confirm_reset_pwd'] = '<spring:message code="confirm.reset.password"/>';
    i18n['pls_select'] = '<spring:message code="please.select"/>';

</script>
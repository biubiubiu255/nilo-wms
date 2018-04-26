<div class="content-header">
	<h2 class="content-title">权限管理</h2>
	<span class="layui-breadcrumb">
	  <a href="#!home">首页</a>
	  <a><cite>权限管理</cite></a>
	</span>
</div>

<div>
	<div class="layui-form toolbar">
		搜索：
		<select id="searchKey">
			<option value="">-请选择-</option>
			<option value="a.permission_id">ID</option>
			<option value="a.permission_name">名称</option>
			<option value="a.permission_value">权限值</option>
			<option value="b.permission_name">父级</option>
		</select>&emsp;
		<input id="searchValue" class="layui-input search-input" type="text" placeholder="输入搜索内容" />&emsp;
		<button id="searchBtn" class="layui-btn search-btn"><i class="layui-icon">&#xe615;</i>搜索</button>&emsp;
		<button id="addBtn" class="layui-btn search-btn"><i class="layui-icon">&#xe654;</i>添加</button>
	</div>
	
	<table class="layui-table" id="table" lay-filter="table"></table>
</div>

<!-- 表单弹窗 -->
<script type="text/html" id="addModel">
	<form id="editForm" class="layui-form model-form" action="">
		<input name="permissionId" type="hidden" />
		<div class="layui-form-item">
			<label class="layui-form-label">父级</label>
 			<div class="layui-input-block">
				<select id="parent-select" name="parentId" lay-verify="required">
				</select>   
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">名称</label>
 			<div class="layui-input-block">
				<input name="permissionName" placeholder="请输入名称" type="text" class="layui-input" maxlength="20" lay-verify="required" required />
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">权限值</label>
 			<div class="layui-input-block">
				<input name="permissionValue" placeholder="请输入权限值" type="text" class="layui-input" maxlength="20" lay-verify="required" required />
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">排序号</label>
 			<div class="layui-input-block">
				<input name="orderNumber" placeholder="请输入排序号" type="number" class="layui-input" lay-verify="number" required />
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">类型</label>
 			<div class="layui-input-block">
				<input type="radio" name="permissionType" id="type0" lay-filter="permissionType" value="0" title="菜单" checked />
				<input type="radio" name="permissionType" id="type1" lay-filter="permissionType" value="1" title="按钮" />
			</div>
		</div>
		<div class="layui-form-item model-form-footer">
			<button class="layui-btn layui-btn-primary" type="button" id="btnCancel">取消</button>
			<button class="layui-btn" lay-filter="btnSubmit" lay-submit>保存</button>
		</div>
	</form>
</script>
<!-- 表格操作列 -->
<script type="text/html" id="barTpl">
  <a class="layui-btn layui-btn-primary layui-btn-xs" lay-event="edit">编辑</a>
  <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del">删除</a>
</script>
<!-- 表格状态列 -->
<script type="text/html" id="statusTpl">
	<input type="checkbox" name="sex" lay-filter="statusCB" value="{{d.permissionId}}" lay-skin="switch" lay-text="正常|停用" {{ d.isDelete == 0 ? 'checked' : '' }}>
</script>
<!-- 目录select模板 -->
<script id="parentsSelect" type="text/html">
<option value="0">-请选择-</option>
{{#  layui.each(d, function(index, item){ }}
<option value="{{ item.permissionId }}">{{ item.permissionName }}</option>
{{#  }); }}
</script>
<script type="text/javascript" src="/static/js/system/permission.js"></script>

$(function () {

    refreshPermission();
    refreshI18n();
    //渲染表格
    layui.table.render({
        elem: '#table',
        url: '/servlet/user',
        where: {
            token: getToken()
        },
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'username', sort: true, title: getI18nAttr('system_user_username')},
            {field: 'nickname', sort: true, title: getI18nAttr('system_user_nickname')},
            {field: 'phone', sort: true, title: getI18nAttr('system_user_phone')},
            {field: 'sexDesc', sort: true, width: 80, title: getI18nAttr('sex')},
            {field: 'roleName', sort: true, title: getI18nAttr('system_role_name')},
            {field: 'userStatus', sort: true, templet: '#statusTpl', width: 80, title: getI18nAttr('status')},
            {
                field: 'createdTime', sort: true, templet: function (d) {
                return layui.util.toDateString(d.createdTime * 1000);
            }, title: getI18nAttr('create_time')
            },
            {align: 'center', toolbar: '#barTpl', minWidth: 180, title: getI18nAttr('opt')}
        ]]
    });



    //添加按钮点击事件
    $("#addBtn").click(function () {
        showEditModel(null);
    });

    //表单提交事件
    layui.form.on('submit(btnSubmit)', function (data) {
        data.field.token = getToken();
        data.field._method = $("#editForm").attr("method");
        layer.load(2);
        $.post("/servlet/user", data.field, function (data) {
            layer.closeAll('loading');
            if ("succ" == data.status) {
                layer.msg("SUCCESS", {icon: 1});
                layer.closeAll('page');
                layui.table.reload('table', {});
            } else {
                layer.msg(data.error, {icon: 2});
            }
        }, "JSON");
        return false;
    });

    //工具条点击事件
    layui.table.on('tool(table)', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') { //修改
            showEditModel(data);
        } else if (layEvent === 'del') { //删除
            doDelete(obj);
        } else if (layEvent === 'reset') { //重置密码
            doReSet(obj.data.userId);
        }
    });

    //监听状态开关操作
    layui.form.on('switch(statusCB)', function (obj) {
        updateStatus(obj);
    });

    //搜索按钮点击事件
    $("#searchBtn").click(function () {
        doSearch(table);
    });
});

//显示表单弹窗
function showEditModel(data) {
    layer.open({
        type: 1,
        title: data == null ? getI18nAttr('add') : getI18nAttr('edit'),
        area: '450px',
        offset: '120px',
        content: $("#addModel").html(),
        success: function(layero, index){
            refreshI18n(layero);
        }
    });

    $("#editForm")[0].reset();
    var selectItem = "";
    if (data != null) {
        $("#editForm input[name=_method]").val('PUT');

        $("#editForm input[name=userId]").val(data.userId);
        $("#editForm input[name=username]").val(data.username);
        $("#editForm input[name=nickname]").val(data.nickname);
        $("#editForm input[name=phone]").val(data.phone);
        $("#editForm input[name=username]").attr("disabled", true);
        selectItem = data.roleId;
        if ('M' == data.sex) {
            $("#sexMan").attr("checked", "checked");
            $("#sexWoman").removeAttr("checked");
        } else {
            $("#sexWoman").attr("checked", "checked");
            $("#sexMan").removeAttr("checked");
        }
        layui.form.render('radio');
    }
    $("#btnCancel").click(function () {
        layer.closeAll('page');
    });
    getRoles(selectItem);
}

//获取所有角色
var roles = null;
function getRoles(selectItem) {
    if (roles != null) {
        $("#role-select").empty();
        $("#role-select").prepend("<option value=''>" + getI18nAttr('pls_select') + "</option>");
        for (var i = 0; i < roles.length; i++) {
            $("#role-select").append("<option value='" + roles[i].roleId + "'>" + roles[i].roleName + "</option>");
        }
        $("#role-select").val(selectItem);
        layui.form.render('select');

    } else {
        var load = layer.load(2);
        $.get("/servlet/role", {
            token: getToken()
        }, function (data) {
            roles = data.data;
            getRoles(selectItem);
            layer.close(load);
        }, "JSON");
    }
}


//删除
function doDelete(obj) {
    layer.confirm(getI18nAttr('confirm_delete'), function (index) {
        layer.close(index);
        layer.load(2);
        $.ajax({
            url: "/servlet/user/" + obj.data.userId + "?token=" + getToken(),
            type: "DELETE",
            dataType: "JSON",
            success: function (data) {
                layer.closeAll('loading');
                if ("succ" == data.status) {
                    layer.msg("SUCCESS", {icon: 1});
                    obj.del();
                } else {
                    layer.msg(data.error, {icon: 2});
                }
            }
        });
    });
}

//更改状态
function updateStatus(obj) {
    var load = layer.load(2);
    var newStatus = obj.elem.checked ? 1 : 0;
    console.log(obj);
    $.post("/servlet/user/status", {
        userId: obj.elem.value,
        status: newStatus,
        _method: "PUT",
        token: getToken()
    }, function (data) {
        layer.close(load);
        if (data.status == 'succ') {
            layui.table.reload('table', {});
        } else {
            layer.msg(data.error, {icon: 2, time: 2000}, function () {
                layui.table.reload('table', {});
            });
        }
    });
}

//搜索
function doSearch(table) {
    var key = $("#searchKey").val();
    var value = $("#searchValue").val();
    if (value == '') {
        key = '';
    }
    layui.table.reload('table', {where: {searchKey: key, searchValue: value}});
}

//重置密码
function doReSet(userId) {
    layer.confirm(getI18nAttr('confirm_reset_pwd'), function (index) {
        layer.close(index);
        layer.load(2);
        $.post("/servlet/user/psw/" + userId, {
            token: getToken(),
            _method: "PUT"
        }, function (data) {
            layer.closeAll('loading');
            if (data.status == 'succ') {
                layer.msg("SUCCESS", {icon: 1});
            } else {
                layer.msg(data.error, {icon: 2});
            }
        }, "JSON");
    });
}
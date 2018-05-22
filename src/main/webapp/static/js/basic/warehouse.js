
$(function () {

    refreshPermission();
    refreshI18n();
    //渲染表格
    layui.table.render({
        size: 'sm',
        elem: '#table',
        url: '/servlet/warehouse',
        skin:'row',
        where: {
            token: getToken()
        },
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'name', sort: true, title: getI18nAttr('basic_warehouse_name')},
            {field: 'country', sort: true, title: getI18nAttr('country')},
            {field: 'space', sort: true, title: getI18nAttr('space')},
            {
                field: 'createdTime', sort: true, templet: function (d) {
                return layui.util.toDateString(d.createdTime * 1000);
            }, title: getI18nAttr('create_time')
            },
            {align: 'center', toolbar: '#barTpl', minWidth: 180, title: getI18nAttr('opt')}
        ]]
    });
    layui.table.render({
        size: 'sm',
        elem: '#table1',
        url: '/servlet/warehouse',
        skin: 'row',
        where: {
            token: getToken()
        },
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'name', sort: true, title: getI18nAttr('basic_warehouse_name')},
            {field: 'country', sort: true, title: getI18nAttr('country')},
            {field: 'space', sort: true, title: getI18nAttr('space')},
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
        layer.load(2);
        $.post("/servlet/warehouse", data.field, function (data) {
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

//显示修改、ADD弹窗
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
    $("#editForm").attr("_method", "POST");

    if (data != null) {
        $("#editForm input[name='id']").val(data.id);
        $("#editForm input[name='name']").val(data.name);
        $("#editForm input[name='space']").val(data.space);
        $("#editForm select[name='country']").val(data.country);
        $("#editForm input[name='_method']").val('PUT');
        layui.form.render('select');
    }
    $("#btnCancel").click(function () {
        layer.closeAll('page');
    });
}


//删除
function doDelete(obj) {
    layer.confirm(getI18nAttr('confirm_delete'), function (index) {
        layer.close(index);
        layer.load(2);
        $.ajax({
            url: "/servlet/warehouse/" + obj.data.id + "?token=" + getToken(),
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
    var value = $("#searchValue").val();
    layui.table.reload('table', {where: {name: value}});
}

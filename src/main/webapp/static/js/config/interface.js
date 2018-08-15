$(function () {

    refreshPermission();
    refreshI18n();
    //渲染表格
    layui.table.render({
        elem: '#table',
        url: '/servlet/interface',
        where: {
            token: getToken()
        },
        limit:30,
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'clientCode', sort: true, title: getI18nAttr('config_client_code')},
            {field: 'bizType', sort: true, title: getI18nAttr('config_interface_biz_type')},
            {field: 'method', sort: true, title: getI18nAttr('config_interface_method')},
            {field: 'url', sort: true, title: getI18nAttr('config_interface_url')},
            {field: 'requestMethod', sort: true, title: getI18nAttr('config_interface_request_method')},
            {field: 'status', sort: true,templet: '#statusTpl', title: getI18nAttr('status')},
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
        $.post("/servlet/interface", data.field, function (data) {
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
        }
    });

    //搜索按钮点击事件
    $("#searchBtn").click(function () {
        doSearch(table);
    });

    $("#refreshBtn").click(function () {
        refreshConfig();
    });

    //监听状态开关操作
    layui.form.on('switch(statusCB)', function (obj) {
        updateStatus(obj);
    });
});

//更改状态
function updateStatus(obj) {
    var load = layer.load(2);
    var newStatus = obj.elem.checked ? 1 : 0;
    $.post("/servlet/interface/status", {
        bizType: obj.elem.value,
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
    },"json");
}

//显示表单弹窗
function showEditModel(data) {
    layer.open({
        type: 1,
        title: data == null ? getI18nAttr('add') : getI18nAttr('edit'),
        area: '450px',
        offset: '120px',
        content: $("#addModel").html(),
        success: function (layero, index) {
            refreshI18n(layero);
        }
    });
    $("#editForm")[0].reset();
    if (data != null) {
        $("#editForm input[name=clientCode]").val(data.clientCode);
        $("#editForm input[name=bizType]").val(data.bizType);
        $("#editForm input[name=wmsKey]").val(data.wmsKey);
        $("#editForm input[name=method]").val(data.method);
        $("#editForm input[name=url]").val(data.url);
        $("#editForm input[name=requestMethod]").val(data.requestMethod);
        $("#editForm input[name=_method]").val("PUT");
        $("#editForm input[name=clientCode]").attr("disabled", true);
        $("#editForm input[name=bizType]").attr("disabled", true);

    }
    $("#btnCancel").click(function () {
        layer.closeAll('page');
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

//refresh config
function refreshConfig(obj) {
    layer.confirm(getI18nAttr('confirm_refresh'), function (index) {
        layer.close(index);
        layer.load(2);
        $.ajax({
            url: "/servlet/interface/refresh?token=" + getToken(),
            type: "POST",
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
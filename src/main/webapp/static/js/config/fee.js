$(function () {

    refreshPermission();
    refreshI18n();
    //渲染表格
    layui.table.render({
        elem: '#table',
        url: '/servlet/fee',
        where: {
            token: getToken()
        },
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'clientCode', sort: true, title: getI18nAttr('config_client_code')},
            {field: 'feeType', sort: true, title: getI18nAttr('config_fee_type')},
            {field: 'classTypeDesc', sort: true, title: getI18nAttr('config_fee_class')},
            {field: 'firstPrice', sort: true, title: getI18nAttr('config_fee_first_price')},
            {field: 'secondPrice', sort: true, title: getI18nAttr('config_fee_second_price')},
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
    //refresh
    $("#refreshBtn").click(function () {
        refreshConfig();
    });
    //表单提交事件
    layui.form.on('submit(btnSubmit)', function (data) {
        data.field.token = getToken();
        layer.load(2);
        $.post("/servlet/fee", data.field, function (data) {
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
        }else if (layEvent === 'del') { //删除
            doDelete(obj);
        }
    });

    //搜索按钮点击事件
    $("#searchBtn").click(function () {
        doSearch(table);
    });
});


//删除
function doDelete(obj) {
    layer.confirm(getI18nAttr('confirm_delete'), function (index) {
        layer.close(index);
        layer.load(2);
        $.ajax({
            url: "/servlet/fee/"+obj.data.feeType+"/" + obj.data.classType + "?token=" + getToken(),
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


//refresh config
function refreshConfig(obj) {
    layer.confirm(getI18nAttr('confirm_refresh'), function (index) {
        layer.close(index);
        layer.load(2);
        $.ajax({
            url: "/servlet/fee/refresh?token=" + getToken(),
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
        $("#editForm select[name=feeType]").val(data.feeType);
        $("#editForm select[name=classType]").val(data.classType);
        $("#editForm input[name=firstPrice]").val(data.firstPrice);
        $("#editForm input[name=secondPrice]").val(data.secondPrice);

        $("#editForm input[name=_method]").val("PUT");
        $("#editForm input[name=feeType]").attr("disabled", true);
        layui.form.render('select');
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
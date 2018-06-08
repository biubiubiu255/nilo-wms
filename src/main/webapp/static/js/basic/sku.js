$(function () {

    refreshPermission();
    refreshI18n();
    //渲染表格
    layui.table.render({
        elem: '#table',
        url: '/servlet/basic/sku',
        where: {
            token: getToken()
        },
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'customerCode', sort: true, title: getI18nAttr('basic_customer')},
            {field: 'sku', sort: true, title: getI18nAttr('basic_sku')},
            {field: 'desc_e', sort: true, title: getI18nAttr('basic_sku_desc_e')},
            {field: 'logisticType', sort: true, title: getI18nAttr('basic_sku_logistic_type')},
            {field: 'freightClass', sort: true, title: getI18nAttr('basic_sku_freight_type')},
            {field: 'price', sort: true, title: getI18nAttr('price')},
            {field: 'storeId', sort: true, title: getI18nAttr('store_id')},
            {field: 'storeName', sort: true, title: getI18nAttr('store_name')},
            {
                field: 'createdTime', sort: true, templet: function (d) {
                return layui.util.toDateString(d.createdTime * 1000);
            }, title: getI18nAttr('create_time')
            },
            {align: 'center', toolbar: '#barTpl', minWidth: 180, title: getI18nAttr('opt')}
        ]]
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


    //搜索按钮点击事件
    $("#searchBtn").click(function () {
        doSearch(table);
    });
});

function init_upload() {
    layui.use('upload', function () {
        var upload = layui.upload;

        //普通图片上传
        var uploadInst = upload.render({
            elem: '#sku_upload_image'
            , url: '/sku/upload/'
            , before: function (obj) {
                //预读本地文件示例，不支持ie8
                obj.preview(function (index, file, result) {
                    $('#sku_image_show').attr('src', result); //图片链接（base64）
                });
            }
            , done: function (res) {
                //如果上传失败
                if (res.code > 0) {
                    return layer.msg('上传失败');
                }
                //上传成功
            }
        });
    });
}

//删除
function doDelete(obj) {
    layer.confirm(getI18nAttr('confirm_delete'), function (index) {
        layer.close(index);
        layer.load(2);
        $.ajax({
            url: "/servlet/basic/sku/" + obj.data.sku + "?token=" + getToken(),
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

//搜索
function doSearch(table) {
    var value = $("#searchValue").val();
    layui.table.reload('table', {where: {name: value}});
}

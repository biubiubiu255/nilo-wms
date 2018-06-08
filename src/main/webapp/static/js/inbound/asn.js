
$(function () {

    refreshPermission();
    refreshI18n();
    
    var initDateRange=layui.util.toDateString(new Date(new Date()-7*24*60*60*1000),"yyyy-MM-dd") + " - " + layui.util.toDateString(new Date(),"yyyy-MM-dd");

    //日期范围
    layui.laydate.render({
        elem: '#date_range'
        , range: true
        , lang: getNavLanguage() == 'en' ? 'en' : 'cn'
        , value: initDateRange
    });

    //渲染表格
    layui.table.render({
        elem: '#table',
        url: '/servlet/inbound',
        where: {
            token: getToken(),
            dateRange: initDateRange
        },
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'referenceNo', sort: true, title: getI18nAttr('reference_no')},
            {field: 'asnType', sort: true, title: getI18nAttr('order_type')},
            {field: 'status', sort: true, title: getI18nAttr('status')},
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

        if (layEvent === 'put_away') { //修改
            put_away(data.referenceNo);
        }
    });


    //搜索按钮点击事件
    $("#searchBtn").click(function () {
        doSearch(table);
    });
});

//删除
function put_away(referenceNo) {
    layer.confirm(getI18nAttr('confirm'), function (index) {
        layer.close(index);
        layer.load(2);
        $.post("/servlet/inbound/put_away", {
            referenceNo: referenceNo,
            token: getToken()
        }, function (data) {
            layer.closeAll('loading');
            if (data.status == 'succ') {
                layer.msg("SUCCESS", {icon: 1});
                layui.table.reload('table', {});
            } else {
                layer.msg(data.error, {icon: 2});
            }
        },"JSON");
    });
}


//搜索
function doSearch(table) {
    var key = $("#searchKey").val();
    var value = $("#searchValue").val();
    if (value == '') {
        key = '';
    }
    var date_range = $("#date_range").val();
    if(!date_range){
        layer.msg("Pls select date.", {icon: 2});
        return;
    }
    layui.table.reload('table', {
        where: {
            searchKey: key,
            searchValue: value,
            dateRange: date_range
        }
    });
}

$(function () {

    refreshPermission();
    refreshI18n();

    var initDateRange = layui.util.toDateString(new Date(new Date() - 1 * 24 * 60 * 60 * 1000), "yyyy-MM-dd") + " - " + layui.util.toDateString(new Date(), "yyyy-MM-dd");

    //日期范围
    layui.laydate.render({
        elem: '#date_range'
        //, range: true
        , lang: getNavLanguage() == 'en' ? 'en' : 'cn'
        , value: initDateRange //initDateRange
    });

    //渲染表格
    layui.table.render({
        elem: '#table',
        url: '/servlet/report/cost_detail',
        method: 'POST',
        page:false,
        where: {
            token: getToken(),
            //fromDate: $("date_range").val()
            fromDate: layui.util.toDateString(new Date(new Date() - 1 * 24 * 60 * 60 * 1000), "yyyy-MM-dd"),
            toDate: layui.util.toDateString(new Date(), "yyyy-MM-dd")
        },
        cols: [[
            {type: 'numbers'},
            {field: 'order_sn', sort: true, title: getI18nAttr('oms_order')},
            {field: 'money_type', sort: true, title: getI18nAttr('report_cost_money_type')},
            {field: 'store_id', sort: true, title: getI18nAttr('report_cost_store_id')},
            {field: 'store_name', sort: true, title: getI18nAttr('report_cost_store_name')},
            {field: 'receivable_money', sort: true, title: getI18nAttr('report_cost_receivable_oney')},
            {field: 'createdTimeDesc', sort: true, title: getI18nAttr('received_time')}
        ]]
    });



    //搜索按钮点击事件
    $("#searchBtn").click(function () {
        doSearch(table);
    });

    //导出事件
    $(".btn-export").click(function () {
        exportFile();
    });

});


//搜索
function doSearch(table) {

    var date_range = $("#date_range").val();
    var timeArr = date_range.split(" - ");
    var fromDate = timeArr[0];
    var toDate   = timeArr[1];
    if (!fromDate || !toDate) {
        layer.msg("Pls select date.", {icon: 2});
        return;
    }
    layui.table.reload('table', {
        where: {
            //page: 1,
            //limit:10,
            fromDate: fromDate,
            toDate: toDate,
            omsOrder: $(".search-input").val()
        }
    });
}


function exportFile() {

    var layer = layui.layer;
    var date_range = $("#date_range").val();
    var timeArr = date_range.split(" - ");
    var fromDate = timeArr[0];
    var toDate   = timeArr[1];
    if (!fromDate || !toDate) {
        layer.msg("Pls select date.", {icon: 2});
        return;
    }

    if(document.cookie.indexOf("downStatus=complete") != -1){
        var exp = new Date();
        exp.setTime(exp.getTime() - 1);
        document.cookie= "downStatus=complete;expires="+exp.toGMTString();
    }
    layer.msg("waiting QAQ==");
    var index;
    setTimeout(function () { index=layer.load(); }, 2900);
    var url = '/servlet/report/export_cost_detail?' + $.param({token: getToken(),fromDate: fromDate,toDate: toDate, offset: 0, limit: 1000});
    document.location.href = url;

    var intervalIndex = setInterval(function () {
        if(document.cookie.indexOf("downStatus=complete") != -1){
            layer.close(index);
            clearInterval(intervalIndex);
        }
    }, 1000);

}
$(function () {

    refreshPermission();
    refreshI18n();

    var initDateRange = layui.util.toDateString(new Date(new Date() - 7 * 24 * 60 * 60 * 1000), "yyyy-MM-dd") + " - " + layui.util.toDateString(new Date(), "yyyy-MM-dd");



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
        url: '/servlet/inbound/detail',
        method: 'POST',
        where: {
            token: getToken(),
            fromDate: layui.util.toDateString(new Date(new Date() - 7 * 24 * 60 * 60 * 1000), "yyyy-MM-dd"),
            toDate: layui.util.toDateString(new Date(), "yyyy-MM-dd")
        },
        page: true,
        cols: [[
            {type: 'numbers'},
            {field: 'orderNo', sort: true, width:200, title: getI18nAttr('waybill')},
            {field: 'inboundType', sort: true,width:130, title: getI18nAttr('inbound_type')},
            {field: 'sku', sort: true,width:130, title: getI18nAttr('basic_sku')},
            {field: 'expectedQty', sort: true,width:130, title: getI18nAttr('expected_qty')},
            {field: 'receivedQty', sort: true,width:130, title: getI18nAttr('received_qty')},
            {field: 'putawayQty', sort: true,width:130, title: getI18nAttr('putaway_qty')},
            {field: 'inboundStatusDesc', sort: true,width:135, title: getI18nAttr('inbound_status')},
            {field: 'linestatusDesc', sort: true,width:130, title: getI18nAttr('line_status')},
            {field: 'inboundTime', sort: true, width:190, title: getI18nAttr('inbound_time')},
            {field: 'receivedTime', sort: true, width:190, title: getI18nAttr('received_time')},
            {field: 'putawayTime', sort: true, width:190, title: getI18nAttr('putaway_time')},







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

function ship(referenceNo) {
    layer.confirm(getI18nAttr('confirm'), function (index) {
        layer.close(index);
        layer.load(2);
        $.post("/servlet/inbound/ship", {
            token: getToken(),
            referenceNo:referenceNo
        }, function (data) {
            layer.closeAll('loading');
            if (data.status == 'succ') {
                layer.msg("SUCCESS", {icon: 1});
                layui.table.reload('table', {});
            } else {
                layer.msg(data.error, {icon: 2});
            }
        }, "JSON");
    });
}


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
            page: 1,
            limit:10,
            fromDate: fromDate,
            toDate: toDate,
            orderNo: $("#searchValue").val(),
            sku: $("#searchValueOfSku").val(),
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

    var index = layer.load();
    var url = '/servlet/inbound/export_detail?' + $.param({token: getToken(),fromDate: fromDate,toDate: toDate, offset: 0, limit: 1000});
    document.location.href = url;

    var intervalIndex = setInterval(function () {
        if(document.cookie.indexOf("downStatus=complete") != -1){
            layer.close(index);
            clearInterval(intervalIndex);
        }
    }, 1000);

}
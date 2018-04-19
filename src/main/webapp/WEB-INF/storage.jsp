<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>Storage Info</title>
    <link rel="stylesheet" href="/layui/css/layui.css">
</head>
<body>

<blockquote class="layui-elem-quote layui-text">
    sku storage info
</blockquote>

<div class="layui-form-item">
    <label class="layui-form-label" style="width:130px">Client Code</label>
    <div class="layui-input-inline">
        <input type="text" name="clientCode" id="clientCode" required lay-verify="required" autocomplete="off"
               placeholder="client code"
               class="layui-input">
    </div>
    <label class="layui-form-label" style="width:130px">Sku</label>
    <div class="layui-input-inline">
        <input type="text" name="sku" id="sku" required lay-verify="required" autocomplete="off" placeholder="sku"
               class="layui-input">
    </div>
    <button class="layui-btn search">Search</button>

</div>
<div class="layui-form-item">
    <label class="layui-form-label" style="width:130px">WMS Storage</label>
    <div class="layui-input-inline">
        <input type="text" name="wmsStorage" id="wmsStorage" disabled autocomplete="off" class="layui-input">
    </div>
    <label class="layui-form-label" style="width:130px">Redis Storage</label>
    <div class="layui-input-inline">
        <input type="tel" name="redisStorage" id="redisStorage" autocomplete="off" class="layui-input">
    </div>
    <label class="layui-form-label" style="width:130px">Lock Storage</label>
    <div class="layui-input-inline">
        <input type="text" name="lockStorage" id="lockStorage" autocomplete="off" class="layui-input">
    </div>
    <button class="layui-btn layui-btn-danger submitBtn">Submit</button>

</div>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 20px;">
    <legend>Lock List</legend>
</fieldset>
<div style="margin-left:160px; width: 1000px">
    <table class="layui-hide" id="lockTable">
    </table>
</div>
<script src="/layui/layui.js"></script>
<script src="/jQuery/jquery-2.2.3.min.js"></script>
<script>
    //一般直接写在一个js文件中

    layui.use(['layer', 'form', 'table'], function () {
        var layer, form, table;
        layer = layui.layer, form = layui.form, table = layui.table;


        $(".submitBtn").on("click", function () {

            if (!$("#clientCode").val()) {
                layer.msg("Client Code Empty", {icon: 2, time: 2000});
                return;
            }
            if (!$("#sku").val()) {
                layer.msg("Sku Empty", {icon: 2, time: 2000});
                return;
            }

            layer.confirm('Confirm Submit?', {
                        btn: ['OK', 'Cancel']
                        //按钮
                    }, function () {
                var load = layer.load(2);
                $.ajax({
                    url: "updateStorage.html",
                    data: {
                        clientCode: $("#clientCode").val(),
                        sku: $("#sku").val(),
                        redisStorage: $("#redisStorage").val(),
                        lockStorage: $("#lockStorage").val()
                    },
                    type: "POST",
                    dataType: "json",
                    success: function (data) {
                        if (data.status == 'succ') {
                            layer.msg('SUCCESS!', {icon: 1, time: 1000}, function () {
                                layer.closeAll();
                            });
                        } else {
                            //失败，提交表单成功后，释放hold，如果不释放hold，就变成了只能提交一次的表单
                            layer.msg(data.error, {icon: 2, time: 2000});
                        }
                    },
                    complete: function () {
                        layer.close(load);
                    }
                });
                    }
            );

        });

        table.render({
            elem: '#lockTable',
            url: 'lockList.html',
            cols: [[
                {field: 'orderNo', width: 350, title: 'Order No'},
                {field: 'time', width: 200, title: 'Lock Time'},
                {field: 'qty', width: 200, title: 'Lock Qty'}
            ]]
        });

        $(".search").on("click", function () {
            if (!$("#clientCode").val()) {
                layer.msg("Client Code Empty", {icon: 2, time: 2000});
                return;
            }
            if (!$("#sku").val()) {
                layer.msg("SKU Empty", {icon: 2, time: 2000});
                return;
            }

            var load = layer.load(2);
            $.ajax({
                url: "storageInfo.html",
                data: {
                    clientCode: $("#clientCode").val(),
                    sku: $("#sku").val()
                },
                type: "GET",
                dataType: "json",
                success: function (data) {
                    if (data.status == 'succ') {
                        var d = data.response;
                        $("#wmsStorage").val(d.wmsStorage);
                        $("#redisStorage").val(d.redisStorage);
                        $("#lockStorage").val(d.lockStorage);
                    } else {
                        //失败，提交表单成功后，释放hold，如果不释放hold，就变成了只能提交一次的表单
                        layer.msg(data.error, {icon: 2, time: 2000});
                    }
                },
                complete: function () {
                    layer.close(load);
                }
            });

            reloadTable();
        })

        var reloadTable = function (item) {
            table.reload("lockTable", {
                where: {
                    clientCode: $("#clientCode").val(),
                    sku: $("#sku").val()
                }
            });
        };
    });

</script>
</body>
</html>
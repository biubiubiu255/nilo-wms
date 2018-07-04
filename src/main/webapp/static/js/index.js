var refreshNav = true;

$(function () {

    initUserInfo();  //获取用户信息
    initNav();  //获取导航栏
    initPermission();  //获取用户权限
    loadI18n(getNavLanguage());
    refreshI18n();
    //路由注册
    Q.reg('home', function () {
        load('home');
    }).reg('system', function (path) {
        load('system/' + path);
    }).reg('basic', function (path) {
        load('basic/' + path);
    }).reg('inbound', function (path) {
        load('inbound/' + path);
    }).reg('outbound', function (path) {
        load('outbound/' + path);
    }).reg('inventory', function (path) {
        load('inventory/' + path);
    }).reg('report', function (path) {
        load('report/' + path);
    }).reg('log', function (path) {
        load('log/' + path);
    }).reg('config', function (path) {
        load('config/' + path);
    }).init({
        index: 'home'
    });

    //点击导航切换页面时不刷新导航,其他方式切换页面要刷新导航
    layui.element.on('nav(index-nav)', function (elem) {
        refreshNav = false;
        if (document.body.clientWidth <= 750) {
            switchNav(true);
        }
    });

    //修改密码表单提交事件
    layui.form.on('submit(pswSubmit)', function (data) {
        data.field.token = getToken();
        data.field._method = $("#pswForm").attr("method");
        layer.load(2);
        $.post("/servlet/user/psw", data.field, function (data) {
            if (data.status == 'succ') {
                layer.msg(data.msg, {icon: 1});
                setTimeout(function () {
                    loginOut();
                }, 1500);
            } else {
                layer.closeAll('loading');
                layer.msg(data.msg, {icon: 2});
            }
        }, "JSON");
        return false;
    });
});

//异步加载子页面
function load(path) {

    if (getCurrentUser() == null) {
        location.replace("/login.html");
    }

    if (refreshNav) {
        activeNav(path);
    }
    refreshNav = true;
    $("#main-content").load("views/" + path + ".html?token=" + getToken(), function () {
        layui.element.render('breadcrumb');
        layui.form.render('select');
    });
}

//获取左侧导航栏
function initNav() {
    var indexNavStr = sessionStorage.getItem("index-nav");
    var indexNav = JSON.parse(indexNavStr);
    if (indexNav == null) {
        $.get("/servlet/menu", {
            token: getToken()
        }, function (data) {
            if (data.status == 'succ') {
                sessionStorage.setItem("index-nav", JSON.stringify(data.menus));
                initNav();
            } else if (data.status == 'failed') {
                layer.msg(data.error, {icon: 2});
                setTimeout(function () {
                    loginOut();
                }, 1500);
            }
        }, "json");
    } else {
        layui.laytpl(sideNav.innerHTML).render(indexNav, function (html) {
            $("#index-nav").html(html);
            layui.element.render('nav', 'index-nav');
        });
    }
}

//获取用户权限
function initPermission() {
    var p = sessionStorage.getItem("permissions");
    if (p == null) {
        $.get("/servlet/permissions", {
            token: getToken()
        }, function (data) {
            if (data.status == 'succ') {
                var temp = new Array();
                var permissionData = data.permissions;
                for (var i = 0; i < permissionData.length; i++) {
                    temp.push(permissionData[i].permissionId);
                }
                sessionStorage.setItem("permissions", JSON.stringify(temp));
            } else if (data.status == 'failed') {
                layer.msg(data.error, {icon: 2});
                setTimeout(function () {
                    loginOut();
                }, 1500);
            }
        }, "json");
    }

}

//获取用户信息
function initUserInfo() {
    try {
        var user = getCurrentUser();
        $("#nickname").text(user.nickname);
    } catch (e) {
        console.log(e.message);
    }
}

//退出登录
function loginOut() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    sessionStorage.removeItem("index-nav");
    sessionStorage.removeItem("permissions");
    var load = layer.load(2);
    $.ajax({
        url: "/servlet/logout?token=" + getToken(),
        type: "POST",
        dataType: "JSON",
        success: function (data) {
            layer.close(load);
            location.href = "/login.html";
        }
    });
}

//个人信息
function myInfo() {
    var user = getCurrentUser();
    var content = '<ul class="site-dir" style="padding:25px 35px 8px 35px;"><li>'+getI18nAttr("system_user_username")+'：' + user.username + '</li><li>'+getI18nAttr("system_user_nickname")+'：' + user.nickname + '</li>';
    content += '<li>'+getI18nAttr("system_user_phone")+'：' + user.phone + '</li><li>'+getI18nAttr("sex")+'：' + user.sexDesc + '</li><li>'+getI18nAttr("system_role_name")+'：' + user.roleName + '</li></ul>';
    layer.open({
        type: 1,
        title: 'Info',
        area: '350px',
        offset: '120px',
        content: content,
        btn: ['关闭'],
        btnAlign: 'c'
    });
}

//显示表单弹窗
function updatePsw() {

    var title;
    layer.open({
        type: 1,
        title: title,
        area: '400px',
        offset: '120px',
        content: $("#pswModel").html()
    });
    $("#pswForm")[0].reset();
    $("#pswCancel").click(function () {
        layer.closeAll('page');
    });
}


package com.nilo.wms.web.controller.interceptor;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.exception.IllegalTokenException;
import com.nilo.wms.common.exception.NoPermissionException;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.common.util.TokenUtil;
import com.nilo.wms.service.platform.RedisUtil;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class SessionInterceptor extends HandlerInterceptorAdapter {

    private static final String ALLOW_URL = "/servlet/logout,/servlet/captcha/image,/servlet/login,/servlet/menu,/servlet/confirmASNData.html,/servlet/confirmTRASNData.html,/servlet/confirmCGSOData.html,/servlet/confirmSOData.html";
    private static final String AJAX_HEADER_KEY = "X-Requested-With";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        //排除登录请求
        if (uri.indexOf("api") != -1 || ALLOW_URL.indexOf(uri) != -1) {
            return true;
        }
        String token = request.getHeader("token");
        if (token == null) {
            token = request.getParameter("token");
        }
        boolean isAjax = request.getHeader(AJAX_HEADER_KEY) != null;
        if (StringUtil.isEmpty(token)) {
            if (isAjax) {
                response.setStatus(403);
                return false;
            }
            throw new IllegalTokenException();
        }
        String userId = TokenUtil.parseToken(token).getSubject();

        Method method = ((HandlerMethod) handler).getMethod();
        RequiresPermissions annotation = method
                .getAnnotation(RequiresPermissions.class);
        if (annotation == null) {
            return true;
        }
        String requiresPermissions = annotation.value();
        boolean has = RedisUtil.hasPermission(userId, requiresPermissions);
        if (!has) {
            throw new NoPermissionException();
        }
        Principal principal = RedisUtil.getPrincipal(userId);
        SessionLocal.setPrincipal(principal);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}
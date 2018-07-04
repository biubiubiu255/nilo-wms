package com.nilo.wms.web.controller.system;

import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.platform.parameter.UserParam;
import com.nilo.wms.dto.platform.system.User;
import com.nilo.wms.service.platform.UserService;
import com.nilo.wms.web.BaseController;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    @GetMapping
    @RequiresPermissions("10011")
    public String list(String searchValue, String searchKey) {

        UserParam parameter = new UserParam();
        if (StringUtil.isNotBlank(searchKey)) {
            BeanUtils.setProperty(parameter, searchKey, searchValue);
        }
        parameter.setPageInfo(getPage());
        return userService.queryUsers(parameter).toJson();
    }

    @PostMapping
    @RequiresPermissions("10012")
    public String add(User user) {

        userService.add(user);

        return ResultMap.success().toJson();
    }

    @PutMapping
    @RequiresPermissions("10013")
    public String update(User user) {

        userService.update(user);

        return ResultMap.success().toJson();
    }

    @PutMapping("/status")
    @RequiresPermissions("10013")
    public String updateStatus(String userId, int status) {

        User user = new User();
        user.setUserId(userId);
        user.setStatus(status);
        userService.update(user);

        return ResultMap.success().toJson();

    }

    @PutMapping("/psw")
    @RequiresPermissions("10015")
    public String psw(@PathVariable("userId") String userId) {

        User user = new User();
        user.setUserId(userId);
        user.setPassword(DigestUtils.md5Hex("12345678"));
        userService.update(user);
        return ResultMap.success().toJson();
    }

    @DeleteMapping("/{userId}")
    @RequiresPermissions("10014")
    public String delete(@PathVariable("userId") String userId) {

        userService.delete(userId);

        return ResultMap.success().toJson();
    }

}

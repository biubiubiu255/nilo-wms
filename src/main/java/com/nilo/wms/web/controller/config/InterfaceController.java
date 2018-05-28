package com.nilo.wms.web.controller.config;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dao.platform.InterfaceConfigDao;
import com.nilo.wms.dto.common.InterfaceConfig;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.platform.parameter.UserParam;
import com.nilo.wms.dto.platform.system.User;
import com.nilo.wms.web.BaseController;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interface")
public class InterfaceController extends BaseController {
    @Autowired
    private InterfaceConfigDao interfaceConfigDao;

    @GetMapping
    @RequiresPermissions("50021")
    public String list() {

        Principal principal = SessionLocal.getPrincipal();
        List<InterfaceConfig> list = interfaceConfigDao.queryByClientCode(principal.getClientCode());

        return toLayUIData(list.size(), list);
    }

    @PutMapping
    @RequiresPermissions("50022")
    public String update(InterfaceConfig interfaceConfig) {

        interfaceConfigDao.update(interfaceConfig);

        return ResultMap.success().toJson();
    }

    @PutMapping("/status")
    @RequiresPermissions("50023")
    public String updateStatus(InterfaceConfig interfaceConfig) {

        return ResultMap.success().toJson();

    }

}

package com.nilo.wms.web.controller.outbound;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.DateUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.platform.parameter.InboundParam;
import com.nilo.wms.dto.platform.parameter.OutboundParam;
import com.nilo.wms.dto.platform.parameter.UserParam;
import com.nilo.wms.dto.platform.system.User;
import com.nilo.wms.service.OutboundService;
import com.nilo.wms.service.platform.UserService;
import com.nilo.wms.web.BaseController;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outbound")
public class OutboundController extends BaseController {
    @Autowired
    private OutboundService outboundService;

    @GetMapping
    @RequiresPermissions("60011")
    public String list(String searchValue, String searchKey,String dateRange) {
        OutboundParam parameter = new OutboundParam();
        Principal principal = SessionLocal.getPrincipal();
        parameter.setClientCode(principal.getClientCode());
        if (StringUtil.isNotBlank(searchKey)) {
            BeanUtils.setProperty(parameter, searchKey, searchValue);
        }
        if (StringUtil.isNotBlank(dateRange)) {
            String[] date = dateRange.split(" - ");
            parameter.setStart_date(DateUtil.parse(date[0], "yyyy-MM-dd"));
            parameter.setEnd_date(DateUtil.parse(date[1], "yyyy-MM-dd") + 60 * 60 * 24 - 1);
        }
        parameter.setPageInfo(getPage());
        return outboundService.queryBy(parameter).toJson();
    }

    @PostMapping("/ship")
    @RequiresPermissions("60012")
    public String ship(String referenceNo) {
        outboundService.ship(referenceNo);
        return ResultMap.success().toJson();
    }
    @PostMapping("/cancel")
    @RequiresPermissions("60012")
    public String cancel(String referenceNo) {
        outboundService.cancel(referenceNo);
        return ResultMap.success().toJson();
    }

}

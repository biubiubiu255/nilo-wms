package com.nilo.wms.web.controller.inbound;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.DateUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.platform.parameter.InboundParam;
import com.nilo.wms.service.InboundService;
import com.nilo.wms.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inbound")
public class InboundController extends BaseController {
    @Autowired
    private InboundService inboundService;

    @GetMapping
    @RequiresPermissions("70011")
    public String list(String searchValue, String searchKey, String dateRange) {

        InboundParam parameter = new InboundParam();
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
        return inboundService.queryBy(parameter).toJson();
    }

    @PostMapping("/put_away")
    @RequiresPermissions("70012")
    public String put_away(String referenceNo) {

        inboundService.putAway(referenceNo);

        return ResultMap.success().toJson();
    }

}

package com.nilo.wms.web.controller.outbound;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.*;
import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.flux.InventoryLocation;
import com.nilo.wms.dto.inbound.OutboundDetailParam;
import com.nilo.wms.dto.inbound.ReportOutboundDetail;
import com.nilo.wms.dto.platform.parameter.InboundParam;
import com.nilo.wms.dto.platform.parameter.OutboundParam;
import com.nilo.wms.dto.platform.parameter.UserParam;
import com.nilo.wms.dto.platform.system.User;
import com.nilo.wms.service.OutboundService;
import com.nilo.wms.service.platform.UserService;
import com.nilo.wms.web.BaseController;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

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

    @PostMapping("/detail")
    @RequiresPermissions("60013")
    public String detail(OutboundDetailParam param) {
        Page page = getPage();
        page.setLimit(page.getLimit()+page.getOffset());
        List<ReportOutboundDetail> list = outboundService.findOutboundDetail(param, page);
        return toLayUIData(page.getCount(), list);
    }

    @ResponseBody
    @GetMapping("/export_detail")
    @RequiresPermissions("60015")
    public String exportDetail(OutboundDetailParam param, HttpServletResponse response) throws IOException {
        Page page = getPage();
        List<ReportOutboundDetail> list = outboundService.findOutboundDetail(param, page);

        HSSFWorkbook wb = new HSSFWorkbook();
        ExportExcel exportExcel = new ExportExcel(wb);

        exportExcel.fillData(list, ReportOutboundDetail.class);
        String path = System.getProperty("user.home") + File.separator + "temp" + File.separator;
        String fileName = IdWorker.getInstance().nextId() + "";
        response.reset();
        Cookie cookie = new Cookie("downStatus", "complete");
        cookie.setPath("/");
        response.addCookie(cookie);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
        ServletOutputStream outputStream = response.getOutputStream();
        exportExcel.exportWrite(outputStream);




        return null;
    }

}

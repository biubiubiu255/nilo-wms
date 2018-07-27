package com.nilo.wms.web.controller.inbound;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.*;
import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.inbound.InboundDetailParam;
import com.nilo.wms.dto.inbound.ReportInboundDetail;
import com.nilo.wms.dto.outbound.OutboundDetailParam;
import com.nilo.wms.dto.outbound.ReportOutboundDetail;
import com.nilo.wms.dto.platform.parameter.InboundParam;
import com.nilo.wms.service.InboundService;
import com.nilo.wms.web.BaseController;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

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

    @PostMapping("/detail")
    @RequiresPermissions("70013")
    public String detail(InboundDetailParam param) {
        Page page = getPage();
        page.setLimit(page.getLimit()+page.getOffset());
        List<ReportInboundDetail> list = inboundService.findInboundDetail(param, page);
        return toLayUIData(page.getCount(), list);
    }


    @ResponseBody
    @GetMapping("/export_detail")
    @RequiresPermissions("70014")
    public String exportDetail(InboundDetailParam param, HttpServletResponse response) throws IOException {
        Page page = getPage();
        List<ReportInboundDetail> list = inboundService.findInboundDetail(param, page);

        HSSFWorkbook wb = new HSSFWorkbook();
        ExportExcel exportExcel = new ExportExcel(wb);

        exportExcel.fillData(list, ReportInboundDetail.class);
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

package com.nilo.wms.web.controller.report;

import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.ExportExcel;
import com.nilo.wms.common.util.IdWorker;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dao.flux.FluxReportDao;
import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.flux.InventoryLocation;
import com.nilo.wms.dto.flux.StaffWork;
import com.nilo.wms.web.BaseController;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by admin on 2018/5/23.
 */
@RestController
@RequestMapping("/report")
public class FluxReportController extends BaseController {

    @Autowired
    private FluxReportDao fluxReportDao;

    @RequiresPermissions("3001")
    @GetMapping("/daily_pick")
    @ResponseBody
    public String daily_pick(String searchKey, String searchValue, String dateRange) {

        Map<String, String> param = new HashMap<>();
        param.put(searchKey, searchValue);
        if (StringUtil.isNotBlank(dateRange)) {
            String[] date = dateRange.split(" - ");
            param.put("start_date", date[0]);
            param.put("end_date", date[1]);
        }
        List<StaffWork> list = fluxReportDao.daily_pick(param);
        return toLayUIData(list.size(), list);
    }

    @RequiresPermissions("3002")
    @GetMapping("/daily_verify")
    @ResponseBody
    public String daily_verify(String searchKey, String searchValue, String dateRange) {

        Map<String, String> param = new HashMap<>();
        param.put(searchKey, searchValue);
        if (StringUtil.isNotBlank(dateRange)) {
            String[] date = dateRange.split(" - ");
            param.put("start_date", date[0]);
            param.put("end_date", date[1]);
        }
        List<StaffWork> list = fluxReportDao.daily_verify(param);
        return toLayUIData(list.size(), list);
    }

    @RequiresPermissions("3003")
    @GetMapping("/daily_dispatch")
    @ResponseBody
    public String daily_dispatch(String searchKey, String searchValue, String dateRange) {

        Map<String, String> param = new HashMap<>();
        param.put(searchKey, searchValue);
        if (StringUtil.isNotBlank(dateRange)) {
            String[] date = dateRange.split(" - ");
            param.put("start_date", date[0]);
            param.put("end_date", date[1]);
        }
        List<StaffWork> list = fluxReportDao.daily_dispatch(param);
        return toLayUIData(list.size(), list);
    }

    @RequiresPermissions("3004")
    @GetMapping("/inventory_location")
    @ResponseBody
    public String inventory_location(String fromLocation, String toLocation) {

        List<InventoryLocation> list = new ArrayList<>();
        if (StringUtil.isEmpty(fromLocation) || StringUtil.isEmpty(toLocation)) {
            return toLayUIData(0, list);
        }

        List<String> locationList = getLocationList(fromLocation, toLocation);
        Integer count = fluxReportDao.inventory_location_count(locationList);
        if (count != 0) {
            list = fluxReportDao.inventory_location(locationList);
        }
        return toLayUIData(count, list);
    }

    @RequiresPermissions("3004")
    @PostMapping("/inventory_location/build_excel")
    @ResponseBody
    public String build_excel(String fromLocation, String toLocation) {

        List<InventoryLocation> list = new ArrayList<>();
        if (StringUtil.isEmpty(fromLocation) || StringUtil.isEmpty(toLocation)) {
            return ResultMap.error().toJson();
        }
        List<String> locationList = getLocationList(fromLocation, toLocation);
        Integer count = fluxReportDao.inventory_location_count(locationList);
        if (count != 0) {
            list = fluxReportDao.inventory_location(locationList);
        }

        HSSFWorkbook wb = new HSSFWorkbook();
        ExportExcel exportExcel = new ExportExcel(wb);

        exportExcel.fillData(list, InventoryLocation.class);
        String path = System.getProperty("user.home") + File.separator + "temp" + File.separator;
        String fileName = IdWorker.getInstance().nextId() + "";
        exportExcel.export(path, fileName + ".xls");

        return ResultMap.success().put("fileName", fileName).toJson();
    }

    @RequiresPermissions("3004")
    @GetMapping("/inventory_location/export/{fileName}")
    @ResponseBody
    public String export(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        InputStream is = null;
        try {
            String path = System.getProperty("user.home") + File.separator + "temp" + File.separator;

            is = new FileInputStream(new File(path + fileName + ".xls"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 设置response参数，可以打开下载页面
        response.reset();
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
        ServletOutputStream out = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            out = response.getOutputStream();
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(out);
            byte[] buff = new byte[2048];
            int bytesRead;
            // Simple read/write loop.
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null)
                    bis.close();
                if (bos != null)
                    bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private List<String> getLocationList(String locationFrom, String locationTo) {
        List<String> locationList = new ArrayList<String>();

        if (StringUtil.equalsIgnoreCase(locationFrom, locationTo)) {
            locationList.add(locationFrom);
            return locationList;
        }

        int from = Integer
                .parseInt(locationFrom.substring(locationFrom.length() - 2, locationFrom.length()));
        int to = Integer
                .parseInt(locationTo.substring(locationTo.length() - 2, locationTo.length()));
        int count = to - from;

        //前缀
        String prefix = locationTo.substring(0, 10);
        for (int i = 0; i < count; i++) {

            String temp = "0" + (from + i);
            temp = temp.substring(temp.length() - 2, temp.length());
            locationList.add(prefix + "-" + 1 + "-" + temp);
            locationList.add(prefix + "-" + 2 + "-" + temp);
            locationList.add(prefix + "-" + 3 + "-" + temp);
            locationList.add(prefix + "-" + 4 + "-" + temp);
        }

        if (count == 0) {
            String temp = "0" + from;
            temp = temp.substring(temp.length() - 2, temp.length());
            locationList.add(prefix + "-" + 1 + "-" + temp);
            locationList.add(prefix + "-" + 2 + "-" + temp);
            locationList.add(prefix + "-" + 3 + "-" + temp);
            locationList.add(prefix + "-" + 4 + "-" + temp);
        }

        return locationList;
    }
}

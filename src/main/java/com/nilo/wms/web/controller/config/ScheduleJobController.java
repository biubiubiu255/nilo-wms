package com.nilo.wms.web.controller.config;

import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.dao.platform.ClientConfigDao;
import com.nilo.wms.dto.ScheduleJob;
import com.nilo.wms.dto.common.ClientConfig;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.service.ScheduleService;
import com.nilo.wms.service.platform.SystemService;
import com.nilo.wms.web.BaseController;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleJobController extends BaseController {
    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    @RequiresPermissions("50041")
    public String list() {
        List<ScheduleJob> list = scheduleService.listAll();
        return toLayUIData(list.size(), list);
    }

    @PutMapping("/status")
    @RequiresPermissions("50042")
    public String updateStatus(String jobName, Integer status) {
        if (status == 1) {
            scheduleService.start(jobName);
        } else {
            scheduleService.stop(jobName);
        }
        return ResultMap.success().toJson();
    }

    @PutMapping("/modify_corn")
    @RequiresPermissions("50042")
    public String modifyCorn(String jobName, String cornExpression) {

        try {
            CronExpression exp = new CronExpression(cornExpression);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Corn Expression Illegal.");
        }

        scheduleService.modifyCorn(jobName, cornExpression);
        return ResultMap.success().toJson();
    }

}

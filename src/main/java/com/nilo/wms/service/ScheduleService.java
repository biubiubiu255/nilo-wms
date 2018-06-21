package com.nilo.wms.service;


import com.nilo.wms.dto.ScheduleJob;

import java.util.List;

/**
 * Created by admin on 2018/6/7.
 */
public interface ScheduleService {

    List<ScheduleJob> listAll();

    void start(String jobName);

    void stop(String jobName);

    void modifyCorn(String jobName, String corn);

}

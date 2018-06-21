package com.nilo.wms.dao.platform;

import com.nilo.wms.common.BaseDao;
import com.nilo.wms.dto.ScheduleJob;
import com.nilo.wms.dto.fee.FeeConfig;
import com.nilo.wms.dto.platform.parameter.FeeConfigParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleJobDao extends BaseDao<Long, ScheduleJob> {

    List<ScheduleJob> listAll();

    ScheduleJob getByJobName(String jobName);

}

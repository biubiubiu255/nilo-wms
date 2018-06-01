/**
 * Kilimall Inc.
 * Copyright (c) 2015-2016 All Rights Reserved.
 */
package com.nilo.wms.dao.flux;

import com.nilo.wms.dto.flux.InventoryLocation;
import com.nilo.wms.dto.flux.StaffWork;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface FluxReportDao {

    List<StaffWork> daily_pick(Map<String, String> param);

    List<StaffWork> daily_verify(Map<String, String> param);

    List<StaffWork> daily_dispatch(Map<String, String> param);

    List<InventoryLocation> inventory_location(Map<String, String> param);

    Integer inventory_location_count(Map<String, String> param);

}

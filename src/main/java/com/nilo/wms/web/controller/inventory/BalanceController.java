package com.nilo.wms.web.controller.inventory;

import com.alibaba.fastjson.JSON;
import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dao.flux.SkuDao;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.platform.parameter.InventoryBalanceParam;
import com.nilo.wms.dto.platform.parameter.StorageParam;
import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/inventory/balance")
public class BalanceController extends BaseController {
    @Autowired
    private BasicDataService basicDataService;
    @Autowired
    private SkuDao skuDao;

    @GetMapping
    @RequiresPermissions("20011")
    public String list(String searchValue, String searchKey) {

        InventoryBalanceParam parameter = new InventoryBalanceParam();
        if (StringUtil.isNotBlank(searchKey)) {
            BeanUtils.setProperty(parameter, searchKey, searchValue);
        }
        parameter.setPageInfo(getPage());

        Principal principal = SessionLocal.getPrincipal();
        StorageParam param = new StorageParam();
        param.setCustomerId(principal.getCustomerId());
        param.setWarehouseId(principal.getWarehouseCode());
        param.setLimit(parameter.getLimit());
        param.setOffset(parameter.getOffset());
        param.setPage(parameter.getOffset() / parameter.getLimit() + 1);
        List<String> skuList = new ArrayList<>();
        if (StringUtil.isNotEmpty(parameter.getSku())) {
            String[] str = parameter.getSku().split(",");
            for (String s : str) {
                skuList.add(s.trim());
            }
        }
        param.setSku(skuList);

        List<String> supplierList = new ArrayList<>();
        if (StringUtil.isNotEmpty(parameter.getSupplierId())) {
            supplierList.add(parameter.getSupplierId());
        }
        param.setStoreId(supplierList);

        return JSON.toJSONString(basicDataService.queryStorageDetail(param));
    }

    @PostMapping
    @RequiresPermissions("20012")
    public String update(String sku, Integer cache_storage, Integer lock_storage, Integer safe_storage) {
        basicDataService.updateStorage(sku, cache_storage, lock_storage, safe_storage);
        return ResultMap.success().toJson();
    }

    @PostMapping("/sync/{sku}")
    @RequiresPermissions("20013")
    public String sync(@PathVariable("sku") String sku) {

        AssertUtil.isNotBlank(sku, CheckErrorCode.SKU_EMPTY);
        List<String> list = new ArrayList<>();
        list.add(sku);
        basicDataService.sync(list);
        return ResultMap.success().toJson();
    }

    @PostMapping("/sync/all")
    @RequiresPermissions("20014")
    public String syncAll() {
        Principal principal = SessionLocal.getPrincipal();
        basicDataService.syncStock(principal.getClientCode());
        return ResultMap.success().toJson();
    }

}

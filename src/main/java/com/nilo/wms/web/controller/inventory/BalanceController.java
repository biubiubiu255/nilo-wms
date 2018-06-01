package com.nilo.wms.web.controller.inventory;

import com.alibaba.fastjson.JSON;
import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dao.flux.FluxInventoryDao;
import com.nilo.wms.dto.common.ResultMap;
import com.nilo.wms.dto.platform.parameter.InventoryBalanceParam;
import com.nilo.wms.dto.platform.parameter.StorageParam;
import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.service.platform.RedisUtil;
import com.nilo.wms.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/inventory/balance")
public class BalanceController extends BaseController {
    @Autowired
    private BasicDataService basicDataService;

    @GetMapping
    @RequiresPermissions("20011")
    public String list(String searchValue, String searchKey) {

        List<String> skuList = new ArrayList<>();
        if (StringUtil.isEmpty(searchKey)) {
            return toLayUIData(0, skuList);
        }

        InventoryBalanceParam parameter = new InventoryBalanceParam();
        BeanUtils.setProperty(parameter, searchKey, searchValue);
        parameter.setPageInfo(getPage());

        Principal principal = SessionLocal.getPrincipal();
        StorageParam param = new StorageParam();
        param.setCustomerId(principal.getCustomerId());
        param.setWarehouseId(principal.getWarehouseCode());
        param.setLimit(parameter.getLimit());
        param.setOffset(parameter.getOffset());
        param.setPage(parameter.getOffset() / parameter.getLimit() + 1);

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

    @GetMapping("/{sku}")
    @RequiresPermissions("20015")
    public String detail(@PathVariable("sku") String sku) {

        List<LockOrder> list = new ArrayList<>();
        if (StringUtil.isEmpty(sku)) {
            return toLayUIData(0, list);
        }
        String clientCode = SessionLocal.getPrincipal().getClientCode();

        Set<String> lockList = RedisUtil.keys(RedisUtil.getLockOrderKey(clientCode, "*"));
        for (String l : lockList) {
            String qty = RedisUtil.hget(l, sku);
            if (StringUtil.isNotBlank(qty)) {
                LockOrder lockOrder = new LockOrder();
                lockOrder.setOrderNo(l);
                lockOrder.setQty(Integer.parseInt(qty));
                lockOrder.setCreatedTime(RedisUtil.hget(l, RedisUtil.LOCK_TIME));
                list.add(lockOrder);
            }
        }
        return toLayUIData(lockList.size(),list);

    }

    private static class LockOrder {

        private String orderNo;
        private Integer qty;
        private String createdTime;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Integer getQty() {
            return qty;
        }

        public void setQty(Integer qty) {
            this.qty = qty;
        }

        public String getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(String createdTime) {
            this.createdTime = createdTime;
        }
    }
}

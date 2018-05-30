package com.nilo.wms.web.controller.basic;

import com.nilo.wms.common.annotation.RequiresPermissions;
import com.nilo.wms.common.util.BeanUtils;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.platform.Sku;
import com.nilo.wms.dto.platform.parameter.SkuParam;
import com.nilo.wms.service.platform.SkuService;
import com.nilo.wms.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sku")
public class SKUController extends BaseController {
    @Autowired
    private SkuService skuService;

    @GetMapping
    @RequiresPermissions("20041")
    public String list(String searchValue, String searchKey) {

        SkuParam param = new SkuParam();
        if (StringUtil.isNotBlank(searchKey)) {
            BeanUtils.setProperty(param, searchKey, searchValue);
        }
        Page page = getPage();
        param.setPageInfo(page);
        List<Sku> list = skuService.queryBy(param);
        return toLayUIData(page.getCount(), list);
    }

    @PostMapping
    @RequiresPermissions("20042")
    public String add(Sku sku) {
        skuService.add(sku);
        return toJsonTrueMsg();

    }

    @PutMapping
    @RequiresPermissions("20043")
    public String update(Sku sku) {
        skuService.update(sku);
        return toJsonTrueMsg();
    }

    @DeleteMapping("/{sku}")
    @RequiresPermissions("20044")
    public String delete(@PathVariable("sku") String sku) {
        skuService.delete(sku);
        return toJsonTrueMsg();
    }

}

package com.nilo.wms.service.platform.impl;

import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.exception.SysErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dao.platform.PermissionDao;
import com.nilo.wms.dto.common.PageResult;
import com.nilo.wms.dto.common.ZTree;
import com.nilo.wms.dto.platform.parameter.PermissionParam;
import com.nilo.wms.dto.platform.system.Permission;
import com.nilo.wms.service.platform.PermissionService;
import com.nilo.wms.service.platform.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ronny on 2017/8/24.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionDao permissionDao;

    @Override
    public List<Permission> getMenusByUser(String userId) {

        List<Permission> results = new ArrayList<>();
        List<Permission> permissions = permissionDao.queryByUserId(userId);
        for (Permission p : permissions) {
            if ("0".equals(p.getParentId())) {
                List<Permission> subMenu = new ArrayList<>();
                for (Permission t : permissions) {
                    if (p.getPermissionId().equals(t.getParentId())) {
                        subMenu.add(t);
                    }
                }
                p.setSubMenus(subMenu);
                results.add(p);
            }
        }
        return results;
    }

    @Override
    public List<Permission> getPermissionByUser(String userId) {
        List<Permission> results = new ArrayList<>();
        List<Permission> permissions = permissionDao.queryByUserId(userId);
        for (Permission p : permissions) {
            if (p.getType() == 2) {
                results.add(p);
            }
        }
        return results;
    }

    @Override
    public List<ZTree> getPermissionTree(String roleId) {

        List<ZTree> listZTree = new ArrayList<>();

        List<Permission> allPermissions = permissionDao.queryAll();
        List<Permission> rolePermissions = permissionDao.queryByRoleId(roleId);

        for (Permission one : allPermissions) {
            ZTree zTree = new ZTree();
            zTree.setId(one.getPermissionId());
            zTree.setName(one.getDesc_e() + "-" + one.getDesc_c());
            zTree.setpId(one.getParentId());
            zTree.setOpen(true);
            zTree.setChecked(false);
            for (Permission temp : rolePermissions) {
                if (StringUtil.equals(temp.getPermissionId(), one.getPermissionId())) {
                    zTree.setChecked(true);
                    break;
                }
            }
            listZTree.add(zTree);
        }
        return listZTree;
    }

    @Override
    @Transactional
    public void updatePermissionTree(String roleId, List<String> permissionId) {

        //1、删除
        permissionDao.deletePermissionByRoleId(roleId);
        //2、插入
        permissionDao.insertRolePermission(permissionId, roleId);

        //更新缓存中信息
        RedisUtil.del(RedisUtil.getRoleKey(roleId));
        RedisUtil.sAdd(RedisUtil.getRoleKey(roleId), permissionId.toArray(new String[permissionId.size()]));

    }

    @Override
    public void add(Permission permission) {

        AssertUtil.isNotNull(permission, SysErrorCode.REQUEST_IS_NULL);
        AssertUtil.isNotBlank(permission.getPermissionId(), CheckErrorCode.PERMISSION_ID_EMPTY);
        AssertUtil.isNotNull(permission.getType(), CheckErrorCode.PERMISSION_Type_EMPTY);
        AssertUtil.isNotBlank(permission.getDesc_c(), CheckErrorCode.PERMISSION_DESC_EMPTY);
        AssertUtil.isNotBlank(permission.getDesc_e(), CheckErrorCode.PERMISSION_DESC_EMPTY);

        //查询permissionID是否已存在
        PermissionParam parameter = new PermissionParam();
        parameter.setPermissionId(permission.getPermissionId());

        List<Permission> p = permissionDao.queryPermissions(parameter);

        if (p != null && p.size() == 1) {
            throw new WMSException(BizErrorCode.PERMISSION_ID_EXIST);
        }

        permission.setStatus(1);
        permissionDao.insert(permission);



    }

    @Override
    public void update(Permission permission) {

        AssertUtil.isNotBlank(permission.getPermissionId(), CheckErrorCode.PERMISSION_ID_EMPTY);
        permissionDao.update(permission);

        if (permission.getStatus() != null) {
            //修改缓存数据
            Set<String> list = RedisUtil.keys(RedisUtil.getRoleKey("*"));
            for (String s : list) {
                if (permission.getStatus() == 0) {
                    Set<String> permissions = RedisUtil.sMember(s);
                    for (String p : permissions) {
                        if (StringUtil.equals(p, permission.getPermissionId())) {
                            RedisUtil.srem(s, new String[]{permission.getPermissionId()});
                            break;
                        }
                    }
                } else {
                    RedisUtil.sAdd(s, new String[]{permission.getPermissionId()});
                }
            }
        }

    }

    @Override
    public void delete(String permissionId) {

        AssertUtil.isNotBlank(permissionId, CheckErrorCode.PERMISSION_ID_EMPTY);

        permissionDao.deleteByPermissionId(permissionId);

        //修改缓存数据
        Set<String> list = RedisUtil.keys(RedisUtil.getRoleKey("*"));
        for (String s : list) {
            Set<String> permissions = RedisUtil.sMember(s);
            for (String p : permissions) {
                if (StringUtil.equals(p, permissionId)) {
                    RedisUtil.srem(s, new String[]{permissionId});
                }
            }
        }

    }

    @Override
    public PageResult<Permission> queryPermissions(PermissionParam parameter) {

        PageResult<Permission> pageResult = new PageResult<>();

        int count = permissionDao.queryPermissionsCount(parameter);
        if (count == 0) {
            return pageResult;
        }
        pageResult.setCount(count);
        List<Permission> list = permissionDao.queryPermissions(parameter);
        pageResult.setData(list);
        return pageResult;
    }
}

package com.ruoyi.flowable.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.flowable.domain.SysProcessCc;
import com.ruoyi.flowable.mapper.SysProcessCcMapper;
import com.ruoyi.flowable.service.ISysProcessCcService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;

@Service
public class SysProcessCcServiceImpl implements ISysProcessCcService {

    @Autowired
    private SysProcessCcMapper ccMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysRoleService roleService;

    @Override
    public void saveCcRecords(String processInstanceId, String processDefinitionId, String processKey,
                              String processName, Map<String, Object> config, String ccType) {
        Set<Long> ccUserIdSet = new HashSet<>();

        if ("user".equals(ccType)) {
            List<String> ccUsers = (List<String>) config.get("ccUsers");
            if (ccUsers != null) {
                for (String userId : ccUsers) {
                    ccUserIdSet.add(Long.parseLong(userId));
                }
            }
        } else if ("dept".equals(ccType)) {
            List<String> ccDepts = (List<String>) config.get("ccDepts");
            if (ccDepts != null) {
                for (String deptId : ccDepts) {
                    SysDept dept = deptService.selectDeptById(Long.parseLong(deptId));
                    if (dept != null) {
                        List<SysUser> users = userService.selectUserList(new SysUser());
                        for (SysUser user : users) {
                            if (dept.getDeptId().equals(user.getDeptId())) {
                                ccUserIdSet.add(user.getUserId());
                            }
                        }
                    }
                }
            }
        } else if ("role".equals(ccType)) {
            List<String> ccRoles = (List<String>) config.get("ccRoles");
            if (ccRoles != null) {
                for (String roleId : ccRoles) {
                    SysRole role = roleService.selectRoleById(Long.parseLong(roleId));
                    if (role != null) {
                        List<SysUser> users = userService.selectUserList(new SysUser());
                        for (SysUser user : users) {
                            if (hasRole(user, Long.parseLong(roleId))) {
                                ccUserIdSet.add(user.getUserId());
                            }
                        }
                    }
                }
            }
        }

        if (ccUserIdSet.isEmpty()) {
            return;
        }

        List<SysProcessCc> ccList = new ArrayList<>();
        for (Long userId : ccUserIdSet) {
            SysUser user = userService.selectUserById(userId);
            if (user != null) {
                SysProcessCc cc = new SysProcessCc();
                cc.setProcessInstanceId(processInstanceId);
                cc.setProcessDefinitionId(processDefinitionId);
                cc.setProcessKey(processKey);
                cc.setProcessName(processName);
                cc.setCcUserId(userId);
                cc.setCcUserName(user.getUserName());
                cc.setCcUserNickName(user.getNickName());
                cc.setStatus("0");
                ccList.add(cc);
            }
        }

        if (!ccList.isEmpty()) {
            ccMapper.batchInsertCc(ccList);
        }
    }

    private boolean hasRole(SysUser user, Long roleId) {
        List<SysRole> roles = user.getRoles();
        if (roles != null) {
            for (SysRole role : roles) {
                if (role.getRoleId().equals(roleId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<SysProcessCc> selectCcByUserId(Long ccUserId) {
        return ccMapper.selectCcByUserId(ccUserId);
    }

    @Override
    public void updateCcStatus(Long id) {
        SysProcessCc cc = new SysProcessCc();
        cc.setId(id);
        cc.setStatus("1");
        cc.setReadTime(new Date());
        ccMapper.updateCcStatus(cc);
    }

    @Override
    public void deleteCcByProcessInstanceId(String processInstanceId) {
        ccMapper.deleteCcByProcessInstanceId(processInstanceId);
    }
}
package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.SysProcessDraft;
import com.ruoyi.flowable.service.ISysProcessDraftService;

/**
 * 流程草稿Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/draft")
public class SysProcessDraftController
{
    @Autowired
    private ISysProcessDraftService sysProcessDraftService;

    /**
     * 查询流程草稿列表
     */
    @GetMapping("/list")
    public AjaxResult list(SysProcessDraft sysProcessDraft)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        sysProcessDraft.setCreateBy(String.valueOf(user.getUserId()));
        List<SysProcessDraft> list = sysProcessDraftService.selectSysProcessDraftList(sysProcessDraft);
        return AjaxResult.success(list);
    }

    /**
     * 获取流程草稿详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return AjaxResult.success(sysProcessDraftService.selectSysProcessDraftById(id));
    }

    /**
     * 根据流程Key获取草稿
     */
    @GetMapping("/getByProcessKey/{processKey}")
    public AjaxResult getByProcessKey(@PathVariable String processKey)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        SysProcessDraft draft = sysProcessDraftService.selectSysProcessDraftByProcessKeyAndCreateBy(processKey, user.getUserId());
        return AjaxResult.success(draft);
    }

    /**
     * 保存流程草稿
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody SysProcessDraft sysProcessDraft)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        sysProcessDraft.setCreateBy(String.valueOf(user.getUserId()));
        int result = sysProcessDraftService.saveSysProcessDraft(sysProcessDraft);
        return result > 0 ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }

    /**
     * 删除流程草稿
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        SysProcessDraft draft = sysProcessDraftService.selectSysProcessDraftById(id);
        if (draft == null) {
            return AjaxResult.error("草稿不存在");
        }
        if (!String.valueOf(user.getUserId()).equals(draft.getCreateBy())) {
            return AjaxResult.error("无权删除他人的草稿");
        }
        return sysProcessDraftService.deleteSysProcessDraftById(id) > 0 ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }

    /**
     * 批量删除流程草稿
     */
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Long[] ids)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        for (Long id : ids) {
            SysProcessDraft draft = sysProcessDraftService.selectSysProcessDraftById(id);
            if (draft == null || !String.valueOf(user.getUserId()).equals(draft.getCreateBy())) {
                return AjaxResult.error("存在无权删除的草稿");
            }
        }
        return sysProcessDraftService.deleteSysProcessDraftByIds(ids) > 0 ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }
}
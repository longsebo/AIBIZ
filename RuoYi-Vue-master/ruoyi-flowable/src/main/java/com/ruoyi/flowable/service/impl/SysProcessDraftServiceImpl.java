package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.flowable.domain.SysProcessDraft;
import com.ruoyi.flowable.mapper.SysProcessDraftMapper;
import com.ruoyi.flowable.service.ISysProcessDraftService;

/**
 * 流程草稿Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysProcessDraftServiceImpl implements ISysProcessDraftService
{
    @Autowired
    private SysProcessDraftMapper sysProcessDraftMapper;

    @Override
    public List<SysProcessDraft> selectSysProcessDraftList(SysProcessDraft sysProcessDraft)
    {
        return sysProcessDraftMapper.selectSysProcessDraftList(sysProcessDraft);
    }

    @Override
    public SysProcessDraft selectSysProcessDraftById(Long id)
    {
        return sysProcessDraftMapper.selectSysProcessDraftById(id);
    }

    @Override
    public SysProcessDraft selectSysProcessDraftByProcessKeyAndCreateBy(String processKey, Long createBy)
    {
        SysProcessDraft draft = new SysProcessDraft();
        draft.setProcessKey(processKey);
        draft.setCreateBy(String.valueOf(createBy));
        return sysProcessDraftMapper.selectSysProcessDraftByProcessKeyAndCreateBy(draft);
    }

    @Override
    public int saveSysProcessDraft(SysProcessDraft sysProcessDraft)
    {
        Long createBy = sysProcessDraft.getCreateBy() != null ? Long.parseLong(sysProcessDraft.getCreateBy()) : null;
        SysProcessDraft existing = selectSysProcessDraftByProcessKeyAndCreateBy(
            sysProcessDraft.getProcessKey(), createBy);
        
        if (existing != null) {
            existing.setProcessName(sysProcessDraft.getProcessName());
            existing.setFormData(sysProcessDraft.getFormData());
            existing.setAttachments(sysProcessDraft.getAttachments());
            return sysProcessDraftMapper.updateSysProcessDraft(existing);
        } else {
            return sysProcessDraftMapper.insertSysProcessDraft(sysProcessDraft);
        }
    }

    @Override
    public int deleteSysProcessDraftById(Long id)
    {
        return sysProcessDraftMapper.deleteSysProcessDraftById(id);
    }

    @Override
    public int deleteSysProcessDraftByIds(Long[] ids)
    {
        return sysProcessDraftMapper.deleteSysProcessDraftByIds(ids);
    }
}
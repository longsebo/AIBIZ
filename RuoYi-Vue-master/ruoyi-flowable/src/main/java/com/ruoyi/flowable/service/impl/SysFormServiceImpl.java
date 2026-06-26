package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.flowable.domain.SysForm;
import com.ruoyi.flowable.mapper.SysFormMapper;
import com.ruoyi.flowable.service.ISysFormService;

@Service
public class SysFormServiceImpl implements ISysFormService {

    @Autowired
    private SysFormMapper formMapper;

    @Override
    public List<SysForm> selectFormList(SysForm form) {
        return formMapper.selectFormList(form);
    }

    @Override
    public SysForm selectFormById(Long id) {
        return formMapper.selectFormById(id);
    }

    @Override
    public int insertForm(SysForm form) {
        return formMapper.insertForm(form);
    }

    @Override
    public int updateForm(SysForm form) {
        return formMapper.updateForm(form);
    }

    @Override
    public int deleteFormByIds(Long[] ids) {
        return formMapper.deleteFormByIds(ids);
    }
}

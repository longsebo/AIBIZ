package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.flowable.domain.SysProcessForm;
import com.ruoyi.flowable.mapper.SysProcessFormMapper;
import com.ruoyi.flowable.service.ISysProcessFormService;

@Service
public class SysProcessFormServiceImpl implements ISysProcessFormService {

    @Autowired
    private SysProcessFormMapper processFormMapper;

    @Override
    public List<SysProcessForm> selectProcessFormList(SysProcessForm processForm) {
        return processFormMapper.selectProcessFormList(processForm);
    }

    @Override
    public SysProcessForm selectProcessFormById(Long id) {
        return processFormMapper.selectProcessFormById(id);
    }

    @Override
    public SysProcessForm selectProcessFormByProcessKey(String processKey) {
        return processFormMapper.selectProcessFormByProcessKey(processKey);
    }

    @Override
    public int insertProcessForm(SysProcessForm processForm) {
        return processFormMapper.insertProcessForm(processForm);
    }

    @Override
    public int updateProcessForm(SysProcessForm processForm) {
        return processFormMapper.updateProcessForm(processForm);
    }

    @Override
    public int deleteProcessFormByIds(Long[] ids) {
        return processFormMapper.deleteProcessFormByIds(ids);
    }
}

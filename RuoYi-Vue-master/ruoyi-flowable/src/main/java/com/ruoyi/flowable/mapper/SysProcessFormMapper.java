package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.SysProcessForm;

public interface SysProcessFormMapper {

    public List<SysProcessForm> selectProcessFormList(SysProcessForm processForm);

    public SysProcessForm selectProcessFormById(Long id);

    public SysProcessForm selectProcessFormByProcessKey(String processKey);

    public int insertProcessForm(SysProcessForm processForm);

    public int updateProcessForm(SysProcessForm processForm);

    public int deleteProcessFormById(Long id);

    public int deleteProcessFormByIds(Long[] ids);
}

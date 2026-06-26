package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.SysProcessForm;

public interface ISysProcessFormService {

    List<SysProcessForm> selectProcessFormList(SysProcessForm processForm);

    SysProcessForm selectProcessFormById(Long id);

    SysProcessForm selectProcessFormByProcessKey(String processKey);

    int insertProcessForm(SysProcessForm processForm);

    int updateProcessForm(SysProcessForm processForm);

    int deleteProcessFormByIds(Long[] ids);
}

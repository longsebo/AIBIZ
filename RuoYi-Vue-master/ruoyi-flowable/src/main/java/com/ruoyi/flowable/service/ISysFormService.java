package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.SysForm;

public interface ISysFormService {

    List<SysForm> selectFormList(SysForm form);

    SysForm selectFormById(Long id);

    int insertForm(SysForm form);

    int updateForm(SysForm form);

    int deleteFormByIds(Long[] ids);
}

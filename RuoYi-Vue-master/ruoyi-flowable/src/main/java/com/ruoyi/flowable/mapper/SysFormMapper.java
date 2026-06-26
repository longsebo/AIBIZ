package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.SysForm;

public interface SysFormMapper {

    public List<SysForm> selectFormList(SysForm form);

    public SysForm selectFormById(Long id);

    public int insertForm(SysForm form);

    public int updateForm(SysForm form);

    public int deleteFormById(Long id);

    public int deleteFormByIds(Long[] ids);
}

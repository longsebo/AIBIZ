package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.SysProcessCc;

public interface SysProcessCcMapper {

    int insertCc(SysProcessCc cc);

    int batchInsertCc(List<SysProcessCc> ccList);

    List<SysProcessCc> selectCcByUserId(Long ccUserId);

    List<SysProcessCc> selectCcByProcessInstanceId(String processInstanceId);

    int updateCcStatus(SysProcessCc cc);

    int deleteCcByProcessInstanceId(String processInstanceId);
}
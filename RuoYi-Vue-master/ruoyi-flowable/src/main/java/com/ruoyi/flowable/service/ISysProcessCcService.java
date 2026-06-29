package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.SysProcessCc;

public interface ISysProcessCcService {

    void saveCcRecords(String processInstanceId, String processDefinitionId, String processKey, 
                       String processName, java.util.Map<String, Object> config, String ccType);

    List<SysProcessCc> selectCcByUserId(Long ccUserId);

    void updateCcStatus(Long id);

    void deleteCcByProcessInstanceId(String processInstanceId);
}
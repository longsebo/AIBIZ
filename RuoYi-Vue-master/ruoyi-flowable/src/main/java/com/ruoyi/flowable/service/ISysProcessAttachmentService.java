package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.SysProcessAttachment;

public interface ISysProcessAttachmentService {

    SysProcessAttachment selectAttachmentById(Long id);

    void saveAttachments(String processInstanceId, String processDefinitionId, String processKey, List<SysProcessAttachment> attachments);

    List<SysProcessAttachment> selectAttachmentsByProcessInstanceId(String processInstanceId);

    void deleteAttachmentsByProcessInstanceId(String processInstanceId);
}
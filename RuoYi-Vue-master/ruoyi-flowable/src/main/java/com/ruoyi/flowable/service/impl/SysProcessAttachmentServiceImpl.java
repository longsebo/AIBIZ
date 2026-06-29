package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.flowable.domain.SysProcessAttachment;
import com.ruoyi.flowable.mapper.SysProcessAttachmentMapper;
import com.ruoyi.flowable.service.ISysProcessAttachmentService;

@Service
public class SysProcessAttachmentServiceImpl implements ISysProcessAttachmentService {

    @Autowired
    private SysProcessAttachmentMapper attachmentMapper;

    @Override
    public SysProcessAttachment selectAttachmentById(Long id) {
        return attachmentMapper.selectAttachmentById(id);
    }

    @Override
    public void saveAttachments(String processInstanceId, String processDefinitionId, String processKey, 
                               List<SysProcessAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        for (SysProcessAttachment attachment : attachments) {
            attachment.setProcessInstanceId(processInstanceId);
            attachment.setProcessDefinitionId(processDefinitionId);
            attachment.setProcessKey(processKey);
        }
        attachmentMapper.batchInsertAttachment(attachments);
    }

    @Override
    public List<SysProcessAttachment> selectAttachmentsByProcessInstanceId(String processInstanceId) {
        return attachmentMapper.selectAttachmentByProcessInstanceId(processInstanceId);
    }

    @Override
    public void deleteAttachmentsByProcessInstanceId(String processInstanceId) {
        attachmentMapper.deleteAttachmentByProcessInstanceId(processInstanceId);
    }
}
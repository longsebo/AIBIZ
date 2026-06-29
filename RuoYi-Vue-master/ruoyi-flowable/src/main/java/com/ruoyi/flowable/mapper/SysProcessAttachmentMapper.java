package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.SysProcessAttachment;

public interface SysProcessAttachmentMapper {

    SysProcessAttachment selectAttachmentById(Long id);

    int insertAttachment(SysProcessAttachment attachment);

    int batchInsertAttachment(List<SysProcessAttachment> list);

    List<SysProcessAttachment> selectAttachmentByProcessInstanceId(String processInstanceId);

    int deleteAttachmentByProcessInstanceId(String processInstanceId);
}
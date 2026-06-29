package com.ruoyi.flowable.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class SysProcessAttachment {

    private Long id;

    private String processInstanceId;

    private String processDefinitionId;

    private String processKey;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
package com.ruoyi.flowable.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class SysProcessCc {

    private Long id;

    private String processInstanceId;

    private String processDefinitionId;

    private String processKey;

    private String processName;

    private Long ccUserId;

    private String ccUserName;

    private String ccUserNickName;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;
}
package com.ruoyi.flowable.domain;

import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程草稿对象 sys_process_draft
 * 
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysProcessDraft extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 流程定义Key */
    @Excel(name = "流程定义Key")
    private String processKey;

    /** 流程名称 */
    @Excel(name = "流程名称")
    private String processName;

    /** 表单数据(JSON格式) */
    @Excel(name = "表单数据")
    private String formData;

    /** 附件列表(JSON格式) */
    @Excel(name = "附件列表")
    private String attachments;



    /** 删除标志 */
    private String delFlag;
}
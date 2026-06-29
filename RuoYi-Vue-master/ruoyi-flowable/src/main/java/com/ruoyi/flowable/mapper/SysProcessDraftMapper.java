package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.SysProcessDraft;

/**
 * 流程草稿Mapper接口
 * 
 * @author ruoyi
 */
public interface SysProcessDraftMapper
{
    /**
     * 查询流程草稿列表
     * 
     * @param sysProcessDraft 流程草稿
     * @return 流程草稿集合
     */
    public List<SysProcessDraft> selectSysProcessDraftList(SysProcessDraft sysProcessDraft);

    /**
     * 查询流程草稿
     * 
     * @param id 流程草稿ID
     * @return 流程草稿
     */
    public SysProcessDraft selectSysProcessDraftById(Long id);

    /**
     * 根据流程key和创建人查询草稿
     * 
     * @param sysProcessDraft 流程草稿
     * @return 流程草稿
     */
    public SysProcessDraft selectSysProcessDraftByProcessKeyAndCreateBy(SysProcessDraft sysProcessDraft);

    /**
     * 新增流程草稿
     * 
     * @param sysProcessDraft 流程草稿
     * @return 结果
     */
    public int insertSysProcessDraft(SysProcessDraft sysProcessDraft);

    /**
     * 修改流程草稿
     * 
     * @param sysProcessDraft 流程草稿
     * @return 结果
     */
    public int updateSysProcessDraft(SysProcessDraft sysProcessDraft);

    /**
     * 删除流程草稿
     * 
     * @param id 流程草稿ID
     * @return 结果
     */
    public int deleteSysProcessDraftById(Long id);

    /**
     * 批量删除流程草稿
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysProcessDraftByIds(Long[] ids);
}
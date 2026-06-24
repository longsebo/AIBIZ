package com.ruoyi.system.service;

import com.ruoyi.system.domain.AiCommandMap;

import java.util.List;

/**
 * AI指令映射Service接口
 *
 * @author ruoyi
 */
public interface IAiCommandMapService {
    /**
     * 查询AI指令映射
     *
     * @param id 主键
     * @return AI指令映射
     */
    public AiCommandMap selectAiCommandMapById(Long id);

    /**
     * 查询AI指令映射列表
     *
     * @param aiCommandMap AI指令映射
     * @return AI指令映射集合
     */
    public List<AiCommandMap> selectAiCommandMapList(AiCommandMap aiCommandMap);

    /**
     * 根据指令关键词查询
     *
     * @param command 指令关键词
     * @return AI指令映射
     */
    public AiCommandMap selectAiCommandMapByCommand(String command);

    /**
     * 查询所有启用的指令映射
     *
     * @return 指令映射列表
     */
    public List<AiCommandMap> selectAiCommandMapAll();

    /**
     * 新增AI指令映射
     *
     * @param aiCommandMap AI指令映射
     * @return 结果
     */
    public int insertAiCommandMap(AiCommandMap aiCommandMap);

    /**
     * 修改AI指令映射
     *
     * @param aiCommandMap AI指令映射
     * @return 结果
     */
    public int updateAiCommandMap(AiCommandMap aiCommandMap);

    /**
     * 删除AI指令映射
     *
     * @param id 主键
     * @return 结果
     */
    public int deleteAiCommandMapById(Long id);
}

package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AiCommandMap;
import com.ruoyi.system.mapper.AiCommandMapMapper;
import com.ruoyi.system.service.IAiCommandMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI指令映射Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AiCommandMapServiceImpl implements IAiCommandMapService {

    @Autowired
    private AiCommandMapMapper aiCommandMapMapper;

    /**
     * 查询AI指令映射
     *
     * @param id 主键
     * @return AI指令映射
     */
    @Override
    public AiCommandMap selectAiCommandMapById(Long id) {
        return aiCommandMapMapper.selectAiCommandMapById(id);
    }

    /**
     * 查询AI指令映射列表
     *
     * @param aiCommandMap AI指令映射
     * @return AI指令映射
     */
    @Override
    public List<AiCommandMap> selectAiCommandMapList(AiCommandMap aiCommandMap) {
        return aiCommandMapMapper.selectAiCommandMapList(aiCommandMap);
    }

    /**
     * 根据指令关键词查询
     *
     * @param command 指令关键词
     * @return AI指令映射
     */
    @Override
    public AiCommandMap selectAiCommandMapByCommand(String command) {
        return aiCommandMapMapper.selectAiCommandMapByCommand(command);
    }

    /**
     * 查询所有启用的指令映射
     *
     * @return 指令映射列表
     */
    @Override
    public List<AiCommandMap> selectAiCommandMapAll() {
        return aiCommandMapMapper.selectAiCommandMapAll();
    }

    /**
     * 新增AI指令映射
     *
     * @param aiCommandMap AI指令映射
     * @return 结果
     */
    @Override
    public int insertAiCommandMap(AiCommandMap aiCommandMap) {
        return aiCommandMapMapper.insertAiCommandMap(aiCommandMap);
    }

    /**
     * 修改AI指令映射
     *
     * @param aiCommandMap AI指令映射
     * @return 结果
     */
    @Override
    public int updateAiCommandMap(AiCommandMap aiCommandMap) {
        return aiCommandMapMapper.updateAiCommandMap(aiCommandMap);
    }

    /**
     * 删除AI指令映射
     *
     * @param id 主键
     * @return 结果
     */
    @Override
    public int deleteAiCommandMapById(Long id) {
        return aiCommandMapMapper.deleteAiCommandMapById(id);
    }
}

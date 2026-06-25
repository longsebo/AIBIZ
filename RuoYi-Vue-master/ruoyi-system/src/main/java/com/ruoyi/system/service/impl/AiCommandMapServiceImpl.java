package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiCommandMap;
import com.ruoyi.system.mapper.AiCommandMapMapper;
import com.ruoyi.system.service.IAiCommandMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI指令映射Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AiCommandMapServiceImpl implements IAiCommandMapService {

    private static final Logger log = LoggerFactory.getLogger(AiCommandMapServiceImpl.class);

    @Autowired
    private AiCommandMapMapper aiCommandMapMapper;

    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String deepseekModel;

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
     * AI语义匹配指令
     *
     * @param userInput 用户输入
     * @param commandList 可用指令列表
     * @return 匹配的指令映射
     */
    @Override
    public AiCommandMap aiMatchCommand(String userInput, List<AiCommandMap> commandList) {
        if (StringUtils.isEmpty(userInput) || commandList == null || commandList.isEmpty()) {
            return null;
        }

        // 先尝试本地模糊匹配
        String input = userInput.toLowerCase().trim();
        for (AiCommandMap cmd : commandList) {
            String cmdText = cmd.getCommand().toLowerCase();
            // 精确匹配或包含匹配
            if (cmdText.equals(input) || cmdText.contains(input) || input.contains(cmdText)) {
                return cmd;
            }
        }

        // 如果本地匹配不到，调用DeepSeek API
        if (StringUtils.isNotEmpty(deepseekApiKey)) {
            try {
                return callDeepSeekApi(userInput, commandList);
            } catch (Exception e) {
                log.error("调用DeepSeek API失败: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * 调用DeepSeek API进行语义匹配
     */
    private AiCommandMap callDeepSeekApi(String userInput, List<AiCommandMap> commandList) throws Exception {
        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个指令匹配助手。用户输入了一句指令，请从以下指令列表中找到最匹配的一个。\n");
        prompt.append("返回格式要求：只返回匹配到的指令关键词（command字段的值），不要返回其他内容。\n");
        prompt.append("如果找不到匹配，返回\"NULL\"。\n\n");
        prompt.append("用户指令: ").append(userInput).append("\n\n");
        prompt.append("可用指令列表:\n");
        for (AiCommandMap cmd : commandList) {
            prompt.append("- ").append(cmd.getCommand())
                  .append(" (类型: ").append(cmd.getCommandType())
                  .append(", 菜单: ").append(cmd.getMenuName())
                  .append(")\n");
        }

        // 构建请求
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepseekModel);
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt.toString());
        requestBody.put("messages", new Object[]{message});

        // 发送请求
        URL url = new URL(deepseekApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + deepseekApiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = JSON.toJSONString(requestBody).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                // 解析响应
                Map<String, Object> respMap = JSON.parseObject(response.toString());
                List<Map<String, Object>> choices = (List<Map<String, Object>>) respMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> msg = (Map<String, Object>) choice.get("message");
                    String content = (String) msg.get("content");
                    content = content.trim();

                    // 如果返回NULL，说明没匹配到
                    if ("NULL".equalsIgnoreCase(content)) {
                        return null;
                    }

                    // 查找匹配的指令
                    for (AiCommandMap cmd : commandList) {
                        if (cmd.getCommand().equalsIgnoreCase(content.trim())) {
                            return cmd;
                        }
                    }
                }
            }
        } else {
            log.error("DeepSeek API返回错误码: {}", responseCode);
        }

        return null;
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
